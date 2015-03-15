################################
# Occupancy values to feature files. 
################################

setwd("/Volumes/DiskA/SMIT/features/") 
library("plyr")


get_feature_file <- function( path, experiment ) {
  a <- read.table(paste( c(path,experiment), collapse="" ),sep="\t",head=F, colClasses=c("character","character") )
  experiment <- gsub(experiment, pattern=".txt", replacement="")
  
  b <- ldply( lapply(strsplit(a$V1, split="_"), FUN=function(x) { data.frame(id=unlist(x)[1], feature=unlist(x)[2]) } ) ) 
  b <- cbind(b, ldply( lapply( strsplit(a$V2, ","), FUN=function(x) { data.frame(mean=mean(as.numeric(unlist(x)))) }) ) )
  
  features <- ddply(b, .(id), .fun=function(x) {
    df <- data.frame(id=x[[1,1]], fiveSS=x[x$feature == "5SS",3],
                     threeSS=x[x$feature == "3SS",3], polyASite=x[x$feature == "PolyASite",3]) 
  } )
  
  features$id <- as.character(features$id)
  
  names(features) <- c("id", paste( names(features)[2], experiment, sep="_" ), paste( names(features)[3], experiment, sep="_" ), paste( names(features)[4], experiment, sep="_" ))
  return(features)
}

get_combined_feature_file_from_path <- function(path) {
  experiments  <- list.files(path,pattern="*.txt")
  
  for( experiment in experiments ) {
    if( !exists("featureFile") ) {
      featureFile <- get_feature_file(path, experiment)
    } else {
      featureFile <- merge(featureFile, get_feature_file(path, experiment), by="id", all=T)
    }
    print(experiment)
    print(paste(c("Nr of rows: ", nrow(featureFile), " Nr of columns: ", ncol(featureFile) )))
  }
  return(featureFile)  
}

path  <- "cramerClip/sacCer3/pileups/"
cramer_features <- get_combined_feature_file_from_path(path)
write.table( cramer_features, "cramerClip/sacCer3/cramerClip_all.featureFile", quote=F, row.names=F, sep="," )

path  <- "histoneModification/sacCer3/pileups/"
histone_mod <- get_combined_feature_file_from_path(path)
write.table( histone_mod, "histoneModification/sacCer3/histoneModifications_all.featureFile", quote=F, row.names=F, sep="," )

path  <- "nucleosomePosition/MNase/sacCer3/pileups/"
nucleosome_pos <- get_combined_feature_file_from_path(path)
write.table( nucleosome_pos, "nucleosomePosition/MNase/sacCer3/nucelesome_pos_all.featureFile", quote=F, row.names=F, sep="," )
