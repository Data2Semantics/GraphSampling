#!/bin/bash

}

if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1
pig pigAnalysis/stats/calcDegreeDistribution.py $dataset;