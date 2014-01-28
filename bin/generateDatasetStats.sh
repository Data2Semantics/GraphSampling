#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1




echo "generating dataset stats"
pig pigAnalysis/stats/generateBio2RdfStats.py.py $dataset
