import matplotlib.pyplot as plt
import math


def BPSK_func(snr):
    sigma = math.sqrt(1 / math.pow(10, snr / 10))
    return (math.erfc((1 / sigma) / math.sqrt(2))) / 2


def FourQAM_func(snr):
    sigma = math.sqrt(1 / (2 * (math.pow(10, snr / 10))))
    q = (math.erfc((1 / sigma) / math.sqrt(2))) / 2
    return 2 * q - q * q


dB = 0
x_list = []
bpsk_list = []
fourQAM_list = []
while dB <= 25:
    x_list.append(dB)
    bpsk_list.append(BPSK_func(dB))
    fourQAM_list.append(FourQAM_func(dB))

    print("BPSK x: %d, y: %s" % (dB, format(BPSK_func(dB), '.8E')))
    print("4QAM x: %d, y: %s" % (dB, format(FourQAM_func(dB), '.8E')))

    dB += 5

fig, ax = plt.subplots()
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)
plt.xlabel('SNR(dB)')
plt.ylabel('BER')
plt.yscale('log')

plt.plot(x_list, bpsk_list, color='r', label='BPSK')
plt.plot(x_list, fourQAM_list, color='g', label='4QAM')

plt.legend()
plt.show()
