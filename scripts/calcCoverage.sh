#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1

queryTripleDir="/home/lrd900/code/subgraphSelection/output/queryTriples/$dataset"
concatFile="/home/lrd900/code/subgraphSelection/output/queryTriples/requiredTriplesConcat";
rm -f $concatFile;
uniqFile=${concatFile}_uniq;
echo "collecting triples"
find $queryTripleDir -type f | grep qs | grep -v "optional" | xargs cat >> ${concatFile};
 
echo "making triples file uniq"
sort $concatFile | uniq >>  ${uniqFile};
mv ${uniqFile} ${concatFile};

echo "counting triples"
count=`wc -l $concatFile`
echo "count: $count"