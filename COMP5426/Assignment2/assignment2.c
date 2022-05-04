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
int num_threads;
int* R;
double* sequences;
double* V;

struct timeval start_time, end_time;
typedef struct {
    int id;
    double* matA;
    double* matB;
    int m;
    int n;
    int x;
    int y;
}arguments;

void print_matrix(double* mat, int m, int n);
void matMultiplyWithSingleThread(double* A, double* B, int m, int n, int x, int y);
void* pthread_do_computation(void* arg);
void matMultiplyWithPThread(double* A, double* B, int m, int n, int x, int y);
bool check_result();

int main(int argc, char* argv[])
{
    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);

    num_threads = T;

    int myid, numprocs;

    MPI_Init(&argc,&argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myid);

    B = N / numprocs;

    int blockN = N / B; // To simplify the calculation, demanded by requirement
    const int steps = (blockN + 1) / 2;
    int result_size = N * (N + 1) / 2;

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
        // printf("\n");
    }

    MPI_Bcast(V, result_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);
    MPI_Bcast(R, N, MPI_INT, 0, MPI_COMM_WORLD);

    MPI_Scatter(sequences, B * M, MPI_DOUBLE, OwnedBlock, B * M, MPI_DOUBLE, 0, MPI_COMM_WORLD); // Scatter the blocked matrix to each process
    MPI_Scatter(sequences, B * M, MPI_DOUBLE, ExchangedBlock, B * M, MPI_DOUBLE, 0, MPI_COMM_WORLD); // Scatter the blocked matrix to each process

    int leftRank = (myid + numprocs - 1) % numprocs;
    int rightRank = (myid + 1) % numprocs;

    for (int i = 0; i < steps; i++) {
        // matMultiplyWithSingleThread(OwnedBlock, ExchangedBlock, B, M, myid, (myid + i) % numprocs);
        matMultiplyWithPThread(OwnedBlock, ExchangedBlock, B, M, myid, (myid + i) % numprocs);
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
    printf("\n");
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

void* pthread_do_computation(void* arg) 
{
    arguments* p = (arguments*) arg;
    int id = (int) p->id;
    int m = (int) p->m;
    int n = (int) p->n;
    int x = (int) p->x;
    int y = (int) p->y;
    double* matA= p->matA;
    double* matB= p->matB;

    int work = 0;
    int total = 0;
    if (x == y) {
        total = B * (B + 1) / 2;
        work = ceil((double)total / num_threads);
    }
    else {
        total = B * B;
        work = ceil((double)total / num_threads);
    }

    int count = 0;
    double sum = 0;
    for (int i = 0; i < m; i++) {
        for (int j = 0; j < m; j++) {
            if (x == y && i > j) continue;
            if(count >= work * id && count < work * (id + 1)) {
                sum = 0;
                for (int z = 0; z < n; z++) {
                    sum += matA[i * n + z] * matB[j * n + z];
                }
                int realX = i + x * B;
                int realY = j + y * B;

                if (x > y) {
                    int tmp = realX;
                    realX = realY;
                    realY = tmp;
                }

                if (realY < realX) continue;
                int k = R[realX] + realY - realX;
                V[k] = sum;
                // printf("V[%d]:%lf ", k, V[k]);

                count++;
            } else {
                count++;
                continue;
            }
        }
    }

    pthread_exit(0);
}


void matMultiplyWithPThread(double* matA, double* matB, int m, int n, int x, int y)
{
    /* Calculating Matrix */
    int rc = 0;
    pthread_t threads[num_threads];

    arguments* arg = (arguments*) malloc(sizeof(arguments) * num_threads);
    // // gettimeofday(&start_time, 0);

    for (int i = 0; i < num_threads; ++i) {
        arg[i].id = i;
        arg[i].matA = matA;
        arg[i].matB = matB;
        arg[i].m = m;
        arg[i].n = n;
        arg[i].x = x;
        arg[i].y = y;

        rc = pthread_create(&threads[i], NULL, pthread_do_computation, (void*) (arg + i));
        if (rc) {
            printf("Pthread Create Failed.\n");
            exit(-1);
        }
    }

    for (int i = 0; i < num_threads; i++) {
        pthread_join(threads[i], NULL);
    }

    // gettimeofday(&end_time, 0);
    // seconds = end_time.tv_sec - start_time.tv_sec;
    // microseconds = end_time.tv_usec - start_time.tv_usec;
    // elapsed = seconds + 1e-6 * microseconds;

}

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