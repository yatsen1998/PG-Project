for (i=0; i<N; i++) {
    for (j=i;j<N;j+=3) {
        for (k=0; k<M; k+=3) {
            C[i,j] += A[i,k] * B[j,k] + A[i, k+1] * B[j, k+1] + A[i, k+2] * B[j, k+2];
        }

        for (k=0; k<M; k+=3) {
            C[i,j+1] += A[i,k] * B[j+1,k] + A[i, k+1] * B[j+1, k+1] + A[i, k+2] * B[j+1, k+2];
        }

        for (k=0; k<M; k+=3) {
            C[i,j+2] += A[i,k] * B[j+2,k] + A[i, k+1] * B[j+2, k+1] + A[i, k] * B[j+2, k+2];
        }
    }
}


int play_time;

pthread_mutex_t toy_mutex = PTHREAD_MUTEX_INITIALIZER; //static initialization
bool current_id = 0;
pthread_cond_t cond;

void child_routine(void * arg)
{
    int *myid;

    myid = (int *)arg;
    while (1) {
        pthread_mutex_lock(&toy_mutex);
        if (myid->id != current_id) {
            pthread_cond_wait(&cond, &toy_mutex);
        }

        current_id = myid->id;
        printf("child %d: I get to play with the toy for %d units of time.\n", myid->id, play_time);
        sleep(play_time);

        printf("child %d: I now give the toy to the other child.\n", myid->id);
        pthread_mutex_unlock(&toy_mutex);
        pthread_cond_signal(&cond);
    }
}

for (int stride = blockDim.x / 2; stride > 0; stride /= 2) {
   	if (tid < stride) {
       	idata[tid] += idata[tid + stride];
   	}
}