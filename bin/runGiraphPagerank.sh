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
unset IFS
dataset="${array[0]}"
if [ "dataset" != "" ]; then
	dataset="${array[3]}" #hmm we had an absolute path here
fi

output="${dataset}/analysis/${rewriteMethod}_pagerank_long"

echo "input: $input"
#echo "output: $output"
#echo "remove $output ? ? "

hadoop fs -rmr $output
#cmd="hadoop jar giraph.jar org.apache.giraph.GiraphRunner org.data2semantics.giraph.pagerank.numerical.PageRankComputation -eif org.data2semantics.giraph.io.EdgeListLongReader  -of org.apache.giraph.io.format -w 10"
cmd="hadoop jar giraph.jar org.apache.giraph.GiraphRunner -Dmapred.map.child.java.opts=\"-Xmx4000m\" org.data2semantics.giraph.pagerank.numerical.PageRankComputation -eif org.data2semantics.giraph.io.EdgeListLongReader -of org.apache.giraph.io.formats.IdWithValueTextOutputFormat -mc org.data2semantics.giraph.pagerank.numerical.RandomWalkVertexMasterCompute -wc org.data2semantics.giraph.pagerank.numerical.RandomWalkWorkerContext -op ${output} -eip ${input} -w 40"
echo $cmd
eval $cmd
