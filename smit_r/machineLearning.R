##############################
# Learn important features for 
# i) position of co-transcriptional splicing (kinetics)
# ii) saturation values of co-transcriptional splicing 
##############################

library("caret"); library("plyr"); library("reshape2")

load_data <- function( fileList ) {
  print("Loading")
  read_input <- function(file) {
    df  <- read.table(file,head=T,sep=",")
    return(df[!duplicated(df$id),])
  }
  
  for( i in fileList ) {
    if( !exists("XY") ) {
      XY <- read_input(i)
    } else { XY <- merge(XY, read_input(i), by="id", all=T)  }
  }
  
  # Remove values with no 
  
  print("Loading. Done.")
  return(XY)
}

#######################
# Clean data 
# 1. Move id to rows. 
# 2. Drop categorical values. 
# 3. Drop unused target values. 
#######################
clean_XY <- function(XY, target_id) {
  print("Cleaning.")
  row.names(XY) <- XY$id
  XY$id <- NULL 
  XY$chrId <- NULL
  XY$strand <- as.numeric(as.character((revalue(XY$strand, replace=c("+"="1","-"="-1", "?"=NA) ))))
  
  XY$X5SS <- NULL
  XY$X3SS <- NULL
  XY$BPS <- NULL
  
  target_ids <- names(XY)[ grep(pattern="target", names(XY)) ]
  remove_ids  <- target_ids[!target_ids %in% target_id]
  XY <- XY[,-which(names(XY) %in% remove_ids)]
  
  print("Cleaning. Done.")
  return(XY)
}


#########################
# Preprosses data for learninn
# 1. remove unwanted rows where target is NA and unwanted target values. 
# (2. remove features with near-zero-variance -> 3'SS rank)
# 3. remove highly correlated features (Pos, Major5SS, Major3SS) 
# (4. remove linear dependent features (MajorIntronLength, major5SS, rel5SSDistance, rel3SSDistance) )
# 5. Split data into training and holdout set (80, 20)
#########################
preprocess_XY <- function(XY, removeNearZeroVariance=F, removeCorrelated=T, removeColinear=F, logTarget=F ) {
  
  XY <- XY[!is.na(XY[[target_id]]),]
  XY <- na.omit(XY)
  
  if( logTarget ) {
    XY[[target_id]] <- log(XY[[target_id]])
  } 
  
  XY[[target_id]]
  
  # keep only features with non nearZeroVariance (nearZeroVar(mdrrDescr, saveMetrics= TRUE))
  if( removeNearZeroVariance ) {
    nzv <- nearZeroVar(XY) 
    if( length(nzv) != 0 ) { XY <- XY[,-nzv(XY)] }  
  }
  
  # identify highly correlated predictors. 
  if( removeCorrelated ) {
    XY_cor <- cor(XY)
    XY_cor_melt <- melt(XY_cor)
    XY_cor_melt[order(abs(XY_cor_melt$value), decreasing=T),]
    highlyCorDescr <- findCorrelation(XY_cor, cutoff = .75)
    if( length(highlyCorDescr) != 0 ) {XY <- XY[,-highlyCorDescr]}  
  }
  
  # Find linear combinations of the data 
  if( removeColinear ) {
    comboInfo <- findLinearCombos(XY[2:ncol(XY)])
    if( !is.null(comboInfo$remove) ) { XY <- XY[, -comboInfo$remove]  }
  }
  print("Preprocessing. Done.")
  return(XY)
}

# Split data into training and hold-out set
split_XY <- function(XY, target_id, split=0.8) {
  set.seed(13)
  trainIndex <- createDataPartition(XY[[target_id]], p = split, list = FALSE, times = 1 )
  XY_train <- XY[trainIndex,]
  y_train <- data.frame(y=XY_train[[target_id]], row.names=row.names(XY_train))
  X_train <- XY_train[,-which(names(XY_train) == target_id)]
  
  XY_holdout <- XY[-trainIndex,]
  y_holdout <- data.frame(y=XY_holdout[[target_id]], row.names=row.names(XY_holdout))
  X_holdout <- XY_holdout[,-which(names(XY_holdout) == target_id)]
  
  return( list("X_train"=X_train, "y_train"=y_train, "X_holdout"=X_holdout, "y_holdout"=y_holdout) ) 
}

get_data <- function( fileList, target_id ) {
  XY <- load_data( fileList )
  XY <- clean_XY(XY, target_id=target_id)
  XY <- preprocess_XY(XY, removeNearZeroVariance=F, removeCorrelated=T, removeColinear=F, logTarget=logTarget ) 
  split_data <- split_XY(XY, target_id, split=0.8 )
  return(split_data)
}


fileList  <- list(
  "genetic"="/Volumes/DiskA/SMIT/features/geneticFeatures/sacCer3/geneticFeatures.featureFile",
  "clip"="/Volumes/DiskA/SMIT/features/cramerClip/sacCer3/cramerClip_all.featureFile",
  "histoneMod"="/Volumes/DiskA/SMIT/features/histoneModification/sacCer3/histoneModifications_all.featureFile",
  "MNase"="/Volumes/DiskA/SMIT/features/nucleosomePosition/MNase/sacCer3/nucelesome_pos_all.featureFile",
  "targets"="~/Projects/SMIT/figures/fig2/data/target_values.csv"
)

target_id <- "target_saturation"
split_data <- get_data(fileList, target_id )

##############################
# Test models 
# 1. Set training parameters. 
# 2. Train different models. 
##############################

cvCtrl <- trainControl(method="repeatedcv", number=5, repeats = 3 )
metric  <-  "RMSE"

# Preprocess: problem with BoxCox: Fails when values are 0. 
# 1. Try YeoJohnson
#preProc  <-  c("BoxCox","center", "scale")
preProc  <-  c("YeoJohnson","center", "scale")



x  <-  split_data[["X_train"]]
y <- split_data[["y_train"]]$y
##########################
# Interpreatable models
# Linear models. 
# 1. GLMNET
# 2. Lasso-regression 
# 3. Ridge-regression 
##########################

########################## GLMNET
set.seed(13)
glmnet_model <- train(x = x, y = y, 
                      method='glmnet', preProc = preProc, tuneLength=10,
                      metric = metric, trControl = cvCtrl)
plot(glmnet_model, scales=list(x=list(log=2)))
LogLambdaMin <- log(glmnet_model$finalModel$lambdaOpt)
plot(glmnet_model$finalModel, "lambda", label=T, sub=LogLambdaMin)

regression_coef <- coef(glmnet_model$finalModel, s=glmnet_model$bestTune$lambda)
c <- data.frame(coef_name=row.names(regression_coef), value=regression_coef[,1])
write.table(c, file = "~/Projects/SMIT/ML/glmnet_coeff.csv", quote=F, row.names=F, sep="\t" ) 

########################## Lasso Regression 
grid  <- expand.grid(.alpha=1, .lambda=2^(seq(-8,1,by=0.1)))
set.seed(13)
glmnet_lasso_model <- train(x = x, y = y,
                            method='glmnet', preProc = preProc, tuneGrid=grid,
                            metric = metric, trControl = cvCtrl)
plot(glmnet_lasso_model, scales=list(x=list(log=2)))
LogLambdaMin <- log(glmnet_lasso_model$finalModel$lambdaOpt)
plot(glmnet_lasso_model$finalModel, "lambda", label=T, sub=LogLambdaMin)

regression_coef <- coef(glmnet_lasso_model$finalModel, s=glmnet_lasso_model$bestTune$lambda)
c <- data.frame(coef_name=row.names(regression_coef), value=regression_coef[,1])
write.table(c, file = "~/Projects/SMIT/ML/lasso_coeff.csv", quote=F, row.names=F, sep="\t" ) 


checkNormalization <- FALSE 
if( checkNormalization ) {  
  grid  <- expand.grid(.alpha=1, .lambda=2^(seq(-8,1,by=0.1)))
  set.seed(13)
  glmnet_lasso_model_BoxCox <- train(x = x, y = y, method='glmnet', preProc = c("BoxCox","center", "scale"), tuneGrid=grid, metric = metric, trControl = cvCtrl)
  set.seed(13)
  glmnet_lasso_model_noTransform <- train(x = x, y = y, method='glmnet', preProc = c("center", "scale"), tuneGrid=grid, metric = metric, trControl = cvCtrl)
  set.seed(13)
  glmnet_lasso_model_YeoJohnson <- train(x = x, y = y, method='glmnet', preProc = c("YeoJohnson","center", "scale"), tuneGrid=grid, metric = metric, trControl = cvCtrl)
  
  set.seed(13)
  glmnet_BoxCox <- train(x = x, y = y, method='glmnet', preProc = c("BoxCox","center", "scale"), tuneLength=10, metric = metric, trControl = cvCtrl)
  set.seed(13)
  glmnet_noTransform <- train(x = x, y = y, method='glmnet', preProc = c("center", "scale"), tuneLength=10, metric = metric, trControl = cvCtrl)
  set.seed(13)
  glmnet_YeoJohnson <- train(x = x, y = y, method='glmnet', preProc = c("YeoJohnson","center", "scale"), tuneLength=10, metric = metric, trControl = cvCtrl)
  
  models  <- list(glmnet_boxCox=glmnet_BoxCox, lasso_boxCox=glmnet_lasso_model_BoxCox, 
                  glmnet_noTransform=glmnet_noTransform, lasso_noTransform=glmnet_lasso_model_noTransform, 
                  glmnet_YeoJohnson=glmnet_YeoJohnson, lasso_YeoJohnson=glmnet_lasso_model_YeoJohnson )
  cvValues <- resamples(models)
  bwplot(cvValues, metric = metric)
}

grid <- expand.grid(.fraction=2^seq(-10,0,by=0.2)) 
lasso_model  <- train(x=x, y=y, method='lasso', preProc = preProc, tuneGrid=grid, metric=metric, trControl=cvCtrl )
plot(lasso_model, scales=list(x=list(log=2)))

grid <- expand.grid(.lambda=2^seq(-6,0,by=0.5)) 
ridge_model  <- train(x=x, y=y, method='ridge', preProc = preProc, tuneGrid=grid, metric=metric, trControl=cvCtrl )
plot(ridge_model, scales=list(x=list(log=2)))

ridgeWithFeatureSelection_model  <- train(x=x, y=y, method='foba', preProc = preProc, tuneLength=10, metric=metric, trControl=cvCtrl )
plot(ridgeWithFeatureSelection_model, scales=list(x=list(log=2)))

#########################
# Leapforward, linear regression with forward, backward and sequenctial selection 
#########################
leap <- FALSE
if( leap ) {
  leapSeq_model  <- train(x = x, y = y, 
                          method='leapSeq', preProc = preProc, tuneLength=10,
                          metric = metric, trControl = cvCtrl)
  
  plot(leapSeq_model)
}

#######################
# Boosting methods
#######################
set.seed(13)

boosted_lm <- train(x = x,y = y, tuneLength = 10,
                       method = "bstLs", preProc = preProc,
                       metric = metric, trControl = cvCtrl)
plot(boosted_lm, metric = metric, scales = list(x = list(log=2)))  

set.seed(13)
boosted_glm <- train(x = x,y = y, tuneLength = 10,
                    method = "glmboost", preProc = preProc,
                    metric = metric, trControl = cvCtrl)
plot(boosted_glm, metric = metric, scales = list(x = list(log=2)))  

set.seed(13)
stochastic_gradient_boosting <- train(x = x,y = y, tuneLength = 10,
                     method = "gbm", preProc = preProc,
                     metric = metric, trControl = cvCtrl)
plot(stochastic_gradient_boosting, metric = metric, scales = list(x = list(log=2)))  



######################
# other
######################
set.seed(13)
cart <- train(x = x,y = y, tuneLength = 10,
              method = "rpart", preProc = preProc,
              metric = metric, trControl = cvCtrl)
plot(cart, metric = metric, scales = list(x = list(log=2)))  

icr <- train(x = x,y = y, tuneLength = 10,
            method = "icr", preProc = preProc,
            metric = metric, trControl = cvCtrl)
plot(icr, metric = metric, scales = list(x = list(log=2)))  

pcaNNet <- train(x = x,y = y, tuneLength = 10,
                method = "pcaNNet", preProc = preProc,
                metric = metric, trControl = cvCtrl)
plot(pcaNNet, metric = metric, scales = list(x = list(log=2)))  

spls <- train(x = x,y = y, tuneLength = 10,
                   method = "spls", preProc = preProc,
                   metric = metric, trControl = cvCtrl)
plot(spls, metric = metric, scales = list(x = list(log=2)))  

avNNet <- train(x = x,y = y, tuneLength = 10,
                 method = "avNNet", preProc = preProc,
                 metric = metric, trControl = cvCtrl)
plot(avNNet, metric = metric, scales = list(x = list(log=2)))  

#######################
# Comparing different models.
# 1. Collate cross-validation values for models with matched cross-validation samples (same seed)
#######################
# Collect all model evaluation in one object. 
models  <- list(glmnet=glmnet_model, lasso_glment=glmnet_lasso_model, 
                lasso = lasso_model, ridge = ridge_model, ridge_withFS = ridgeWithFeatureSelection_model,
                Boosted_lm=boosted_lm, Boosted_glm=boosted_glm, CART=cart, 
                SGB = stochastic_gradient_boosting, ICR = icr, pcaNNet=pcaNNet, spls=spls, avNNet = avNNet )
cvValues <- resamples(models)
bwplot(cvValues, metric = metric)

#######################
# Show prediction performance
#######################


plot_prediction <- function( model ) {
  y_train  <- data.frame(observed=y, predicted=predict(model, newdata=x), label="train")
  y_holdout  <- data.frame(observed=split_data[["y_holdout"]]$y, predicted=predict(model, newdata=split_data[["X_holdout"]]), label="holdout")
  
  
  p <- ggplot(rbind(y_train, y_holdout), aes(x=observed, y=predicted, colour=label))
  p <- p + geom_point()
  p <- p + theme(legend.position = c(0, 0))
  return(p)  
}

show(plot_prediction(glmnet_lasso_model))
show(plot_prediction(glmnet_model))
show(plot_prediction(lasso_model))
show(plot_prediction(ridge_model))
show(plot_prediction(ridgeWithFeatureSelection_model))

show(plot_prediction(boosted_lm))
show(plot_prediction(boosted_glm))
show(plot_prediction(stochastic_gradient_boosting))
show(plot_prediction(cart))
show(plot_prediction(icr))
show(plot_prediction(pcaNNet))
show(plot_prediction(spls))
show(plot_prediction(avNNet))



