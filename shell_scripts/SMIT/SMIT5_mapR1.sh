#!/bin/sh

if [ $# -ne 1 ]
then
	echo "\nThis script maps SMIT 3' reads (R1) to the genome."
	echo "Index needs to be build of Scer3.fasta with bowtie2-build Scer3.fasta Scer3 .."
	echo "Please generate a text-File in advance containing all filenames of interest by ls * > in_R1."
	echo "provide: <in_R1> samName \n"
	exit  
fi

while read line
	do
		
		fields=$(echo $line | sed 's/\//	/g' | awk '{print NF}')				
		if [ $fields -eq 1 ]
			then
				output=$(echo $line | cut -f 1 -d "." )
			else
				output=$(echo $line | awk ' BEGIN {FS="/"}; {print $NF}' | sed 's/_R1*//' )
			fi
		echo $output
		
		index="Saccharomyces_cerevisiae_UCSC_sacCer3/Saccharomyces_cerevisiae/UCSC/sacCer3/Sequence/Bowtie2Index/genome"
		# gtf file contains gene and intron annotation according to Nagalakshmi et al. 2008 & Apr 2011 Scer3 annotation
		gtfFile="Scer3_snyder_all_final.gff"
		~/programs/tophat-2.0.13.OSX_x86_64/tophat2 -p 5 -N 1 -m 0 --segment-mismatches 0 -i 30 -I 1010 -g 1 --segment-length 20 --no-coverage-search --library-type fr-firststrand --min-anchor-length 8 --min-coverage-intron 30 --max-coverage-intron 1010 --min-segment-intron 30 --max-segment-intron 1010 -o "$output" $index $line
		
		cp "$output"/accepted_hits.bam "$output".bam
		samtools index "$output".bam
		
	done < $1