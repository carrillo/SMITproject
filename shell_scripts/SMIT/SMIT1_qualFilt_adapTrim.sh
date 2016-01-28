#! /bin/sh

if [ $# -ne 1 ]
then
	echo "\nThis script manipulates FastQ files for quality filtering & cutadapt adaptor trimming for paired-end SMIT-data."
	echo "Please generate a text-File in advance named 'filesFastq.txt' containing all filenames of interest by ls * > filesFastq.txt. \n And provide as input read length"
	exit  
fi

files=$(cat filesFastq.txt)
# define maximum read length: actual read length - adaptor length
readLength=$(expr $1 - 21)
echo "maximum Length: " $readLength

for filepath in $files
	do
		#filename - fastq extension
		fields=$(echo $filepath | sed 's/\//	/g' | awk '{print NF}')
				
		if [ $fields -eq 1 ]
			then
				filename=$(echo $filepath | cut -f 1 -d "." )
			else
				filename=$(echo $filepath | awk ' BEGIN {FS="/"}; {print $NF}' | cut -f 1 -d "." )
			fi
				
		echo "quality filter file: " $filename
		fastq_quality_filter -Q 33 -q 20 -p 90 -i "$filepath" -o "$filename"_filt.fastq
		
		if [ $(echo $filename | grep "R1" | wc -l | sed 's/ //g') -eq '1' ]
		then
			echo 'R1 .. proceed with cutadapt'
			~/programs/cutadapt-1.8/bin/cutadapt -g CATTGATGGTGCCTACAG -a AGATCGGAAGAGCACACGTCTGAACTCCAGTCACGACCTCATCTCGTATGCCGTCTTCTGCTTG -n 2 -O 18 -m 23 -e 0.11 --match-read-wildcards --discard-untrimmed -o "$filename"_filt_ca.fastq "$filename"_filt.fastq
		
		else
			echo 'R2 .. proceed with cutadapt'
			~/programs/cutadapt-1.8/bin/cutadapt -a CTGTAGGCACCATCAATG -a AGATCGGAAGAGCGTCGTGTAGGGAAAGAGTGTAGATCTCGGTGGTCGCCGTATCATT -n 2 -m 28 -M $1 -e 0.11 --match-read-wildcards -o "$filename"_filt_ca.fastq "$filename"_filt.fastq
			
			# first step of splicing events: isolate reads with 3'ends at 5'SS
			~/programs/cutadapt-1.8/bin/cutadapt -a CTGTAGGCACCATCAATG -n 2 -O 8 -M $1 -e 0.11 --discard-untrimmed  --match-read-wildcards -o "$filename"_filt_ca_1stStep.fastq "$filename"_filt.fastq
		
		fi
	done