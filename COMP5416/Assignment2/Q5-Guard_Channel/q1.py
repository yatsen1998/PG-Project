import math


def get_numerator(i):
    result = math.pow(50, i) / math.factorial(i)
    return result


def get_denominator():
    n = 0
    result = 0
    while n <= 55:
        #print(n)
        result += math.pow(50, n) / math.factorial(n)
        n += 1

    n = 56
    while n <= 60:
        #print(n)
        result += math.pow(50, 55) * math.pow(10, n - 55) / math.factorial(n)
        n += 1

    return result


def getEachProbability(i):
    numerator = get_numerator(i)
    denominator = get_denominator()
    return numerator / denominator


i = 56
probability = []
sum = 0
while i <= 60:
    sum += getEachProbability(i)
    probability.append(getEachProbability(i))
    print("%f" % getEachProbability(i))
    i += 1

print(sum)