  #include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

#define DEFAULT_TAG			1


int main (int argc , char **argv)
{
	int myid, numprocs;
	int token = 0;
	MPI_Status status;
	MPI_Init(&argc,&argv);
	MPI_Comm_size(MPI_COMM_WORLD,&numprocs);
	MPI_Comm_rank(MPI_COMM_WORLD,&myid);

	if (myid == 0) {
		printf("This is process %i from %i - I have the token now and its value is %d\n",myid,numprocs,token);
		token++;
		if (numprocs > 1)
		{
			MPI_Send(&token, 1, MPI_INT, 1, DEFAULT_TAG, MPI_COMM_WORLD);
			MPI_Recv(&token, 1, MPI_INT, numprocs - 1, DEFAULT_TAG, MPI_COMM_WORLD, &status);
		}
	}
	else {
		MPI_Recv(&token, 1, MPI_INT, myid - 1, , MPI_COMM_WORLD, &status);
		printf("This is process %i from %i - I have the token now and its value is %d\n", myid, numprocsm,token);
		token++;
		MPI_Send(&token, 1, MPI_INT, (myid + 1) % numprocs, DEFAULT_TAG, MPI_COMM_WORLD);
	}

	MPI_Finalize();

	return 0;
}


