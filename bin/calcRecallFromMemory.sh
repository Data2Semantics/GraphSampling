#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset). Optional second arg is pattern used in hadoop ls"
        exit;
fi
echo "setting up ramdir"
dataset=$1
subgraphDir="/virdir/Scratch/subgraphSelection/"
memDir="/run/shm"
rm $memDir/*

cp $subgraphDir/*.jar $memDir;
mkdir -p $memDir/input/qTripleWeights/;
mkdir -p $memDir/input/weightDistribution/;
mkdir -p $memDir/output/evaluation/;
mkdir -p $memDir/output/queryTriples/;

echo "copying dataset files"
cp -r $subgraphDir/input/qTripleWeights/$dataset $memDir/input/qTripleWeights;
cp -r $subgraphDir/input/weightDistribution/$dataset $memDir/input/weightDistribution;
cp -r $subgraphDir/output/queryTriples/$dataset $memDir/output/queryTriples;


echo "running jar"
cd $memDir;
java -jar run.jar inMem;

echo "copying results back"
rsync -avz $memDir/output/evaluation/$dataset $subgraphDir/output/evaluation;
