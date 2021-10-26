from socket import *
import uuid

serverPort = 12010
serverSocket = socket(AF_INET, SOCK_STREAM)
Thread_count = 0

serverSocket.bind(('127.0.0.1', serverPort))
serverSocket.listen(5)

print("Server Started...")
print("Waiting for other connections")

conn_socket, addr = serverSocket.accept()
print("Connection established, receiving data...")

f = open(str(uuid.uuid4()), 'wb')
l = conn_socket.recv(1024)

while True:
    f.write(l)
    l = conn_socket.recv(1024)
    if (len(l)==0):
        break

f.close()

print("All data received, Connection lost")

conn_socket.close()
