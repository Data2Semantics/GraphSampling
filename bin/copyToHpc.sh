#!/bin/bash


echo "copying subgraph dir to das"

src="~/code/subgraphSelection"
target="145.100.58.2:/virdir/Scratch/"
eval rsync -avz $src $target
#rsync -avz fs0.das4.cs.vu.nl:weightDistribution $localTargetDir;

