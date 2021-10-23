import matplotlib.pyplot as plt

# fig, ax = plt.subplots()
# ax.spines['right'].set_visible(False)
# ax.spines['top'].set_visible(False)
# plt.xlabel('x')
# plt.ylabel('y')
# plt.xlim(xmax=5, xmin=0)
# plt.ylim(ymax=4, ymin=0)
# x1 = [1, 2, 3, 4, 5]
# y1 = [2.4118, 2.3837, 1.5294, 2.6, 1.5663]
# x2 = x1
# y2 = [0.64, 0.83034, 0.76093, 0.32199, 0.55581]
# plt.plot(x1, y1, color='r', label='test')
# plt.plot(x2, y2, color='g', label='test2')
# plt.title(r'Title')
# plt.legend()
# plt.text(1, 3.0, r'1', color='r', fontsize=15)
# plt.text(3, 3.0, r'2', color='r', fontsize=15)
# plt.show()


cwnd1 = 10
cwnd2 = 5
cwnd3 = 1
cwnd1_list = [10]
cwnd2_list = [5]
cwnd3_list = [1]
cong_AtoB = cwnd1 + cwnd3
cong_BtoC = cwnd2 + cwnd3
interval = 1
x_list=[0]
while interval < 100:
    interval += 1
    x_list.append(interval)
    cwnd1 += 1
    cwnd1_list.append(cwnd1)
    cwnd2 += 1
    cwnd2_list.append(cwnd2)
    cwnd3 += 0.5
    cwnd3_list.append(cwnd3)
    cong_AtoB = cwnd1 + cwnd3
    cong_BtoC = cwnd2 + cwnd3
    if cong_AtoB >= 40:
        cwnd1 /= 2
        cwnd3 /= 2
    if cong_BtoC >= 40:
        cwnd2 /= 2
        cwnd3 /= 2

for i in cwnd1_list:
    print(i, end=',')
print("\n")
for i in cwnd2_list:
    print(i, end=',')
print("\n")
for i in cwnd3_list:
    print(i, end=',')
print("\n")

fig, ax = plt.subplots()
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)
plt.xlabel('time interval')
plt.ylabel('cwnd size/MSS')

plt.plot(x_list, cwnd1_list, color='r', label='cwnd1')
plt.plot(x_list, cwnd2_list, color='g', label='cwnd2')
plt.plot(x_list, cwnd3_list, color='b', label='cwnd3')

plt.legend()
plt.show()