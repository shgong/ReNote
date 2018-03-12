# Computer Architecture

## 1. Fundamentals of Quantitative Design and Analysis
### ISA: Instruction set architecture

1. class of ISA: Nearly all ISAs today are classified as general-purpose register architectures, where the operands are either registers or memory locations. The 80x86 has 16 general-purpose registers and 16 that can hold floating-point data, while MIPS has 32 general-purpose and 32 floating-point registers

2. Memory addressing: Virtually all desktop and server computers, including the 80x86, ARM, and MIPS, use byte addressing to access memory operands.

3. Addressing modes
    - MIPS addressing modes are Register, Immediate (for constants), and Displacement, where a constant offset is added to a register to form the memory address. 
    - The 80x86 supports those three plus three variations of displacement: no register (absolute), two registers (based indexed with displacement), and two registers where one register is multiplied by the size of the operand in bytes (based with scaled index and displacement). It has more like the last three, minus the displacement field, plus register indirect, indexed, and based with scaled index. 
    - ARM has the three MIPS addressing modes plus PC-relative addressing, the sum of two registers, and the sum of two registers where one register is multiplied by the size of the operand in bytes. It also has autoincrement and autodecrement addressing, where the calculated address replaces the contents of one of the registers used in forming the address.

4. Types and size of operands: 8,16,32,64 bit
5. Operations
    - data transfers
        + LB,LBU,SB
        + LH,LHU,SH
        + LW,LWU,SW
        + LD,SD
        + MFCO,MTCO
        + MOV.S, MOV.D
        + MFC1, MTC1
    - arithmetic/logical
        + DADD, DADDI, DADDU, DADDIU
        + DSUB, DSUBU
        + DMUL, DMULU, DDIV, DDIVU, MADD
        + AND, ANDI
        + OR, ORI, XORI, XOR
        + LUI
        + DSLL, DSRL, DSRA, DSLLV, DSRLV, DSRAV
        + SLT, SLTI, SLTU, SLTIU
    - control
        + BEQZ, BNEZ
        + BEQ, BNE
        + BCIT, BCIF
        + MOVN, MOVZ
        + J, JR
        + JAL, JALR
        + TRAP
        + ERET
    - floating point
        + ADD.D, ADD.S, ADD.PS
        + SUB.D, SUB.S, SUB.PS
        + MUL.D, MUL.S, MUL.PS
        + MADD.D, MADD.S, MADD.PS
        + DIV.D, DIV.S, DIV.PS
6. Control flow instructions
    - conditional branches, 
    - unconditional jumps, 
    - procedure calls
    - returns
7. Encoding an ISA
    - Basic instruction formats
        + R
            * integer register-to-register operation
            * DADDU, DSUBU 
            * opcode(6) rs(5) rt(5) rd(5) shamt(5) funct(6)
        + I
            * data transfers, branches and immediates
            * LD, SD, BEQZ, DADDI
            * opcode(6) rs(5) rt(5) immediate(16) 
        + J 
            * Jumps
            * opcode(6) address(26)
    - floating point instruction formats
        + FR: opcode(6) fmt(5) ft(5) fs(5) fd(5) funct(6)
        + FI: opcode(6) fmt(5) ft(5) immediate(16) 


## 2. Memory Hierarchy Design

- Memory example
    + Typical server
        * CPU Registers - 1000B - 300ps
        * L1 Cache - 64KB - 1ns
        * L2 Cache - 256KB - 3-10ns
        * L3 Cache - 2-4MB - 10-20ns
        * Memory - 4-16GB - 50-100ns
        * Disk - 4-16TB - 5-10ms
    + Typical mobile device 
        * CPU Registers - 500B - 500ps
        * L1 Cache - 64KB - 2ns
        * L2 Cache - 256KB - 10-20ns
        * Memory - 512MB - 50-100ns
        * Flash Memory - 4-8GBB - 25-50us
- Main focus
    + traditionally: optimizing average memory access time
        * cache access time
        * miss rate
        * miss penalty
    + recently: power
        * caches can account for 25% - 50% total power consumption
        
### Memory Basics

+ Key design decision: where blocks can be placed in cache
+ set associative
    * set is group of blocks in cache
    * choose set by address of data, search set to find block
+ cache reads is easy
+ cache writes
    * how can copy in cache and memory kept consistent
    * two main strategies
        - write-through cache 
            + updates item in the cache 
            + writes through to update main memory
        - write-back cache
            + only updates copy in the cache
        - use write buffer to allow cache to proceed asap
+ cache miss 3C model
    * Compulsory - very first access cannot be in the cache, always occur
    * Capacity - cannot contain all blocks needed, occur for discarded
    * Conflict - if block placement strategy is not fully associative, occur because multiple blocks map to its set 

Average memory access time = Hit_L1 + MissRate_L1 * ( Hit_L2 + MissRate_L2 * MissPenalty_L2 )

### Ten Advanced Optimization

1. Small and simple first-level cache
    - use low-level associativity
    - reduce hit time and power
2. Way Prediction
    - extra bits kept in cahce to predict the way
        + add predictor bits to each block of cache
        + if not hit, change way predictor, latency of one clock cycle
    - reduce conflict misses and maintain hit speed
3.  Pipeline cache access
    - fast clock cycle time and high bandwidth, but slower hit
    - cache access for Pentium took 1 cc,  but for Core i7 take 4 cc
    - easier to incorporate high degrees of associativity
4. Nonblocking caches
    - processor need not stall on a cache miss
    - reduce effective miss penalty ( sum of misses => non-overlapped time that processor stalled)
5. Multibanked caches
    - divide into independent banks instead of single monolithic block
        + banks: originally improve performance of main memory, now inside DRAM/cache as well
    - support simultaneous accesses, increase cache bandwidth
6. Critical word first & Early Restart
    - processor normally need one word of block at a time
    - impatience, don't wait full block to be loaded
    - only benefits design with large cache blocks
7. Merging Write Buffer
    - if buffer have other blocks, check address, if match, new data merged with prev buffer entry
    - if write buffer full & no address match, cache must wait until buffer has empty entry
    - uses the memory more efficiently since multiword writes are usually faster 
8. Compiler Opotimization
    - optimize software: the hardware designers' favorite solution
    - Loop Interchange
        + swap nested loops `for j: for i: access x[i][j]` 
        + swap i,j to reflect data in sequential order in memory
    - Blocking
        + improve temporal locality to reduce misses
        + operate on submatrices or blocks, to maximize access to the data loaded into cache
9. Hardware Prefetching of instruction and data
    - fetches two blocks on a miss: requested and next block
    - prefetched placed into instruction stream buffer, cancel next request if match
10. Compiler-controlled prefetching

### Memory Technologies

- SRAM
- DRAM
- Flash Memory

### Protection: VMemory and VMachines

## 3. Instruction Level Parallism

- exploit parallism among instructions
- available with a basic block: a straight-line code sequence with no brnaches
- whenever overlap operation is possible

```
for(i=0;i<999;i=i+1) x[i] = x[i] + y[i]
```

### Basic Compiler Techniques for Exposing ILP

### Reducing Branch Costs with Advanced Branch Prediction

## 4. Data-Level Parallelism in Vector SIMD and GPU Architectures

- SIMD: Single instruction, multiplpe data architecture
- three variations
    + vector architectures
    + multimedia SIMD
    + graphics processing units (GPU)

    