/*
    Assignment1 for COMP5426
    Baseline with Pthread
*/

#include <stdio.h>
#include <stdlib.h>
#include<stdbool.h>
#include <sys/time.h>
#include <pthread.h>
#include <mpi.h>
#include <math.h>

#define MAX_NUM 0x3f3f3f3f
#define UNROLLING_FACTOR 4

int N;
int M;
int T;
int B;
int* R;
double* sequences;
double* V;

struct timeval start_time, end_time;


void print_matrix(double* mat, int m, int n);
void matMultiplyWithSingleThread(double* A, double* B, int m, int n, int x, int y);
void computeMatrix(int i, int j);
void* pthread_efficient(void* threadId);
bool check_result();

int main(int argc, char* argv[])
{
    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);

    int num_threads = T;

    int myid, numprocs;
    MPI_Status status;

    MPI_Init(&argc,&argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myid);

    B = N / numprocs;

    double* sequence;
    int blockN = N / B; // To simplify the calculation, demanded by requirement
    const int steps = (blockN + 1) / 2;
    int result_size = N * (N + 1) / 2;

    int value = 0;
    int pos = 0;

    /* Allocate result array. */
    V = (double*) malloc(result_size * sizeof(double));
    double* allV = NULL;

    /* Allocate pivot array */
    R = (int*) malloc(N * sizeof(int));

    /*
     *  OwnedBlock denotes the unchanged matrix of each process.
     *  ExchangedBlock denotes the exchanged matrix after each step. 
     */
    double* OwnedBlock = (double*) malloc(B * M * sizeof (double)); 
    double* ExchangedBlock = (double*) malloc(M * B * sizeof (double)); 

    if (myid == 0) {
        for (int i = 0; i < result_size; i++) {
            V[i] = 0;
        }

        allV = (double*)malloc(blockN * result_size * sizeof(double));

        R[0] = 0;
        for (int i = 1; i < N; i++) {
            R[i] = R[i - 1] + N - i + 1;
        }

        /* Generate Sequence matrix randomly. */
        sequences = (double*) malloc(N * M * sizeof(double));

        for (int i = 0; i < N; ++i) {
            for (int j = 0; j < M; ++j) {
                sequences[i * M + j] = (double) rand() / RAND_MAX;
                // printf("%lf ", sequences[i * M + j]);
            }
            // printf("\n");
        }

    }

    MPI_Bcast(V, result_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    MPI_Bcast(R, N, MPI_INT, 0, MPI_COMM_WORLD);

    MPI_Scatter(sequences, B * M, MPI_DOUBLE, OwnedBlock, B * M, MPI_DOUBLE, 0, MPI_COMM_WORLD); // Scatter the blocked matrix to each process
    MPI_Scatter(sequences, B * M, MPI_DOUBLE, ExchangedBlock, B * M, MPI_DOUBLE, 0, MPI_COMM_WORLD); // Scatter the blocked matrix to each process

    int leftRank = (myid + numprocs - 1) % numprocs;
    int rightRank = (myid + 1) % numprocs;

    for (int i = 0; i < steps; i++) {
        matMultiplyWithSingleThread(OwnedBlock, ExchangedBlock, B, M, myid, (myid + i) % numprocs);
        //ComputeMatrixWithThreads(ExchangedBlock);

        MPI_Sendrecv_replace((void*)ExchangedBlock, M * B * 2, MPI_FLOAT, leftRank, 0, rightRank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    // if (myid == 2) {
    //     for (int i = 0; i < result_size; i++) {
    //         printf("P:%d V[%d]:%lf\n", myid, i, V[i]);
    //     }
    // }

    MPI_Gather(V, result_size, MPI_DOUBLE, allV, result_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);

    if (myid == 0) {
        for (int i = 0; i < blockN * result_size; i++) {
            if (allV[i] != 0) {
                V[i % result_size] = allV[i];
            }
            // printf("V[%d]:%lf\n", i % result_size, allV[i]);
        }

        // for (int i = 0; i < result_size; i++) {
        //     printf("V[%d]:%lf\n", i, V[i]);
        // }

        bool check = check_result();

        if (!check)
            printf("Calculation is not correct.\n");
        else 
            printf("Calculation is correct!\n");
    }

    // /* Calculating Matrix */
    // int rc = 0;
    // int tids[num_threads];
    // pthread_t threads[num_threads];

    // // gettimeofday(&start_time, 0);

    // for (int i = 0; i < num_threads; ++i) {
    //     tids[i] = i;
    //     rc = pthread_create(&threads[i], NULL, pthread_efficient, (void*) &tids[i]);
    //     if (rc) {
    //         printf("Pthread Create Failed.\n");
    //         exit(-1);
    //     }
    // }

    // for (int i = 0; i < num_threads; i++) {
    //     pthread_join(threads[i], NULL);
    // }

    // gettimeofday(&end_time, 0);
    // seconds = end_time.tv_sec - start_time.tv_sec;
    // microseconds = end_time.tv_usec - start_time.tv_usec;
    // elapsed = seconds + 1e-6 * microseconds;

    // printf("Efficient Algorithm took %lf seconds to complete.\n\n", elapsed);



    free(sequences);
    free(R);
    free(V);

    MPI_Finalize();

    return 0;
}

void print_matrix(double* mat, int m, int n)
{
    for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                printf("%lf ", mat[i * n + j]);
            }
            printf("\n");
        }
}

void matMultiplyWithSingleThread(double* matA, double* matB, int m, int n, int x, int y)
{
    double* matResult = (double*) malloc(m * m * sizeof(double));
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < m; j++) {
            double tmp = 0;
            if (x == y && i > j) {
                matResult[i * m + j] = 0;
                continue;
            }

            for (int z = 0; z < n; z++) {
                tmp += matA[i * n + z] * matB[j * n + z];
            }
            matResult[i * m + j] = tmp;
        }
    }

    for (int i = 0; i < m; i++) {
        for (int j = 0; j < m; j++) {
            int realX = i + x * B;
            int realY = j + y * B;

            if (x > y) {
                int tmp = realX;
                realX = realY;
                realY = tmp;
            }
            if (realY < realX) continue;
            int k = R[realX] + realY - realX;
            V[k] = matResult[i * m + j];
        }
    }
}

// void computeMatrix(int i, int j)
// {
//     int rowSize = min((i + 1) * B - 1, N - 1) - i * B + 1;
//     int colSize = min((j + 1) * B - 1, N - 1) - j * B + 1;

//     //printf("rowSize:%d, colSize:%d, M:%d\n", rowSize, colSize, M);
//     int rem = M % UNROLLING_FACTOR;

//     int realX = 0, realY = 0;
//     int k = 0;
//     double res = 0, res1 = 0, res2 = 0, res3 = 0, res4 = 0;
//     for (int x = 0; x < rowSize; ++x) {
//         for (int y = 0; y < colSize; ++y) {
//             realX = x + i * B;
//             realY = y + j * B;
//             if (realY < realX) continue;
//             k = R[realX] + realY - realX;
//             res = 0;
//             for (int z = 0; z < M - rem; z += UNROLLING_FACTOR) {
//                 res1 = sequences[realX][z] * sequences[realY][z];
//                 res2 = sequences[realX][z + 1] * sequences[realY][z + 1];
//                 res3 = sequences[realX][z + 2] * sequences[realY][z + 2];
//                 res4 = sequences[realX][z + 3] * sequences[realY][z + 3];
//                 res += res1 + res2 + res3 + res4;
//             }

//             for (int z = M - rem; z < M; ++z) {
//                 res += sequences[realX][z] * sequences[realY][z];
//             }
//             V[k] = res;
//         }
//     }
// }

// void* pthread_efficient(void* threadId)
// {
//     int* id = (int*) threadId;
//     struct block curBlock;

//     for (int i = 0; tasks[(*id)][i] != MAX_NUM && i < max_task_size; ++i) {
//         curBlock = blocks[tasks[(*id)][i]];
//         //printf("Calculating Block:%d A%d A%d...\n", tasks[(*id)][i], curBlock.i, curBlock.j);
//         computeMatrix(curBlock.i, curBlock.j);
//     }

//     return NULL;
// }

bool check_result()
{
    int count = 0;
    double sum = 0;
    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            sum = 0;
            for (int z = 0; z < M; z++) {
                sum += sequences[i * M + z] * sequences[j * M + z];
            }

            if (fabs(V[count] - sum) > 0.000001) {
                for (int z = 0; z < M; z++) {
                    printf("%lf ", sequences[i * M + z]);
                }
                printf("\n");
                for (int z = 0; z < M; z++) {
                    printf("%lf ", sequences[j * M + z]);
                }
                printf("Computation Error count:%d V[count]:%lf sum:%lf\n", count, V[count], sum);
                return false;
            }
            count++;
        }
    }
    return true;
}