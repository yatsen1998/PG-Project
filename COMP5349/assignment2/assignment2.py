from pyspark.sql import SparkSession
from pyspark.sql.functions import explode, explode_outer
from pyspark.sql.functions import udf

# Read Json files
spark = SparkSession \
    .builder \
    .appName("COMP5349 A2 Data Loading") \
    .config("spark.executor.memory", "4g") \
    .config("spark.driver.memory",'4g') \
    .getOrCreate()

data = "test.json"
init_df = spark.read.json(data)

contract_num = 102

data_df= init_df.select((explode("data").alias('data')))
paragraph_unrolled_df = data_df.select(explode("data.paragraphs").alias("paragraph")) \
                               .select("paragraph.context" , explode("paragraph.qas").alias("qas")) \
                               .withColumnRenamed("paragraph.context", "context") \
                               .select("context", "qas.id", "qas.question", "qas.is_impossible", explode_outer("qas.answers").alias("answer")) \
                               .withColumnRenamed("qas.id", "id").withColumnRenamed("qas.question", "question").withColumnRenamed("qas.is_impossible", "is_impossible") \
                               .select("context", "id", "question", "is_impossible", "answer.answer_start", "answer.text")

# paragraph_unrolled_df.show(5)
# paragraph_unrolled_df.printSchema()

import re
from pyspark.sql.types import ArrayType, StringType, LongType, BooleanType, IntegerType
from pyspark.sql.functions import split

@udf(returnType = ArrayType(StringType()))
def segmentContext(str):
    res = [repr(i)+','+str[i:i+4096] for i in range(0, len(str) - 2048, 2048)]
    return res

# res = segmentContext("1111122222\n\n\n\n\n333334444455")
# print(res)

paragraph_preproc_df = paragraph_unrolled_df.withColumn("sequence", explode(segmentContext("context"))).drop("context") \
                                            .withColumn("index", split("sequence",",").getItem(0)) \
                                            .withColumn("source", split("sequence",",").getItem(1)) \
                                            .drop("sequence")
# paragraph_preproc_df.show(5)
# paragraph_preproc_df.printSchema()

from pyspark.sql.functions import col, count
from pyspark.sql.window import Window

@udf(returnType = LongType())
def getEndPos(startPos, text):
    length = len(text)
    return startPos + length

@udf(returnType = StringType())
def getOverlappedPos(start, end, index):
    index = int(index)
    if start > index and start < index + 4096:
        if end < index + 4096 :
            return str(start % 4096) + ','+ str(end % 4096)
        else:
            return str(start % 4096) + ','+ str(4096)
    if end > index and end < index + 4096:
        if start < index:
            return str(index % 4096) + ','+ str(end % 4096)
    else:
        return "0,0"


ps_preproc_df = paragraph_preproc_df.filter("is_impossible==False").drop("is_impossible") \
                                         .withColumn("answer_end", getEndPos("answer_start", "text")) \
                                         .withColumn("overlapped", getOverlappedPos("answer_start", "answer_end", "index")) \
                                         .drop("answer_start", "answer_end") \
                                         .withColumn("answer_start", split("overlapped",",").getItem(0).cast('int')) \
                                         .withColumn("answer_end", split("overlapped",",").getItem(1).cast('int')) \
                                         .drop("overlapped", "index")

positive_sample_df = ps_preproc_df.filter((col("answer_start") != 0) | (col("answer_end") != 0)) \
                                       .select("id", "source", "question", "answer_start", "answer_end") \

window = Window.partitionBy(["id"])
ps_question_count = positive_sample_df.withColumn("n", count("id").over(window)) \
                                      .select("id", "question", "n") \
                                      .dropDuplicates(["id"])
ps_question_count_list = ps_question_count.collect()


ps_all_count = positive_sample_df.groupBy("question").count() \
                                 .withColumnRenamed("count", "n")

ps_all_count_list = ps_all_count.collect()

from pyspark.sql.functions import row_number

@udf(returnType = IntegerType())
def getAvgInOther(id, question):
    answer_num = 0
    res = 0
    # To get the question numbers in the local contract
    for row in ps_question_count_list:
        if id == row["id"]:
            answer_num = int(row["n"])
    
    # Use the total question reduce the local question number to get the number of 
    # that question in other contracts
    for row in ps_all_count_list:
        if question == row["question"]:
             res = int(row["n"]) - answer_num
    return int(res / contract_num - 1)

impossible_negative_preproc_df = paragraph_preproc_df.filter("is_impossible==true").fillna(0).drop("text", "is_impossible", "index") \
                                                     .withColumnRenamed("source", "im_source") \
                                                     .withColumnRenamed("answer_start", "im_answer_start")

# To remove the duplicate sequences with the positive samples and distinct the values
# Use a join and null check to realize the prior purpose
impossible_negative_sample_df = impossible_negative_preproc_df.join(positive_sample_df, ["id","question"], "outer") \
                                                            .filter("source is null or im_source != source") \
                                                            .drop("source", "answer_start", "answer_end") \
                                                            .withColumnRenamed("im_answer_start", "answer_start") \
                                                            .withColumnRenamed("im_answer_start", "answer_start") \
                                                            .withColumnRenamed("im_source", "source") \
                                                            .withColumn("answer_end", col("answer_start")) \
                                                            .select("id", "source", "question", "answer_start", "answer_end") \

# impossible_negative_sample_df.show(10)
# impossible_negative_sample_df.describe(["id"]).show()

window = Window.partitionBy(["id"]).orderBy(col("id"))
impossible_negative_samples = impossible_negative_sample_df.withColumn("row", row_number().over(window)) \
                                 .dropDuplicates(["id", "source"]) \
                                 .filter(col("row") <= getAvgInOther("id", "question")) \
                                 .drop("id", "row")

@udf(returnType = IntegerType())
def getQuestionNum(id):
    for row in ps_question_count_list:
        if row["id"] == id:
            return int(row["n"])
    return 0

possible_negative_preproc_df = ps_preproc_df.filter((col("answer_start") == 0) & (col("answer_end") == 0)).drop( "text") \
                                            .withColumnRenamed("source", "p_source") \
                                            .withColumnRenamed("answer_start", "p_answer_start") \
                                            .withColumnRenamed("answer_end", "p_answer_end")

# possible_negative_preproc_df.show(50)
possible_negative_sample_df = possible_negative_preproc_df.join(positive_sample_df, ["id", "question"], "outer") \
                                                          .filter("source is null or p_source != source") \
                                                          .drop("source", "answer_start", "answer_end") \
                                                          .withColumnRenamed("p_source", "source") \
                                                          .withColumnRenamed("p_answer_start", "answer_start") \
                                                          .withColumnRenamed("p_answer_end", "answer_end")

# possible_negative_sample_df.show(20)
# possible_negative_sample_df.describe(["id"]).show()

window = Window.partitionBy(["id"]).orderBy(col("id"))
possible_negative_samples = possible_negative_sample_df.withColumn("row", row_number().over(window)) \
                                 .dropDuplicates(["id", "source"]) \
                                 .filter(col("row") <= getQuestionNum("id")) \
                                 .drop("id", "row")

# possible_negative_samples.show(10)

positive_samples = positive_sample_df.drop("id")

output_sample = positive_samples.union(possible_negative_samples).union(impossible_negative_samples)
output_sample.write.mode('Overwrite').json("result")

# The following code can cause OOM exception easily, I recommend use the hdfs commmand to get the merged json file
# output_sample.coalesce(1).write.mode("overwrite").format("json").save("result")

# hdfs dfs -getmerge result result.json