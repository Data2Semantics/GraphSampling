#!/bin/bash


echo "copying subgraph dir to das"

src="/home/lrd900/code/subgraphSelection/output/queryTriples"
target="hpc:/virdir/Scratch/subgraphSelection/output"
eval rsync -rtvz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

