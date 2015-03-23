library(pwr)
samplePower = .80
sampleSigLevel = 0.05 #alpha / significance level
sampleType = "paired"
sampleAlternative = "two.sided"



datasetName = "swdf"
dataset = read.csv(paste(datasetName, ".csv", sep=""),header=TRUE, sep="\t")


randomSampleSig <- c(0, 0, 0)
freqBaselineSig <- c(0,0,0)
randomSampleN <- c(0,0,0)
freqBaselineN <- c(0,0,0)

i <- 4

# result <- t.test(dataset[,2], dataset[,4], paired=TRUE, conf.level=0.99)
# valuesss <- result$p.value
# randomSampleSig <- append(randomSampleSig,valuesss)
i <- 4
while( i <= 28 ){
  
  result <- t.test(dataset[,2], dataset[,i], paired=TRUE, conf.level=1-sampleSigLevel)
  randomSampleSig <- append(randomSampleSig, result$p.value)
  nStats = power.t.test(power = samplePower, sig.level=sampleSigLevel, delta = mean(dataset[,2] - dataset[,i]), sd = sd(dataset[,2] - dataset[,i]), alternative = sampleAlternative, type = sampleType)
  randomSampleN <- append(randomSampleN, nStats$n)
  i <- i + 1
}
i <- 4
while( i <= 28 ){
  tryCatch({ 
    result <- t.test(dataset[,3], dataset[,i], paired=TRUE, conf.level=1-sampleSigLevel)
    freqBaselineSig <- append(freqBaselineSig, result$p.value)
  }, error=function(err) { 
    cat("err:", conditionMessage(err), "\n") 
    freqBaselineSig <- append(freqBaselineSig,666666)
  }) 
  tryCatch({ 
    nStats = power.t.test(power = samplePower, sig.level=sampleSigLevel, delta = mean(dataset[,3] - dataset[,i]), sd = sd(dataset[,3] - dataset[,i]), alternative = sampleAlternative, type = sampleType)
    freqBaselineN <- append(freqBaselineN, nStats$n)
  }, error=function(err) { 
    cat("err:", conditionMessage(err), "\n") 
    freqBaselineN <- append(freqBaselineN, 666666)
  }) 
  i <- i + 1
}
dataframe = rbind(dataset, randomSampleSig, freqBaselineSig, randomSampleN, freqBaselineN)

write.table(dataframe, file=paste(datasetName, "_sig.csv", sep=""), sep="\t", row.names=FALSE)