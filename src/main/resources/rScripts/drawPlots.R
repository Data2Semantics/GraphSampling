library(ggplot2)

# Dogfood
flatlist_5 = read.csv("max-50_flatlist.csv",header=TRUE, sep=";")
flatlist_2 = read.csv("max-20_flatlist.csv",header=TRUE, sep=";")
summary = read.csv("summary.csv",header=TRUE, sep=";")



flatlist_all <- merge(flatlist_5,flatlist_2, all=TRUE)
flatlist <- flatlist_5

list <- read.csv("max-50_list.csv", header=TRUE, sep=";")
graph_list <- list
graph_list$queryId <- NULL

pdf("radar_plots_per_query_max-50.pdf")
stars(graph_list, labels=list$queryId)
dev.off()

# low_recall_list <- subset(flatlist, recall < 0.2)
# low_recall_list <- subset(low_recall_list, graph != 'http://df_sample_0.5.nt')
# low_recall_query_graph <- low_recall_list[,c('queryId','graph')]

# freq_table <- table(low_recall_query_graph)


# Boxplot
pdf("boxplot_plus_average_recall.pdf")
ggplot(data=flatlist_all, aes(x=graph, y=recall)) + geom_boxplot() + theme(axis.text.x=element_text(angle=-90))  + geom_point(data=summary, aes(x=graph,y=avg.recall, size=avg.recall, colour=graph)) + theme(legend.position="none")
dev.off()

# Normal scatterplot queryId by recall, colored by graph
pdf("scatterplot_byQuery_colorByGraph.pdf")
ggplot(data=flatlist, aes(x=queryId, y=recall)) + geom_point(aes(colour=graph)) + theme(axis.text.x=element_text(angle=-90))
dev.off()


# Jittered scatterplot queryId by recall, colored by graphsd
pdf("scatterplot_jittered_byQuery_colorByGraph.pdf")
ggplot(data=flatlist, aes(x=queryId, y=recall)) + geom_jitter(aes(colour=graph)) + theme(axis.text.x=element_text(angle=-90))
dev.off()

# Normal scatterplot graph by recall, colored by queryId
pdf("scatterplot_byGraph_colorByQuery.pdf")
ggplot(data=flatlist, aes(x=graph, y=recall)) + geom_point(aes(colour=queryId)) + theme(axis.text.x=element_text(angle=-90))
dev.off()


# Jittered scatterplot graph by recall, colored by queryId
pdf("scatterplot_jittered_byGraph_colorByQuery.pdf")
ggplot(data=flatlist, aes(x=graph, y=recall)) + geom_jitter(aes(colour=queryId)) + theme(axis.text.x=element_text(angle=-90))
dev.off()

# Jittered scatterplot graph by queryId, colored and sized by recall 
pdf("scatterplot_jittered_byQuery_colorByGraph.pdf")
ggplot(data=flatlist, aes(x=graph, y=queryId)) + geom_jitter(aes(size=recall, colour=recall)) + theme(axis.text.x=element_text(angle=-90))
dev.off()

# Normal scatterplot graph by queryId, colored and sized by recall
pdf("scatterplot_byQuery_colorByGraph.pdf")
ggplot(data=flatlist, aes(x=graph, y=queryId)) + geom_point(aes(size=recall, colour=recall)) + theme(axis.text.x=element_text(angle=-90))
dev.off()

# Scatterplot for low recall, graph by queryId, colored and sized by recall
# ggplot(data=low_recall_list, aes(x=graph, y=queryId)) + geom_point(aes(size=recall, colour=recall)) + theme(axis.text.x=element_text(angle=-90))

# Normal scatterplot for low recall, recall by queryId, colored and sized by recall 
# ggplot(data=low_recall_list, aes(x=queryId, y=recall)) + geom_point(aes(size=recall, colour=recall)) + theme(axis.text.x=element_text(angle=-90))


