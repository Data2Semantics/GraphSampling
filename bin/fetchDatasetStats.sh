#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1


statsDir="$dataset/stats"

echo "fetching stats"
numTriples=`hadoop fs -cat $statsDir/tripleCount/*`;
subCount=`hadoop fs -cat $statsDir/subCount/*`;
predCount=`hadoop fs -cat $statsDir/predCount/*`;
objCount=`hadoop fs -cat $statsDir/objCount/*`;
typeCount=`hadoop fs -cat $statsDir/distinctTypeCount/*`;
distinctLiteralCount=`hadoop fs -cat $statsDir/distinctLiteralCount/*`;
literalCount=`hadoop fs -cat $statsDir/literalCount/*`;

echo "numTriples: $numTriples";
echo "subCount: $subCount";
echo "predCount: $predCount";
echo "objCount: $objCount";
echo "distinct typeCount: $typeCount";
echo "literalCount: $literalCount"
echo "distinct literalCount: $distinctLiteralCount"; 
