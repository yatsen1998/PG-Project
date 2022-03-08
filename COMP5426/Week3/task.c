#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>

struct timeval start_time, end_time;

int main(int argc, char* argv[])
{
    int N1 = atoi(argv[1]);
    int N2 = atoi(argv[2]);
    int M = atoi(argv[3]);
    double **mat1;
    double **mat2;
    double **resMat;
    double *mat1_seq;
    double *mat2_seq;
    double *res_seq;

    // Random initialize matrix1
    mat1_seq = (double*) malloc(N1 * M * sizeof(double));
    mat1 = (double**) malloc(N1 * sizeof(double*));
    for (int i = 0; i < N1; ++i) {
        mat1[i] = mat1_seq + i * M;
    }

    for (int i = 0; i < N1; i++) {
        for (int j = 0; j < M; j++) {
            mat1[i][j] = (double) rand() / RAND_MAX;
        }
    }

    // Random initialize matrix2
    mat2_seq = (double*) malloc(M * N2 * sizeof(double));
    mat2 = (double**) malloc(M * sizeof(double*));
    for (int i = 0; i < M; ++i) {
        mat2[i] = mat2_seq + i * N2;
    }

    for (int i = 0; i < N1; i++) {
        for (int j = 0; j < M; j++) {
            mat2[i][j] = (double) rand() / RAND_MAX;
        }
    }

    // Init and Calculate the result matrix
    res_seq = (double*) malloc(N1 * N2 * sizeof(double));
    resMat = (double**) malloc (N1 * sizeof(double*));
    for (int i = 0; i < N1; ++i) {
        resMat[i] = res_seq + i * N2;
    }

    gettimeofday(&start_time, 0);
    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < N2; ++j) {
            for (int z = 0; z < M; ++z) {
                resMat[i][j] += mat1[i][z] * mat2[z][j];
            }
        }
    }
    gettimeofday(&end_time, 0);

    for (int i = 0; i < N1; ++i) {
        for (int j = 0; j < N2; ++j) {
            printf("%3lf", resMat[i][j]);
        }
    }

    long seconds = end_time.tv_sec - start_time.tv_sec;
    long microseconds = end_time.tv_usec - start_time.tv_usec;
    double elapsed = seconds + 1e-6 * microseconds;

    printf("It took %f seconds to complete.\n\n", elapsed);
    return 0;
}
