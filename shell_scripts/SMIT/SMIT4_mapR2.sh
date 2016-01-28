#!/bin/sh

# this script maps R2 reads to Scer3 genome and distinguishes between spliced and unspliced read

if [ $# -ne 1 ]
then
	echo "This script maps R2 reads to the S. cerevisiae genome (Scer3/ Apr 2011) and distinguishes between spliced and unspliced reads."
	echo "Please provide a list of R2 read files: ls <R2>.fastq > in_R2 ."
	exit  
fi

input=$1

index_EIJ="bowtieIndices/Bowtie2Indices/Scer3_110_EIJ"
index_EEJ="bowtieIndices/Bowtie2Indices/Scer3_110_EEJ"


for R2 in $(cat $input)
	do
		
		fields=$(echo $R2 | sed 's/\//	/g' | awk '{print NF}')				
		if [ $fields -eq 1 ]
			then
				output=$(echo $R2 | cut -f 1 -d "." )
			else
				output=$(echo $R2 | awk ' BEGIN {FS="/"}; {print $NF}' | sed 's/_R2*//' )
			fi
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
