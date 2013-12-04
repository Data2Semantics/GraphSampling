Graph Sampling
==================
This repository covers the analysis of our Graph Sampling method, and the scripts to run our sampling pipeline. 
###Dependencies
* PIG[1]
* Hadoop[2]
* Python[3]
* R[4]
* R Igraph Package[5]
* PigAnalysis[6]: a collection of PIG scripts (wrapped as python scripts) we execute in our sampling pipeline. Check their [README](https://github.com/Data2Semantics/PigAnalysis) for more documentation on how to configure these scripts, and which dependencies this project has


###How it works
1. We rewrite an RDF graph to an unlabelled graph, in order to apply regular network analysis algorithms. We support different rewrite methods, as the applicable rewrite method may depend on the dataset properties or sampling requirements
2. We analyze the rewritten graph using a set of network analysis algorithms. Again, we support different analysis algorithms, as the quality of the sample may differ depending on the algorithm, network structure, and sampling requirements
3. We aggregate the results from the previous step back to RDF (i.e. triples). We assign weights to each triple, by aggregating the node weights calculated in the previous step. Based on this ranking, we select create the smaller sample


###Getting started
The following steps will result in a number of different samples (half the size of the original graph). All possible rewrite methods and algorithms are used.

1. Initialize the hadoop directory structure by running `initHadoopDataset <datasetId>`
2. Initialize a local directory structure by running `initRDir`
3. Add an RDF file (in ntriple format) to your hadoop file system
4. Run `bin/rewrite.sh <hdfs-ntriple-file>`, to create a set of rewritten graphs (all stored on hdfs, as well as on your file system)
5. Run R-scripts to analyze all rewritten graphs: `runAnalysis.sh .`
6. Store the analysis results on the hadoop file system, and roundtrip back to a weighted list of triples: `runPigRoundtrip.sh <datasetId>`
7. Create the desired samples: `selectMaxTopK <datasetId/roundtrip` 


####links
1. [http://pig.apache.org/](http://pig.apache.org/)
2. [http://hadoop.apache.org/](http://hadoop.apache.org/)
3. [http://www.python.org/](http://www.python.org/)
4. [http://www.r-project.org/](http://www.r-project.org/)
5. [http://igraph.sourceforge.net/](http://igraph.sourceforge.net/)
6. [https://github.com/Data2Semantics/PigAnalysis](https://github.com/Data2Semantics/PigAnalysis)
