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

#define min(a,b) ( ((a)>(b)) ? (b):(a) )
#define MAX_NUM 0x3f3f3f3f
#define UNROLLING_FACTOR 4

pthread_mutex_t lock1;

int N;
int M;
int T;
int B;
double** sequences;
double* V;
int* R;

struct timeval start_time, end_time;

struct block
{
    int bid;
    int i;
    int j;
    int weight;
};

struct block* blocks;

int** tasks;
int* workloads;
int max_task_size;

int cmp(const void *a, const void *b)
{
    int weight1 = ((struct block *) a)->weight;
    int weight2 = ((struct block *) b)->weight;
    return weight1 < weight2 ? 1 : -1;
}

int getMinFromNThread(int size) 
{
    int minLoadThread = 0;
    for (int i = 0; i < size; i++) {
        if (workloads[i] < workloads[minLoadThread]) {
            minLoadThread = i;
        }
    }
    return minLoadThread;
}

int getFirstNoAssignmentPos(int threadId, int size)
{
    for (int i = 0; i < size; ++i) {
        if (MAX_NUM == tasks[threadId][i])
            return i;
    }
    return -1;
}

void computeMatrix(int i, int j)
{
    int rowSize = min((i + 1) * B - 1, N - 1) - i * B + 1;
    int colSize = min((j + 1) * B - 1, N - 1) - j * B + 1;

    //printf("rowSize:%d, colSize:%d, M:%d\n", rowSize, colSize, M);
    int rem = M % UNROLLING_FACTOR;

    int realX = 0, realY = 0;
    int k = 0;
    double res = 0, res1 = 0, res2 = 0, res3 = 0, res4 = 0;
    for (int x = 0; x < rowSize; ++x) {
        for (int y = 0; y < colSize; ++y) {
            realX = x + i * B;
            realY = y + j * B;
            if (realY < realX) continue;
            k = R[realX] + realY - realX;
            res = 0;
            for (int z = 0; z < M - rem; z += UNROLLING_FACTOR) {
                res1 = sequences[realX][z] * sequences[realY][z];
                res2 = sequences[realX][z + 1] * sequences[realY][z + 1];
                res3 = sequences[realX][z + 2] * sequences[realY][z + 2];
                res4 = sequences[realX][z + 3] * sequences[realY][z + 3];
                res += res1 + res2 + res3 + res4;
            }

            for (int z = M - rem; z < M; ++z) {
                res += sequences[realX][z] * sequences[realY][z];
            }
            V[k] = res;
        }
    }
}

void* pthread_efficient(void* threadId)
{
    int* id = (int*) threadId;
    struct block curBlock;

    for (int i = 0; tasks[(*id)][i] != MAX_NUM && i < max_task_size; ++i) {
        curBlock = blocks[tasks[(*id)][i]];
        //printf("Calculating Block:%d A%d A%d...\n", tasks[(*id)][i], curBlock.i, curBlock.j);
        computeMatrix(curBlock.i, curBlock.j);
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
            if (fabs(V[count] - sum) > 0.000001) {
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
    B = atoi(argv[4]);
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
            //printf("%lf ", sequences[i][j]);
        }
        //printf("\n");
    }

    /* Efficient pthread implementation */
    
    printf("\nPartitioning Blocks...\n");
    int blockN = ceil((double)N / B);;     // To make more even blocks, B should be divisible by N
    int blockNum = blockN * (blockN + 1) / 2;

    int* block_R;
    block_R = (int*) malloc(blockN * sizeof(int));
    block_R[0] = 0;
    for (int i = 1; i < blockN; i++) {
        block_R[i] = block_R[i - 1] + blockN - i + 1;
    }

    blocks = (struct block*) malloc(blockNum * sizeof(struct block));
    for (int i = 0; i < blockN; ++i) {
        for (int j = i; j < blockN; ++j) {
            int k = block_R[i] + j - i;
            blocks[k].bid = k;
            blocks[k].i = i;
            blocks[k].j = j;
            int rowSize = min((i + 1) * B - 1, N - 1) - i * B + 1;
            int colSize = min((j + 1) * B - 1, N - 1) - j * B + 1;
            
            if (i == j) blocks[k].weight = rowSize * (rowSize + 1) / 2; // Note that the diagonal blocks should have less work
            else blocks[k].weight = rowSize * colSize; 
        }
    }

    qsort(blocks, blockNum, sizeof(struct block), cmp);
    // for (int i = 0; i < blockNum; ++i) {
    //     printf("");
    // }

    printf("\nAssigning tasks...\n");
    // Allocating taskList
    max_task_size = blockNum;
    int* task = (int*) malloc(num_threads * max_task_size * sizeof(int));
    tasks = (int**) malloc(num_threads * sizeof(task));
    for (int i = 0; i < num_threads; ++i) {
        tasks[i] = task + max_task_size * i;
    }

    for (int i = 0; i < num_threads; ++i) {
        for (int j = 0; j < max_task_size; ++j) {
            tasks[i][j] = MAX_NUM;
        } 
    }

    workloads = (int*) malloc(num_threads * sizeof(int));
    for (int i = 0; i < num_threads; ++i) {
        workloads[i] = 0;
    }

    // A rather primitive assigning algorithm,
    // it assigns current task to the current lightest thread.
    for (int i = 0; i < blockNum; ++i) {
        int toAssignThread = getMinFromNThread(num_threads);
        int freeIdPos = getFirstNoAssignmentPos(toAssignThread, max_task_size);
        tasks[toAssignThread][freeIdPos] = blocks[i].bid;
        //printf("%d tasks[%d][%d]:%d \n", i, toAssignThread, freeIdPos, tasks[toAssignThread][freeIdPos]);
        workloads[toAssignThread] += blocks[i].weight;
    }

    // for (int i = 0; i < num_threads; ++i) {
    //     printf("thread: %d task: ", i);
    //     for (int j = 0; j < max_task_size; ++j) {
    //         if (tasks[i][j] == MAX_NUM) continue;
    //         printf("%d ",tasks[i][j]);
    //     }
    //     printf("total_workload:%d\n", workloads[i]);
    // }

    printf("\nCalculating Matrix...\n");

    int rc = 0;
    int tids[num_threads];
    pthread_t threads[num_threads];

    gettimeofday(&start_time, 0);

    for (int i = 0; i < num_threads; ++i) {
        tids[i] = i;
        rc = pthread_create(&threads[i], NULL, pthread_efficient, (void*) &tids[i]);
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

    printf("\nEfficient Pthread method took %lf seconds to complete.\n\n", elapsed);

    // for (int i = 0; i < result_size; i++) {
    //     printf("%2lf ", result[i]);
    // }

    bool check = check_result();

    if (!check)
        printf("Calculation is not correct.\n");
    else 
        printf("Calculation is correct!\n");

    free(task);
    free(tasks);
    free(workloads);
    free(sequence);
    free(sequences);
    free(blocks);
    free(block_R);
    free(R);
    free(V);

    return 0;
}
