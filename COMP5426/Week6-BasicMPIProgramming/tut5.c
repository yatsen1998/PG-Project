#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

int main(int argc , char **argv) {
    int myid, numprocs;
    MPI_Status status;
    int buf[4] = {0};

    MPI_Init(&argc,&argv); /* Used to send the command line argumenys to all procs */
    MPI_Comm_size(MPI_COMM_WORLD,&numprocs); /* Initializes the number of procs in the group specified by mpirun */
    MPI_Comm_rank(MPI_COMM_WORLD,&myid);	 /* Initialize the rank of this process in the group */

    buf[0] = buf[3] = -1; /* ghost cell */
    buf[1] = myid;
    buf[2] = myid + numprocs;

    int rightRank = (myid + 1) % numprocs;
    int leftRank = (myid + numprocs - 1) % numprocs;

    MPI_Sendrecv(&(buf[2]), 1, MPI_INT, rightRank, 0,
                 &(buf[0]), 1, MPI_INT, leftRank, 0, MPI_COMM_WORLD, &status);

    MPI_Sendrecv(&(buf[1]), 1, MPI_INT, leftRank, 1,
                 &(buf[3]), 1, MPI_INT, rightRank, 1, MPI_COMM_WORLD, &status);

    printf("process %i:  %i, %i, %i, %i.\n", myid, buf[0], buf[1], buf[2], buf[3]);

    MPI_Finalize();

    return 0;
}