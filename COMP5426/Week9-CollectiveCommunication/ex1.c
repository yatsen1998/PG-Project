#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>

#define MAX_FILENAME		10

const char* baseFilename = "infile";

FILE * idfopen(int myid);

int main (int argc , char *argv[])
{
	int current, count, myid, numProcs, numElements, lSum, gSum;
	FILE *inFile;

	MPI_Init(&argc, &argv);
	MPI_Comm_size(MPI_COMM_WORLD, &numProcs);
	MPI_Comm_rank(MPI_COMM_WORLD, &myid);

	lSum = gSum = 0;
	if (myid == 0)
	{
		printf("Please enter the number of elements to read in\n");

		//read from the standard input an int to numElements
	 	...

	}

	// process 0 bradcasts numElements to all other processes
	...

	// all processes open and read a number of numElements integers from 
	// file infileI where I is equal to process's myid and then
	// do the local sum which is stored in lSum.
	...


	// global sum - store it in gSum and print it on the screen by process 0
	...


	fclose(inFile);

	MPI_Finalize();

	return 0;
}


FILE * idfopen(int myid)
{
	FILE *FP;
	char *filename = (char *)malloc(MAX_FILENAME * sizeof(char));

	sprintf(filename, "%s%d", baseFilename, myid);
	FP = fopen(filename,"r");
	free(filename);

	return FP;
}
