#!/bin/bash


echo "copying subgraph dir to das"

src="/home/lrd900/code/subgraphSelection/bin"
target="hpc:/virdir/Scratch/subgraphSelection/"
eval rsync -rtvz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

src="/home/lrd900/code/subgraphSelection/src/main/resources/rScripts"
target="hpc:/virdir/Scratch/subgraphSelection/src/main/resources/"
eval rsync -rtvz $src $target