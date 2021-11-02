import math


def get_numerator(i, l):
    result = math.pow(50, l) * math.pow(10, i - l) / math.factorial(i)
    return result


def get_denominator(l, s):
    n = 0
    result = 0.0
    while n <= l:
        # print(n)
        result += math.pow(50, n) / math.factorial(n)
        n += 1

    n = l + 1
    while n <= s:
        # print(n)
        result += math.pow(50, l) * math.pow(10, n - l) / math.factorial(n)
        n += 1

    return result


def getEachProbability(i, l, s):
    numerator = get_numerator(i, l)
    denominator = get_denominator(l, s)
    return numerator / denominator


l = 0
s = 60
sums = 1000
flag = 0

while l <= s:
    pb = 0.0
    pd = 0.0

    i = l
    while i <= s:
        pb += getEachProbability(i, l, s)
        i += 1

    pd = getEachProbability(s, l, s)

    tmp = 0.01 * pb + 0.1 * pd

    if tmp <= sums:
        sums = tmp
        flag = l
    l += 1

print(flag, sums)
