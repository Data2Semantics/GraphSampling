#!/bin/bash


echo "copying subgraph dir to das"

src="/home/lrd900/code/subgraphSelection/bin"
target="145.100.58.2:/virdir/Scratch/subgraphSelection/"
eval rsync -avz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

