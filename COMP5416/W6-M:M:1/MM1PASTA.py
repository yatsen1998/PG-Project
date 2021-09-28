
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
        recording.append(N)
        N=N+1
    else:
        

        
            if departure[0]<arrival[0]:
                told=t
                t=departure[0]
                del departure[0]
                
                
                N=N-1
                if N>=1:
                    de=t+np.random.exponential(1.0/drate)
                    departure.append(de)
                    departure.sort()
                
            else:
                told=t
                t=arrival[0]
                del arrival[0]
                recording.append(N)
                N=N+1




M=max(recording)
distribution=[0 for i in range(M+1)] 

for i in range(0, len(recording)):
    state=recording[i]
    distribution[state]=distribution[state]+1

sumtime=len(recording)
pdf2=[float(x)/sumtime for x in distribution]

meanqueue=np.mean(recording)
    
print(meanqueue)        
print(pdf2)

for i in range(0, len(recording)):
    print(recording[i])
    
