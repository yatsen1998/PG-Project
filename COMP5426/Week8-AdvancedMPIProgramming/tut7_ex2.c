#include <mpi.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc, char **argv) {
    int myid;
    int numprocs;
    int *A0, **A;
    int M, N, K;
    int q, r;
    int ib, kn, i, j;
    MPI_Status status;  

    MPI_Init (&argc, &argv);
    MPI_Comm_size (MPI_COMM_WORLD, &numprocs);
    MPI_Comm_rank (MPI_COMM_WORLD, &myid);

	if (argc != 3){
            if (myid == 0){
		printf("Wrong number of arguments.\n");
		printf("Please enter the command in the following format:\n");
		printf("mpirun -np [proc num] main [matrix rows M] [matrix columns N]\n");
		printf("SAMPLE: mpirun -np 3 main 20 20\n");
            }

            MPI_Finalize();
            return 0;             
	}
	
	M = atoi(argv[1]);
	N = atoi(argv[2]);
	
	if (myid == 0) {

		//create matrix A of size M X N.
		A0 = malloc(M * N * sizeof(int));
		A = malloc(M * sizeof(int *));
		if(A == NULL){
			fprintf(stderr, "**A out of memory\n");
			exit(1);
		}
		for(i = 0; i < M; i++)
			A[i] = &A0[i*N];
		
		//initialize matrix to 0.
		for (i=0; i<M; i++)
			for (j=0; j<N; j++)
				A[i][j] = 0;
			
		printf("\n Output Initial Matrix A:\n");
		for(i=0; i<M; i++){
			for(j=0; j<N; j++){
				printf("%d  ", A[i][j]);
				if(j == N - 1)
				printf("\n\n");
			}
		}

		if (numprocs > 1){
			//send a submatrix to every other processes
			q = M / numprocs;
			r = M % numprocs;

			for (i=1; i<numprocs; i++){
				//calculate the first row.
				if (i < r){
					ib = i * (q+1);
					K = q+1;
				}
				else{
					ib = i * q + r;
					K = q;
				}
				printf("i = %d, ib = %d, K = %d.\n", i, ib, K);
				kn = K * N;
				MPI_Send(&A[ib][0], kn, MPI_INT, i, 1, MPI_COMM_WORLD);
			}

			//receive a submatrix from every other processes
			for (i=1; i<numprocs; i++)	{
				//calculate the first row.
				if (i < r){
					ib = i * (q+1);
					K = q+1;
				}
				else{
					ib = i * q + r;
					K = q;
				}
				kn = K * N;
				printf("i = %d, ib = %d, K = %d.\n", i, ib, K);
				MPI_Recv(&A[ib][0], kn, MPI_INT, i, 2, MPI_COMM_WORLD, &status);
			}
		}

		printf("\n Output Updated Matrix A:\n");
		for(i=0; i<M; i++){
			for(j=0; j<N; j++){
				printf("%d  ", A[i][j]);
				if(j == N - 1)
				printf("\n\n");
			}
		}
    }
    else {
		//create a submatrix A of size K X N.
		q = M / numprocs;
		r = M % numprocs;
		if (myid < r)
			K = q+1;
		else
			K = q;
		kn = K * N;

		A0 = malloc(K * N * sizeof(int));
		A = malloc(K * sizeof(int *));
		if(A == NULL){
			fprintf(stderr, "**A out of memory\n");
			exit(1);
		}
		for(i = 0; i < K; i++)
			A[i] = &A0[i*N];

		printf("myid = %d, K = %d.\n", myid, K);	

		/* recv a submatrix from process 0.*/
		MPI_Recv(&A[0][0], kn, MPI_INT, 0, 1, MPI_COMM_WORLD, &status);
		
		/*update matrix A.*/
		for (i=0; i<K; i++)
			for (j=0; j<N; j++)
				A[i][j] += myid;
			
		/* send the submatrix back to process 0. */
        MPI_Send(&A[0][0], kn, MPI_INT, 0, 2, MPI_COMM_WORLD); 

    }

    MPI_Finalize();
    return 0;
}
