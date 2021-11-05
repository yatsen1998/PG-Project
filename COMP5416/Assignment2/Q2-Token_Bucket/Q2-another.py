import numpy as np
import math

T = 100
arate = 1
drate = 1.25
r = 1 / drate
s = 2
x = 3 + math.floor(s / 2)

t = 0
arrival = []
while t < T:
    t = t + np.random.exponential(1.0 / arate)
    arrival.append(t)

t = 0
told = 0
N = 0
flag = False
B = 7
buffer = []
departure = []
recording = []

while t < T and len(arrival) > 0:

    if B < 7:
        B += 1
        if B > 7:
            B = 7

    while B > 0 and len(arrival) > 0:

        if len(departure) == 0:

            told = t
            t = arrival[0]
            del arrival[0]
            de = t + np.random.exponential(1.0 / drate)
            departure.append(de)
            B = B - 1
            recording.append([told, t, N, - B])
            N = N + 1

        else:

            if departure[0] < arrival[0]:
                if flag:
                    told = lastt
                else:
                    told = t
                t = departure[0]
                del departure[0]
                # recording.append([told, t, N, - B])

                N = N - 1
                if N >= 1:
                    de = t + np.random.exponential(1.0 / drate)
                    departure.append(de)
                    departure.sort()

            else:
                if flag:
                    told = lastt
                else:
                    told = t
                t = arrival[0]
                del arrival[0]
                B = B - 1
                recording.append([told, t, N, - B])
                N = N + 1
    flag = True
    lastt = t
    t = t + 1 / r

duration = []
x1 = 0.0
x2 = 0.0
for i in range(0, len(recording)):
    x1 = x1 + (recording[i][1] - recording[i][0]) * recording[i][2]
    x2 = x2 + (recording[i][1] - recording[i][0])

Mnumber = [x[2] for x in recording]
M = max(Mnumber)
distribution = [0 for i in range(M + 1)]

for i in range(0, len(recording)):
    state = recording[i][2]
    distribution[state] = distribution[state] + recording[i][1] - recording[i][0]

meanqueue = x1 / x2
print(meanqueue)
sumtime = sum(distribution)
pdf = [x / sumtime for x in distribution]
print(pdf)

# for i in range(0, len(recording)):
#    print(recording[i])
