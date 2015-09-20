library(ggplot2); library(Hmisc)

# Read observed insert lengths 
path <- "data/wt/"
setwd(path)
data <- data.frame(insert_length=read.table(gzfile("eij.length.gz"), sep='\t', head=F)[,8])
data$bin <- cut(data$insert_length, breaks=seq(from=0, to=max(data$insert_length), by=10), labels=seq(from=5, to=max(data$insert_length)-5, by=10 ) )

############################
# Plotting
############################
# Observed pdf 
plot.hist <- ggplot( data, aes( insert_length ) ) + theme_bw() # Initiate a ggplot object with our data and axis labelling 
p <- plot.hist + geom_histogram( colour = "darkgreen", fill = "white", binwidth = 20 ) # Add histogramm 
ggsave(plot=p, filename="insertLengthDistribution.pdf", width=7, height=7, useDingbats=F)
show(p)

# Observed cdf 
plot(ecdf(data$insert_length), cex=1, main="cdf",xlab="insert length" ) 

##################
# Estimation of exponential decay rate. 
##################
count_per_pos <- data.frame( table( data$insert_length ) ) #Get counts over each position 
count_per_pos <- data.frame( table( data$insert_length ) ) #Get counts over each position 
count_per_pos$Var1 <- as.numeric(as.character(count_per_pos$Var1))

# Set boundaries 
xLow <- 50
xHigh <- .Machine$double.xmax

plot( count_per_pos$Var1, log(count_per_pos$Freq), cex=0.1, xlim=c(0,1000), ylab="counts (Log e)",xlab="insert size (nt)" ) # Log-linear plot
arrows( xLow,0,xLow,40, col='red' )
arrows( xHigh,0,xHigh,40, col='red' )

# Extract data within boundaries
x <- count_per_pos[count_per_pos[,1]>xLow & count_per_pos[,1]<=xHigh,1]
y <- count_per_pos[count_per_pos[,1]>xLow & count_per_pos[,1]<=xHigh,2]
fit <- lm( log(y) ~x )
abline( fit, col='blue')
write.table(data.frame(cutoff=xLow, rate=-fit$coefficients[2]), "insertLengthBias.txt", quote=F, sep="\t", row.names=F)


# Simulate exponential distribution and truncate both model data and observed data  
k <- -fit$coefficients[ 2 ]
model <- rexp(10000,k)
model <- model[ model > xLow & model <=xHigh ]
observed <- data[ data[,1] > xLow & data[,1] <=xHigh ,1]

# Plot as cumulative distributions
png(filename="cumulative_distribution_data_fit.png")
g <- c( rep("data",length( observed )), rep("model",length( model )))
Ecdf(c(observed , model), group=g, col=c('black', 'red'),main="exponential",xlab=c("insert size (nt)"))
dev.off()