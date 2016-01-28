#! /bin/sh

if [ $# -ne 1 ]
then
	echo "\nThis script manipulates FastQ files after quality and duplicate filtering."
	echo "Please generate a text-File in advance containing all filenames (R1 and R2) of interest by ls * ."
	echo "Input to this script should be the filename text-file and initial read length, e.g. 76.\n"
	exit  
fi

files=$1
readLength=$2
echo "read length: " $readLength


for filepath in $(cat $files | grep "_R1")
	do
		fields=$(echo $filepath | sed 's/\//	/g' | awk '{print NF}')		
		if [ $fields -eq 1 ]
			then
				filename=$(echo $filepath | cut -f 1 -d "." )
			else
				filename=$(echo $filepath | awk ' BEGIN {FS="/"}; {print $NF}' | cut -f 1 -d "." )
			fi
		echo "trim file: " $filename
		
		fastx_trimmer -Q 33 -f 6 -i "$filepath" -o "$filename"_trimmed.fastq

	done

for filepath in $(cat $files | grep "_R2")
	do
		fields=$(echo $filepath | sed 's/\//	/g' | awk '{print NF}')		
		if [ $fields -eq 1 ]
			then
				filename=$(echo $filepath | cut -f 1 -d "." )
			else
				filename=$(echo $filepath | awk ' BEGIN {FS="/"}; {print $NF}' | cut -f 1 -d "." )
			fi
		echo "trim file: " $filename

		# identify cutadapt trimmed and untrimmed reads, paste 4 lines (= one fastq entry) separated by tab delimiter
		# keep only lines of read length (shorter than read length)
		# change tab delimiter to line break with sed
		cat $filepath | paste - - - - | awk -v rel="$readLength" ' FS="\t" { if (length($2)==rel) print $0 } ' | sed 's/	/\
/g' > uncut.fastq

		cat $filepath | paste - - - - | awk -v rel="$readLength" ' FS="\t" { if (length($2)<rel) print $0 } ' | sed 's/	/\
/g' > cut.fastq

		fastx_trimmer -Q 33 -t 5 -i cut.fastq -o cut_trimmed.fastq
		cat uncut.fastq cut_trimmed.fastq > "$filename"_trimmed.fastq
		rm uncut.fastq cut_trimmed.fastq cut.fastq
	done
