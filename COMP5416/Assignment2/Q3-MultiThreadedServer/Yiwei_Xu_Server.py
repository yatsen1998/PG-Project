import socket
import uuid
from _thread import *

Server = socket.socket()
host = '127.0.0.1'
port = 12010
ThreadCount = 0
try:
    Server.bind((host, port))
except socket.error as e:
    print(str(e))

print("Server Started ...")
Server.listen(5)


def multi_threaded_client(connection):
    f = open(str(uuid.uuid4()), 'wb')
    while True:
        data = connection.recv(2048)
        if not data:
            break
        f.write(data)
        if len(data) == 0:
            break
    f.close()
    print("All data received, Connection closed\n")
    connection.close()



while True:
    print("Waiting for other connection ...")
    Client, address = Server.accept()
    #print('Connected to: ' + address[0] + ':' + str(address[1]))
    print("Connection established, receiving data...\n")
    start_new_thread(multi_threaded_client, (Client,))
    ThreadCount += 1
    #print('Thread Number: ' + str(ThreadCount))

Server.close()
