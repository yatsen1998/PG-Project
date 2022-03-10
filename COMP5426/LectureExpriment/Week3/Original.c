#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

#define N1 1024
#define N2 1024
#define M 1024
struct timeval start_time, end_time;

int main()
{
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
    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < N2; ++j) {
            for (int z = 0; z < M; ++z) {
                matrixC[i][j] += matrixA[i][z] * matrixB[z][j];
            }
        }
    }
    gettimeofday(&end_time, 0);

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
