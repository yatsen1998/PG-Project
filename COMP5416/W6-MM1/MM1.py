
import numpy as np


T=20
arate=3
drate=4



t=0
arrival=[]
while t<T:
    t=t+np.random.exponential(1.0/arate)
    arrival.append(t)



t=0
N=0
departure=[]
recording=[]



while (t<T and len(arrival)>0):

    if len(departure)==0:

        told=t
        t=arrival[0]
        del arrival[0]        
        de=t+np.random.exponential(1.0/drate)
        departure.append(de)
        recording.append([told,t,N])
        N=N+1
    else:

        
            if departure[0]<arrival[0]:
                told=t
                t=departure[0]
                del departure[0]
                recording.append([told,t,N])
                
                N=N-1
                if N>=1:
                    de=t+np.random.exponential(1.0/drate)
                    departure.append(de)
                    departure.sort()
                
            else:
                told=t
                t=arrival[0]
                del arrival[0]
                recording.append([told,t,N])
                N=N+1


duration=[]
x1=0.0
x2=0.0
for i in range(0, len(recording)):
    x1=x1+(recording[i][1]-recording[i][0])*recording[i][2]
    x2=x2+(recording[i][1]-recording[i][0])


Mnumber=[x[2] for x in recording]
M=max(Mnumber)
distribution=[0 for i in range(M+1)] 

for i in range(0, len(recording)):
    state=recording[i][2]
    distribution[state]=distribution[state]+recording[i][1]-recording[i][0]



meanqueue=x1/x2
print (meanqueue)
sumtime=sum(distribution)
pdf=[x/sumtime for x in distribution]
print(pdf)

for i in range(0, len(recording)):
    print(recording[i])
            
    
        
    
    
