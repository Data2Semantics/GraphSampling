Graph Sampling
==================
This repository covers the analysis of our Graph Sampling method, and the scripts to run the SampLD pipeline. 
###Dependencies
* PIG[1] 
* Hadoop[2]
* Python[3]
* PigAnalysis[4]: a collection of PIG scripts (wrapped as python scripts) we execute in our sampling pipeline. Check their [README](https://github.com/aaai2014sampld/PigAnalysis/blob/master/README.md) for more documentation on how to configure these scripts, and which dependencies this project has
* GiraphAnalysis[5]: the giraph codebase to execute network analysis algorithms using map-reduce. Check their [README](https://github.com/aaai2014sampld/GiraphAnalysis/blob/master/README.md) for more documentation for more information on how to run giraph, and which dependencies this project has


###How it works
1. We rewrite an RDF graph to an unlabelled graph, in order to apply regular network analysis algorithms. We support different rewrite methods, as the applicable rewrite method may depend on the dataset properties or sampling requirements
2. We analyze the rewritten graph using a set of network analysis algorithms. Again, we support different analysis algorithms, as the quality of the sample may differ depending on the algorithm, network structure, and sampling requirements
3. We aggregate the results from the previous step back to RDF (i.e. triples). We assign weights to each triple, by aggregating the node weights calculated in the previous step. Based on this ranking, we select create the smaller sample



###Getting started
The following steps will result in a number of different samples (half the size of the original graph). All possible rewrite methods and algorithms are used.

1. Initialize the hadoop directory structure by running `initHadoopDataset <datasetId>`
2. Add an RDF file (in ntriple format) to your hadoop file system. Filename: `~/<datasetId>/<datasetId>.nt`
3. Run `bin/runAll.sh <ntriple-file>`, to run the pipeline, and create the weighted set of triples from which you can select your sample <sub>note: this command depends on the PigAnalysis and GiraphAnalysis codebase. Make sure that both this current repository, and these other repositories, are subdirectories of your current working directory</sub>



####links
1. [http://pig.apache.org/](http://pig.apache.org/)
2. [http://hadoop.apache.org/](http://hadoop.apache.org/)
3. [http://www.python.org/](http://www.python.org/)
4. [https://github.com/aaai2014sampld/PigAnalysis](https://github.com/aaai2014sampld/PigAnalysis)
5. [https://github.com/aaai2014sampld/GiraphAnalysis](https://github.com/aaai2014sampld/GiraphAnalysis)



