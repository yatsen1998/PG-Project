from socket import *

import time

serverName = 'localhost'
serverPort = 12010
clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket.connect((serverName, serverPort))

file_name = input("File name: ")

try:
    t_start = time.time()
    f0 = open(file_name, 'rb')
    data = f0.read()
    f0.close()
    clientSocket.send(data)

except IOError:
    print('No such file')

clientSocket.close()
