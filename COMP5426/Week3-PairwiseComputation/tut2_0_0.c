// Sample Answer for Week3 Tut2
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <sys/time.h>

void pairwise_two_mat_0(double** A, double** B, double** C, int rows1, int rows2, int cols);
void print_matrix(double** T, int rows, int cols);

int main(int argc, char *argv[]){
    // declare variables and arguments
    double* A00; 
    double* A01;
    double** A0;
    double** A1; //the two-dimensional matrix
    double* C0;
    double** C; //resulting matrix
    int N1, N2, M; //matrix sizes N1 x M and N2 x M
    struct timeval start_time, end_time;

   if(argc == 4){
        N1 = atoi(argv[1]); 
        N2 = atoi(argv[2]);
        M = atoi(argv[3]); 

        printf("N1 = %d, N2 = %d, M = %d\n\n", N1, N2, M);
    }  
    else{
            printf("Usage: %s N1 N2 M\n\n"
                   " N1: matrix A0 row length\n"
                   " N2: matrix A1 row length\n"
                   " M:  matrix column length\n\n",argv[0]);
        return 1;
    }

    // Initialise the random matrices
    A00 = (double*)malloc(N1*M*sizeof(double));
    A0 = (double**)malloc(N1*sizeof(double*));
    for (int i=0; i<N1; i++){
        A0[i] = A00 + i*M;
    }
 
    A01 = (double*)malloc(N2*M*sizeof(double));
    A1 = (double**)malloc(N2*sizeof(double*));
    for (int i=0; i<N2; i++){
        A1[i] = &(A01[i*M]);
    }

    C0 = (double*)malloc(N1*N2*sizeof(double));
    C = (double**)malloc(N1*sizeof(double*));
    for (int i=0; i<N1; i++){
        C[i] = &(C0[i*N2]);
    }

    srand(time(0)); // Seed the random number generator

    // Initialize matrices
    for(int i=0; i < N1; i++)
        for (int j=0; j < M; j++)
            A0[i][j] = (double) rand() / RAND_MAX; //elements < 1.0

    for(int i=0; i < N2; i++)
        for (int j=0; j < M; j++)
            A01[j+i*M] = (double) rand() / RAND_MAX; //elements < 1.0

    for(int i=0; i < N1; i++)
        for(int j=0; j < N2; j++)
            C0[j+i*N2] = 0.0; // elements = 0.0

    // print initialized matrices
//    print_matrix(A0, N1, M);
//    print_matrix(A1, N2, M);
//    print_matrix(C, N1, N2);

    gettimeofday(&start_time, 0);
    pairwise_two_mat_0(A0, A1, C, N1, N2, M);
    gettimeofday(&end_time, 0);

    //print results
//    print_matrix(C, N1, N2);

    //print the running time
    long seconds = end_time.tv_sec - start_time.tv_sec;
    long microseconds = end_time.tv_usec - start_time.tv_usec;
    double elapsed = seconds + 1e-6 * microseconds;
    printf("it took %f seconds to complete.\n\n", elapsed);

    return 0;
}

/* Pairwise computation of two matrices. Each row i in A (rows1 by cols) is paired with every 
   row j in B (rows2 by cols). The inner product of each pair (i, j) is computed and stored in 
   C[i][j] (equivalent to C = C + A*B_T).
   Warning: The code is not efficient and needs to be optimized.
*/
void pairwise_two_mat_0(double** A, double** B, double** C, int rows1, int rows2, int cols){
    for (int i=0; i < rows1; i++)
        for (int j=0; j < rows2; j++)
            for(int k=0; k < cols; k++)
                C[i][j] += A[i][k] * B[j][k];
}

void print_matrix(double** T, int rows, int cols){
    for (int i=0; i < rows; i++){
        for (int j=0; j < cols; j++)
            printf("%.2f  ", T[i][j]);
        printf("\n");
    }
    printf("\n\n");
    return;
}

