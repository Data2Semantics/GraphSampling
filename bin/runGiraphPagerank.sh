#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 arguments required: graph to run analysis on"
        exit;
fi
input=$1

rewrittenGraph=`basename $input`
IFS=_ read -a delimited <<< "$rewrittenGraph"
rewriteMethod=${delimited[0]}


IFS='/' array=($input)
dataset="${array[0]}"
if [ "dataset" != "" ]; then
	dataset="${array[3]}" #hmm we had an absolute path here
fi

output="${dataset}/analysis/${rewriteMethod}_pagerank_long"

echo "input: $input"
echo "output: $output"


hadoop jar giraph.jar org.apache.giraph.GiraphRunner org.data2semantics.giraph.pagerank.numerical.PageRankComputation -eif org.data2semantics.giraph.io.EdgeListLongReader  -of org.apache.giraph.io.formats.IdWithValueTextOutputFormat -mc org.data2semantics.giraph.pagerank.numerical.RandomWalkVertexMasterCompute -wc org.data2semantics.giraph.pagerank.numerical.RandomWalkWorkerContext -op $output -eip $input -w 2

