// My Answer for Week4 Tut3 Ex1
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define UNROLLING_FACTOR 4
struct timeval start_time, end_time;

int main(int argc, char* argv[])
{
    int N1 = atoi(argv[1]);
    int N2 = atoi(argv[2]);
    int M = atoi(argv[3]);
    int rem = 0;
    double sum = 0;
    double *A0;
    double *B0;
    double *C0;
    double **matrixA;
    double **matrixB;
    double **matrixC;

    A0 = (double*) malloc(N1 * M * sizeof(double));
    matrixA = (double**) malloc(N1 * sizeof(double*));
    for (int i = 0; i < N1; ++i) {
        matrixA[i] = A0 + i * M;
    }
    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < M; ++j) {
            matrixA[i][j] = (double) rand() / RAND_MAX;

        }
    }

    B0 = (double*) malloc(M * N2 * sizeof(double));
    matrixB = (double**) malloc(M * sizeof(double*));
    for (int i = 0; i < M; ++i) {
        matrixB[i] = B0 + i * N2;
    }
    for (int i = 0; i < M; ++i) {
        for (int j = 0; j < N2; ++j) {
            matrixB[i][j] = (double) rand() / RAND_MAX;
        }
    }

    C0 = (double*) malloc(N1 * N2 * sizeof(double));
    matrixC = (double**) malloc(N1 * sizeof(double*));
    for (int i = 0; i < N1; ++i) {
        matrixC[i] = C0 + i * N2;
    }

    gettimeofday(&start_time, 0);

    if (M % UNROLLING_FACTOR != 0) {
        rem = M % UNROLLING_FACTOR;
    }

    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < N2; ++j) {
            sum = 0;
            for (int z = 0; z < M - rem; z += UNROLLING_FACTOR) {
                sum += matrixA[i][z] * matrixB[z][j]
                    + matrixA[i][z + 1] * matrixB[z + 1][j]
                    + matrixA[i][z + 2] * matrixB[z + 2][j]
                    + matrixA[i][z + 3] * matrixB[z + 3][j];
            }

            for (int z = M - rem; z < M; z++) {
                sum += matrixA[i][z] * matrixB[z][j];
            }
            matrixC[i][j] = sum;
        }
    }    
    gettimeofday(&end_time, 0);

    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < N2; ++j) {
            printf("%2lf ", matrixC[i][j]);
        }
        printf("\n");
    }

    long seconds = end_time.tv_sec - start_time.tv_sec;
    long microseconds = end_time.tv_usec - start_time.tv_usec;
    double elapsed = seconds + 1e-6 * microseconds;

    printf("It took %f seconds to complete.\n\n", elapsed);

    free(A0);
    free(matrixA);
    free(B0);
    free(matrixB);
    free(C0);
    free(matrixC);

    return 0;
}
