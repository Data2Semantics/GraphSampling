#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
echo "setting up ramdir"
dataset=$1
subgraphDir="/virdir/Scratch/subgraphSelection/"
memDir="/run/shm"
#rm $memDir/*

rsync -rtvz --del $subgraphDir/*.jar $memDir;
mkdir -p $memDir/input/qTripleWeights/;
mkdir -p $memDir/input/weightDistribution/;
mkdir -p $memDir/output/evaluation/;
mkdir -p $memDir/output/queryTriples/;
mkdir -p $memDir/cache/queries/;
mkdir -p $memDir/src/main/resources/rScripts/;


echo "copying dataset files"
rsync -rtvzq --del $subgraphDir/src/main/resources/rScripts/* $memDir/src/main/resources/rScripts;
rsync -rtvzq --del $subgraphDir/input/qTripleWeights/$dataset $memDir/input/qTripleWeights;
rsync -rtvzq --del $subgraphDir/input/weightDistribution/$dataset $memDir/input/weightDistribution;
#rsync -rtvzq --del $subgraphDir/output/queryTriples/$dataset $memDir/output/queryTriples;
rsync -rtvzq --del $subgraphDir/cache/queries $memDir/cache/;

echo "extracting query triples"
tar -zxvf $subgraphDir/output/queryTriples/${dataset}.tar.gz -C $memDir;

echo "running jar"
cd $memDir;
java -jar calcRecall.jar $dataset;

echo "copying results back"
rsync -rtvz $memDir/output/evaluation/$dataset $subgraphDir/output/evaluation;
