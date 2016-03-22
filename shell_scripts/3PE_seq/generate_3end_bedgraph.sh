# generate bedgraph of 3' end data 3'PE-Seq

samtools view -f 0x40 -b accepted_hits.bam > Pair_1.bam
bamToBed -i Pair_1.bam -bed12 > Pair_1.bed

cat Pair_1.bed  | awk ' {if ($6=="+") print $1"\t"$2"\t"$2+1"\t"$6 ; if ($6=="-") print $1"\t"$3-1"\t"$3"\t"$6 }' > Pair_1_short.bed

cat Pair_1_short.bed | awk '$1=="chrI" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrI
cat Pair_1_short.bed | awk '$1=="chrII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrII
cat Pair_1_short.bed | awk '$1=="chrIII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrIII
cat Pair_1_short.bed | awk '$1=="chrIV" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrIV
cat Pair_1_short.bed | awk '$1=="chrIX" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrIX
cat Pair_1_short.bed | awk '$1=="chrM" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrM
cat Pair_1_short.bed | awk '$1=="chrV" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrV
cat Pair_1_short.bed | awk '$1=="chrVI" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrVI
cat Pair_1_short.bed | awk '$1=="chrVII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrVII
cat Pair_1_short.bed | awk '$1=="chrVIII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrVIII
cat Pair_1_short.bed | awk '$1=="chrX" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrX
cat Pair_1_short.bed | awk '$1=="chrXI" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXI
cat Pair_1_short.bed | awk '$1=="chrXII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXII
cat Pair_1_short.bed | awk '$1=="chrXIII" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXIII
cat Pair_1_short.bed | awk '$1=="chrXIV" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXIV
cat Pair_1_short.bed | awk '$1=="chrXV" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXV
cat Pair_1_short.bed | awk '$1=="chrXVI" {print $0 }' | sort -n | uniq -c | awk ' { print $2"\t"$3"\t"$4"\t"$1"\t"$5 }' > chrXVI

cat chr* > Pair_1.bedgraph
rm chr*
# filter by strand - -> W, + -> C
cat Pair_1.bedgraph | awk '$5=="+" {print $1"\t"$2"\t"$3"\t-"$4 }' > SC_R1_3end_C.bedgraph
cat Pair_1.bedgraph | awk '$5=="-" {print $1"\t"$2"\t"$3"\t"$4 }' > SC_R1_3end_W.bedgraph
