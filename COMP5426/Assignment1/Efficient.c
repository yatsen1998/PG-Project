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

double** transpose(double** A, int row, int col)
{
    double* At0 = (double*)malloc(col * row * sizeof(double));
    double** At = (double**)malloc(row * sizeof(double*));
    for (int i = 0; i < col; ++i) {
        At[i] = At0 + col * i;
    }

    for (int i = 0; i < col; ++i) {
        for (int j = 0; j < row; ++j) {
            At[i][j] = A[j][i];
        }
    }
    return At;
}

void computeMatrix(int i, int j)
{
    int rowSize = min((i + 1) * B - 1, N - 1) - i * B + 1;
    int colSize = min((j + 1) * B - 1, N - 1) - j * B + 1;

    double* A0 = (double*)malloc(rowSize * M * sizeof(double));
    double** A = (double**)malloc(rowSize * sizeof(double*));
    for (int x = 0; x < rowSize; x++){
        A[x] = A0 + x * M;
    }

    for (int x = 0; x < rowSize; x++) {
        A[x] = sequences[i * B + x];
    }

    for (int x = 0; x < rowSize; x++) {
        for (int y = 0; y < M; y++) {
            printf("%lf ", A[x][y]);
        }
        printf("\n");
    }

    double* At0 = (double*)malloc(colSize * M * sizeof(double));
    double** At = (double**)malloc(colSize * sizeof(double*));
    for (int x = 0; x < colSize; x++){
        At[x] = At0 + x * M;
    }

    for (int x = 0; x < rowSize; x++) {
        At[x] = sequences[j * B + x];
    }

    printf("Calculating Transposition Matrix...\n");
    At = transpose(At, colSize, M);

    for (int x = 0; x < M; x++) {
        for (int y = 0; y < colSize; y++) {
            printf("%lf ", At[x][y]);
        }
        printf("\n");
    }
    return;

    // res_seq = (double*) malloc(N1 * N2 * sizeof(double));
    // resMat = (double**) malloc (N1 * sizeof(double*));
    // for (int i = 0; i < N1; ++i) {
    //     resMat[i] = res_seq + i * N2;
    // }

    // for (int x = i * B; x < rowSize; ++x) {
    //     for (int y = j * B; y < colSize; ++y) {
    //         for (int z = 0; z < M; ++z) {
    //             res += A[x][z] * At[z][y];
    //         }
    //     }
    // }
}

void* pthread_efficient(void* threadId)
{
    int* id = (int*) threadId;
    struct block curBlock;

    for (int i = 0; tasks[(*id)][i] != MAX_NUM; ++i) {
        curBlock = blocks[tasks[(*id)][i]];
        printf("Calculating Block:%d A%d A%d...\n", tasks[(*id)][i], curBlock.i, curBlock.j);
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
        }
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
    
    printf("\nAssigning tasks...\n");
    // Allocating taskList
    int max_task_size = blockNum / T + 1;
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

    for (int i = 0; i < num_threads; ++i) {
        printf("thread: %d task: ", i);
        for (int j = 0; j < max_task_size; ++j) {
            if (tasks[i][j] == MAX_NUM) continue;
            printf("%d ",tasks[i][j]);
        }
        printf("\n");
    }

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

    printf("Baseline Pthread method took %2f seconds to complete.\n\n", elapsed);

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
    free(V);

    return 0;
}
