class Reducer(object):
    """The top-most Reducer interface.

    You need to implement the reduce method.
    """

    def reduce(self, key, values):
        raise NotImplementedError


class RatingReducer(Reducer):
    """Example Reducer that does nothing at the moment.

    Your task is to implement the reduce method which will return the average film rating.
    """

    def reduce(self, key, values):
        sum = 0
        for val in values:
            sum += (float)(val)
        return round(sum/len(values), 1)
