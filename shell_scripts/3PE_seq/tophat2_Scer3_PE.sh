#Tophat usage

if [ $# -ne 2 ]
	then
		echo "This script performs a tophat analysis with the settings appropriate for S. cerevisiae."
	fi

input1=$1
input2=$2
mateDist=200

gtfFile="~/annotations/Saccharomyces_cerevisiae_UCSC_sacCer3/Saccharomyces_cerevisiae/UCSC/sacCer3/Annotation/Genes/sacCer3_exons_final.gff"
indexFile="~/annotations/Saccharomyces_cerevisiae_UCSC_sacCer3/Saccharomyces_cerevisiae/UCSC/sacCer3/Sequence/Bowtie2Index/genome"
		
ls $gtfFile

~/programs/tophat-2.0.12.Linux_x86_64/tophat2 -p 5 -i 30 -I 1010 -g 1 -G $gtfFile -r $mateDist --mate-std-dev 50 --segment-length 25 --library-type fr-firststrand --min-anchor-length 3 --splice-mismatches 0 --min-coverage-intron 30 --max-coverage-intron 1010 --min-segment-intron 30 --max-segment-intron 1010 -o SC_newGTF_"$mateDist" $indexFile $input1 $input2
