#!/bin/bash


echo "copying subgraph dir to das"

src="/home/lrd900/code/subgraphSelection/cache/queries"
target="hpc:/virdir/Scratch/subgraphSelection/cache"
eval rsync -rtvz $src $target
#rsync -rtvz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

