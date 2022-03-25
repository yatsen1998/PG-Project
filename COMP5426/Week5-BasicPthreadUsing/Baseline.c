/*
    Assignment1 for COMP5426
    Baseline with Pthread
*/

#include <stdio.h>
#include <stdlib.h>
#include<stdbool.h>
#include <sys/time.h>
#include <pthread.h>
#include <math.h>

#define MIN(a, b) a <= b ? a : b;

int N;
int M;
int T;
int workload = 0;
double** sequences;
double* V;
int* R;

struct timeval start_time, end_time;
struct coordinate
{
    int i;
    int j;
};

void getCoordinate(int k, struct coordinate* co)
{
    for (int i = 0; i < N; i++) {
        for (int j = i; j < M; j++) {
            if (k == R[i] + j - i) {
                co->i = i;
                co->j = j;
            }
        }
    }
}

void* pthread_baseline(void* threadId)
{
    int* id;

    id = (int*) threadId;

    int start = workload * (*id);
    int end = MIN(workload * ((*id) + 1), N * (N + 1) / 2);
    struct coordinate* co;
    co = (struct coordinate*) malloc (sizeof(struct coordinate));

    printf("id:%d start:%d, end:%d\n", (*id), start, end);
    for (int k = start; k < end; ++k) {
        getCoordinate(k, co);

        for (int z = 0; z < M; z++) {
            V[k] +=  sequences[co->i][z] * sequences[co->j][z];
        }
        //printf("k:%d i:%d j:%d V[k]:%2lf\n", k, co->i, co->j, V[k]);
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
            if (V[count] != sum) {
                printf("count:%d V[count]:%lf sum:%lf\n", count, V[count], sum);
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
    int num_threads = T;

    /* Allocate result array. */
    int result_size = N * (N + 1) / 2;
    V = (double*) malloc(result_size * sizeof(double));

    /* Allocate pivot array */
    R = (int*) malloc(N * sizeof(int));
    R[0] = 0;
    for (int i = 1; i < N; i++) {
        R[i] = R[i - 1] + N - i + 1;
    }

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
    
    int rc = 0;
    workload = ceil((double)result_size / T);
    int tids[num_threads];
    pthread_t threads[num_threads];

    printf("result_size: %d workload:%d\n", result_size, workload);

    gettimeofday(&start_time, 0);

    for (int i = 0; i < num_threads; ++i) {
        tids[i] = i;
        rc = pthread_create(&threads[i], NULL, pthread_baseline, (void*) &tids[i]);
        if (rc) {
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

    bool check = check_result();

    if (!check)
        printf("Calculation is not correct.\n");
    else 
        printf("Calculation is correct!\n");

    free(sequence);
    free(sequences);
    free(V);

    return 0;
}
