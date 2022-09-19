spark-submit \
    --master yarn \
    --deploy-mode cluster \
    --driver-memory 2G \
    --driver-cores 2 \
    --num-executors 4 \
    --executor-cores 4 \
    --executor-memory 4G \
    assignment2.py