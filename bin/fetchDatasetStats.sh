#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1

echo "getting number of triples used as input (for validation purposes)"
numRequiredTriples=`hadoop fs -cat $dataset/evaluation/qTriples | wc -l`

statsDir="$dataset/stats"

echo "fetching stats"
numTriples=`hadoop fs -cat $statsDir/tripleCount/*`;
subCount=`hadoop fs -cat $statsDir/subCount/*`;
predCount=`hadoop fs -cat $statsDir/predCount/*`;
objCount=`hadoop fs -cat $statsDir/objCount/*`;
typeCount=`hadoop fs -cat $statsDir/distinctTypeCount/*`;
literalCount=`hadoop fs -cat $statsDir/distinctLiteralCount/*`;

echo "numTriples: $numTriples";
echo "subCount: $subCount";
echo "predCount: $predCount";
echo "objCount: $objCount";
echo "typeCount: $typeCount";
echo "literalCount: $literalCount"; 