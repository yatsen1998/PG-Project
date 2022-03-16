// My Answer for Week4 Tut3 Ex2
#include <stdio.h>
#include <stdlib.h>
#include <sys/time.h>

struct timeval start_time, end_time;

int main(int argc, char* argv[])
{
    int N = atoi(argv[1]);
    int M = atoi(argv[2]);

    // Alloc result array.
    double *result;
    int result_size = N * (N + 1) / 2;
    result = (double*) malloc(result_size * sizeof(double));

    // Generate Sequence matrix.
    double* sequence;
    double** sequences;
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

    int count = 0;
    double sum = 0;

    // Do the Calculation
    gettimeofday(&start_time, 0);

    for (int i = 0; i < N; i++) {
        for (int j = i; j < N; j++) {
            sum = 0;
            for (int z = 0; z < M; z++) {
                sum += sequences[i][z] * sequences[j][z];
            }
            result[count] = sum;
            count++;
        }
    }

    gettimeofday(&end_time, 0);

    long seconds = end_time.tv_sec - start_time.tv_sec;
    long microseconds = end_time.tv_usec - start_time.tv_usec;
    double elapsed = seconds + 1e-6 * microseconds;

    printf("It took %f seconds to complete.\n\n", elapsed);

    // for (int i = 0; i < count; i++) {
    //     printf("%2lf ", result[i]);
    // }

    free(sequence);
    free(sequences);
    free(result);

    return 0;
}
