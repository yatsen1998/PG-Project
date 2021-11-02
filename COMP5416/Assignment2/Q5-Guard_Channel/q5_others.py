import math


# Q5 (1)
def sigma1():
    sum = 0
    for i in range(1, 61 - 5):
        sum += ((50 ** i) / math.factorial(i))
    return sum


def sigma2():
    sum = 0
    for i in range(61 - 5, 61):
        sum += ((10 ** (i - 60 + 5)) * (50 ** (60 - 5)) / math.factorial(i))
    return sum


print(1 / (1 + sigma1() + sigma2()))
print((sigma2()+(50**55)/math.factorial(55))*(2.4304*(10**(-22))))
