#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1




echo "generating dataset stats"
pig $PIG_SCRIPTS/stats/generateBio2RdfStats.py $dataset
