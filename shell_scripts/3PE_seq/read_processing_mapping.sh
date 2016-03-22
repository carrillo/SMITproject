#! /bin/sh

~/programs/cutadapt-1.8/bin/cutadapt -g GATTGATGGTGCCTACAG -n 1 -e 0.1 -m 15 -M 69 -o SC_R1.fastq -p SC_R2.fastq Sc_2b_Lig_014_AGTTCC_L002_R1.fastq Sc_2b_Lig_014_AGTTCC_L002_R2.fastq

~/bin/fastx_trimmer -Q 33 -f 6 -i SC_R1.fastq -o SC_R1_tr.fastq

R1="SC_R1_tr.fastq"
R2="SC_R2.fastq"

sh ~/scripts/tophat2_Scer3_PE.sh $R1 $R2
