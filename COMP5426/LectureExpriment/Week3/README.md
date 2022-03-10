# README

Using Matrix Multiplication problem to test the following methods, See Week3 PPT P18~29:

- Original (No optimization)
- Blocked Matrix Multiplication
- Loop Unrolling

## Environment & Test sample

1. Environment: MacOS 12.2.1 - VScode - Intel i5/4Core/2GHz- 16GB Mem

2. Sample: using fixed-size matrix (1024 * 1024) to multiply.

## Results

| Methods                                | Elapsed Time/s |
| -------------------------------------- | -------------- |
| Original                               | 10.23          |
| Blocked (BlockSize as 128 * 128)       | 5.04           |
| Loop Unrolling (Unrolling Factor as 4) | 6.93           |

BlockSize should be determined by Fast Memory Size.

The unrolling factor should be decided carefully: as the unproper unrolling may introduce **Data Dependency**.

## Tips

We need to consider that compiler may do some optimization automatically. 

Using Command to compile:`gcc -Wall -pedantic random.c -o random`

