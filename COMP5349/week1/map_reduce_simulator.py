import sys

from mapper import RatingFilter
from reducer import RatingReducer


def map_reduce(file_name, mapper, reducer):
    """ This is the main program that scans through a given file and calls your filtering class for each line.

    Based on your filtering rules, a tuple in the form of (k,v) (or null) is output by your filter.
    """
    group_by_key = {}

    num_lines = 0
    num_records = 0

    with open(file_name) as file:
        for line in file:
            num_lines += 1
            result = mapper.map(line.strip())

            if result:
                num_records += 1
                if result[0] not in group_by_key:
                    group_by_key[result[0]] = []

                group_by_key[result[0]].append(result[1])

    for records in group_by_key.items():
        res = reducer.reduce(records[0], sorted(records[1]))
        print("{}: {}".format(records[0], res))

    if len(group_by_key) == 0 or not res:
        print("<empty results>")

    return num_lines, num_records

if __name__ == "__main__":
    filename = "data.csv"
    start_movie_id = 1
    end_movie_id = 10

    if len(sys.argv) >= 2:
        filename = sys.argv[1]
    if len(sys.argv) >= 3:
        start_movie_id = int(sys.argv[2])
    if len(sys.argv) >= 4:
        end_movie_id = int(sys.argv[3])

    rating_filter = RatingFilter(start_movie_id, end_movie_id)
    rating_reducer = RatingReducer()
    stats = map_reduce(filename, rating_filter, rating_reducer)

    print("MapReduce Simulator using mapper:  " + rating_filter.__repr__())
    print("MapReduce Simulator using reducer: " + rating_reducer.__repr__())
    print("Lines in File:    {} records (should be: {})".format(rating_filter.get_num_calls(), stats[0]))
    print("Filtered records: {} (should be: {})".format(rating_filter.get_num_records(), stats[1]))
