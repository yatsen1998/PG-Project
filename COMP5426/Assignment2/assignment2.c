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
    /* Requirements for N, M, T: N = np * T, M = k * UNROLLING_FACTOR (k as a integer) */
    N = atoi(argv[1]);
    M = atoi(argv[2]);
    T = atoi(argv[3]);

    num_threads = T;

    int myid, numprocs;

    MPI_Init(&argc,&argv);
    MPI_Comm_size(MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myid);

    B = N / numprocs;
    /* To simplify the calculation, demanded by requirement */
    int blockN = N / B; 
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
            }
        }
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
        /* Send and replace the exchanged blocks as requested */
        MPI_Sendrecv_replace((void*)ExchangedBlock, M * B * 2, MPI_FLOAT, leftRank, 0, rightRank, 0, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    MPI_Gather(V, result_size, MPI_DOUBLE, allV, result_size, MPI_DOUBLE, 0, MPI_COMM_WORLD);

    if (myid == 0) {
        for (int i = 0; i < blockN * result_size; i++) {
            if (allV[i] != 0) {
                V[i % result_size] = allV[i];
            }
        }

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
    /* A simple matrix to handle*/
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
    /* A single threaded function to help test the MPI code */
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

void index_conversion(int i, int j, int x, int y, double sum)
{
    /* To convert the coordinate to the actual location in the large matrix */
    int realX = i + x * B;
    int realY = j + y * B;

    if (x > y) {
        int tmp = realX;
        realX = realY;
        realY = tmp;
    }

    if (realY < realX) return;
    int k = R[realX] + realY - realX;
    V[k] = sum;
}

void* pthread_do_computation(void* arg) 
{
    /* To do the actual computation within each thread */
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
    double res1 = 0, res2 = 0, res3 = 0, res4 = 0;
    for (int i = 0; i < m; i += 2) {
        /* Outer Loop unrolliong factor: 2
         *
         * Of course, the unrolling can be more complex, as all the 3
         * level can be unrolled.
         * Unroll all the level here is a manual labour work, I will only
         * unrolls the first and the last level to prove I have got the
         * knowledge of unrolling outer loop to increase efficiency.
         */
        for (int j = 0; j < m; j++) {
            if (x == y && i > j) continue;
            if(count >= work * id && count < work * (id + 1)) {
                sum = 0;
                /* Inner Loop unrolling factor: 4
                 * The laziness also made me to assume all the B should be n times of 4.
                 */
                for (int z = 0; z < n; z += UNROLLING_FACTOR) {
                    res1 = matA[i * n + z] * matB[j * n + z];
                    res2 = matA[i * n + z + 1] * matB[j * n + z + 1];
                    res3 = matA[i * n + z + 2] * matB[j * n + z + 2];
                    res4 = matA[i * n + z + 3] * matB[j * n + z + 3];
                    sum += res1 + res2 + res3 + res4;
                }
                index_conversion(i, j, x, y, sum);
            }
            count++;
        }

        for (int j = 0; j < m; j++) {
            if (x == y && (i + 1) > j) continue;
            if(count >= work * id && count < work * (id + 1)) {
                sum = 0;
                for (int z = 0; z < n; z += UNROLLING_FACTOR) {
                    res1 = matA[(i + 1) * n + z] * matB[j * n + z];
                    res2 = matA[(i + 1) * n + z + 1] * matB[j * n + z + 1];
                    res3 = matA[(i + 1) * n + z + 2] * matB[j * n + z + 2];
                    res4 = matA[(i + 1) * n + z + 3] * matB[j * n + z + 3];
                    sum += res1 + res2 + res3 + res4;
                }
                index_conversion(i + 1, j, x, y, sum);
            }
            count++;
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
    /* The sequence computation to check the result */
    int count = 0;
    double sum = 0;
    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            sum = 0;
            for (int z = 0; z < M; z++) {
                sum += sequences[i * M + z] * sequences[j * M + z];
            }

            if (fabs(V[count] - sum) > 0.000001) {
                printf("Computation Error count:%d V[count]:%lf sum:%lf\n", count, V[count], sum);
                return false;
            }
            count++;
        }
    }
    return true;
}