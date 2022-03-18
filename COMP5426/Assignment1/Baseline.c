/*
    Assignment1 for COMP5426
    Baseline with Pthread
*/

#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <pthread.h>

int N;
int M;
int T;
int workload = 0;
double** sequences;
double* result;

struct timeval start_time, end_time;

void* pthread_baseline(void* threadId)
{
    int count = 0;
    int* id;

    id = (int*) threadId;

    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            if(count >= workload * (*id) && count < workload * ((*id) + 1)) {
                //printf("%d count: %d\n", (*id), count);
                for (int z = 0; z < M; z++) {
                    result[count] += sequences[i][z] * sequences[j][z];
                }
                //printf("%d, result[%d]: %2lf\n", (*id), count, sum);
            }
            count++;

        }
    }

    return NULL;
}

int main(int argc, char* argv[])
{
    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);
    int num_threads = T;

    /* Allocate result array. */
    int result_size = N * (N + 1) / 2;
    result = (double*) malloc(result_size * sizeof(double));

    /* Generate Sequence matrix randomly. */
    double* sequence;
    sequence = (double*) malloc(N * M * sizeof(double));
    sequences = (double**) malloc(N * sizeof(double*));
    for (int i = 0; i < N; ++i) {
        sequences[i]  = sequence + M * i;
    }

    for (int i = 0; i < N; ++i) {
        for (int j = 0; j < M; ++j) {
            sequences[i][j] = (double) rand() / RAND_MAX;
        }
    }
    
    /* Baseline pthread implementation */
    int ret = 0;
    workload = (int)((double)result_size / T + 0.5);
    int tids[num_threads];
    pthread_t threads[num_threads];

    printf("result_size: %d workload:%d\n", result_size, workload);

    gettimeofday(&start_time, 0);

    for (int i = 0; i < num_threads; ++i) {
        tids[i] = i;
        ret = pthread_create(&threads[i], NULL, pthread_baseline, (void*) &tids[i]);
        if (ret) {
            printf("Pthread Create Failed.\n");
            exit(-1);
        }
    }

    for (int i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }

    gettimeofday(&end_time, 0);
    long seconds = end_time.tv_sec - start_time.tv_sec;
    long microseconds = end_time.tv_usec - start_time.tv_usec;
    double elapsed = seconds + 1e-6 * microseconds;

    printf("Baseline Pthread method took %2f seconds to complete.\n\n", elapsed);

    // for (int i = 0; i < result_size; i++) {
    //     printf("%2lf ", result[i]);
    // }

    free(sequence);
    free(sequences);
    free(result);

    return 0;
}
