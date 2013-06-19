datasetName <- "dbp"
# datasetName <- "lmdb"
datasetName <- "sp2b"
datasetName <- "swdf"

# source("getSignificance.R")


datasets <- c("dbp", "lmdb", "sp2b", "swdf")
# datasets <- c("dbp", "sp2b", "swdf")



for(i in 1:length(datasets)){
  datasetName <- datasets[i]
  source("getSignificance.R")
}

