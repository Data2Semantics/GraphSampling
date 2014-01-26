#!/bin/bash


echo "copying subgraph dir to das"

target="/home/lrd900/code/subgraphSelection/output"
src="hpc:/virdir/Scratch/subgraphSelection/output/evaluation"
eval rsync -rtvz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

