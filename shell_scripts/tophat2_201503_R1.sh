#!/bin/sh

#Bowtie2 usage

if [ $# -ne 1 ]
then
	echo "\nThis script maps SMIT 3' reads to junction sequences."
	echo "Index needs to be build of Scer3.fasta with bowtie2-build Scer3.fasta Scer3 .."
	echo "Please generate a text-File in advance containing all filenames of interest by ls * > inFastq."
	echo "provide: <inFastq> samName \n"
	exit  
fi

input=$(cat $1)

while read line
	do
		
		output=$(echo $line | awk ' BEGIN { FS="/" }; { print $NF }' | cut -f 1-3 -d "_")
		
		echo $output
		
		index="/Users/herzel/Documents/LAB_STUFF/annotations/Sc_annotations/Saccharomyces_cerevisiae_UCSC_sacCer3/Saccharomyces_cerevisiae/UCSC/sacCer3/Sequence/Bowtie2Index/genome"
		gtfFile="/Users/herzel/Documents/LAB_STUFF/annotations/Sc_annotations/Scer3/Scer3_snyder_all_final.gff"
		~/programs/tophat-2.0.13.OSX_x86_64/tophat2 -p 5 -N 1 -m 0 --segment-mismatches 0 -i 30 -I 1010 -g 1 --segment-length 20 --no-coverage-search --library-type fr-firststrand --min-anchor-length 8 --min-coverage-intron 30 --max-coverage-intron 1010 --min-segment-intron 30 --max-segment-intron 1010 -o "$output" $index $line
# --b2-mp 20,10 --b2-gbar 50  --read-gap-length 0 --read-edit-dist 0 
		
		cp "$output"/accepted_hits.bam "$output".bam
		samtools index "$output".bam
		
	done < $1