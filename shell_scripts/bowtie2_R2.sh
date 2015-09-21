#!/bin/sh

# this script maps R2 reads to Scer3 genome and distinguishes between spliced and unspliced read

if [ $# -ne 1 ]
then
	echo " provide: ls <R2>.fastq > in_R2 "
	exit  
fi

# comment1

input=$1
#index_EIJ="/Volumes/DiskMeen/SMIT_clean/mapping/bowtieIndices/Bowtie2Indices/Scer3_110_EIJ"
#index_EEJ="/Volumes/DiskMeen/SMIT_clean/mapping/bowtieIndices/Bowtie2Indices/Scer3_110_EEJ"

index_EIJ="~/Documents/LAB_STUFF/sequencing_array/SMIT_Mac/mapping/bowtieIndices/Bowtie2Indices/Scer3_110_EIJ"
index_EEJ="~/Documents/LAB_STUFF/sequencing_array/SMIT_Mac/mapping/bowtieIndices/Bowtie2Indices/Scer3_110_EEJ"


for R2 in $(cat $input)
	do
		
		output=$(echo $R2 | awk ' BEGIN { FS="/" }; { print $NF }' | cut -f 1-3 -d "_")
		
		echo $output
		echo "mapping R2 unspliced (EIJ)"
		bowtie2 -L 10 --end-to-end -N 0 -k 1 --norc -x $index_EIJ $R2 -S EIJ.sam

		echo "mapping R2 spliced (EEJ)"
		bowtie2 -L 10 --end-to-end -N 0 -k 1 --norc -x $index_EEJ $R2 -S EEJ.sam

		cat EIJ.sam | awk '{ if ($3!="*") { print $0 } }' > tmp.sam
		mv tmp.sam EIJ.sam
		samtools view -S -b EIJ.sam > "$output"_EIJ.bam

		cat EEJ.sam | awk '{ if ($3!="*") { print $0 } }' > tmp.sam
		mv tmp.sam EEJ.sam
		samtools view -S -b EEJ.sam > "$output"_EEJ.bam

	done
