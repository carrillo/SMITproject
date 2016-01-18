#!/bin/sh

if [ $# -ne 1 ]
then
	echo "\nThis script removes duplicated sequences produced by SMIT PCRs."
	echo "Please generate a text-File in advance containing all R1 filenames of interest by ls *_R1.fastq .\nThe FastQ files must have the adaptor sequence removed by cutadapt already, so that the PCR barcode of 5Ns is at the 5' end of the read. \n"

	exit  
fi

for line in $(cat $1)
	do
		fields=$(echo $line | sed 's/\//	/g' | awk '{print NF}')

		if [ $fields -eq 1 ]
					then
						OUTPUTname=$(echo $line | cut -f 1 -d ".")
					else
						OUTPUTname=$(echo $line | awk ' BEGIN {FS="/"}; {print $NF}' | cut -f 1 -d "." )
						OUTPUTname3=$(echo $line | cut -f 1-$(expr $fields) -d "/" | sed 's/_R1.*//')
						
					fi
				
		OUTPUTname2=$(echo $OUTPUTname | sed 's/_R1/_R2/')
		echo "name R1: "$OUTPUTname" name R2: "$OUTPUTname2" name folder: "$OUTPUTname3
		
		perl ~/programs/prinseq-lite-0.20.4/prinseq-lite.pl -fastq "$OUTPUTname3"/"$OUTPUTname".fastq -fastq2 "$OUTPUTname3"/"$OUTPUTname2".fastq -out_format 3 -qual_noscale -ns_max_p 1 -derep 1

	done
