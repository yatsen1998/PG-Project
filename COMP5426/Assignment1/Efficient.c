/* 
    Assignment1 for COMP5426
    Effcient Design with pthread, blocking and Loop Unrolling
*/

#include <stdio.h>
#include <stdlib.h>
#include<stdbool.h>
#include <sys/time.h>
#include <pthread.h>
#include <math.h>

#define UNROLLING_FACTOR 4

int N;
int M;
int T;
int B = 1;
int workload = 0;
double** sequences;
double* result;

struct timeval start_time, end_time;

void* pthread_lp(void* threadId)
{
    int count = 0;
    int* id;

    id = (int*) threadId;

    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            if(count >= workload * (*id) && count < workload * ((*id) + 1)) {
                //printf("%d count: %d\n", (*id), count);
                for (int z = 0; z < M; z += UNROLLING_FACTOR) {
                    result[count] = sequences[i][z] * sequences[j][z]
                                  + sequences[i][z + 1] * sequences[j][z + 1]
                                  + sequences[i][z + 2] * sequences[j][z + 2]
                                  + sequences[i][z + 3] * sequences[j][z + 3];
                }
                //printf("%d, result[%d]: %2lf\n", (*id), count, sum);
            }
            count++;
        }
    }

    return NULL;
}

void* pthread_efficient(void* threadId)
{
    int count = 0;
    int* id;
    double sum = 0;

    id = (int*) threadId;

    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            if(count >= workload * (*id) && count < workload * ((*id) + 1)) {
                //printf("%d count: %d\n", (*id), count);
                sum = 0;
                for (int z = 0; z < M; z += B) {
                    for (int zz = z; zz < z + B; zz += UNROLLING_FACTOR) {
                        sum = sequences[i][zz + 1] * sequences[j][zz + 1] +
                            sequences[i][zz + 1] * sequences[j][zz + 1] +
                            sequences[i][zz + 1] * sequences[j][zz + 1] +
                            sequences[i][zz + 1] * sequences[j][zz + 1];
                    }
                }
                //printf("%d, result[%d]: %2lf\n", (*id), count, sum);
                result[count] = sum;
            }
            count++;
        }
    }

    return NULL;
}

bool check_result()
{
    int count = 0;
    double sum = 0;
    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            sum = 0;
            for (int z = 0; z < M; z++) {
                sum += sequences[i][z] * sequences[j][z];
            }
            if (result[count] != sum) {
                return false;
            }
            count++;
        }
    }
    return true;
}

int main(int argc, char* argv[])
{
    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);
    B = atoi(argv[4]);
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

    printf("result_size: %d workload:%d blocksize:%d\n", result_size, workload, B);

    gettimeofday(&start_time, 0);

    for (int i = 0; i < num_threads; ++i) {
        tids[i] = i;
        ret = pthread_create(&threads[i], NULL, pthread_efficient, (void*) &tids[i]);
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

    printf("Efficient method took %2f seconds to complete.\n", elapsed);

    // check_result();
    // if (!check_result())
    //     printf("Calculation is not correct.\n");
    // else 
    //     printf("Calculation is correct!\n");

    free(sequence);
    free(sequences);
    free(result);

    return 0;
}
