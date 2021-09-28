
import numpy as np


T=20000
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




while (t<T and len(arrival)>0):

    if len(departure)==0:

        told=t
        t=arrival[0]
        del arrival[0]        
        de=t+np.random.exponential(1.0/drate)
        departure.append(de)
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

                N=N+1


    
        
    
    
