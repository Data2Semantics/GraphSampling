#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required (dataset)"
        exit;
fi
dataset=$1
dir=/home/lrd900/code/subgraphSelection/output/queryTriples/$dataset
dirBasename=`basename $dir`
echo "processing $dirBasename";
concatFile=${dir}/../${dirBasename}AllQtriples;
rm -f $concatFile;
find $dir -type f | grep qs | xargs cat >> ${concatFile};
uniqFile=${concatFile}_uniq; 
sort $concatFile | uniq >>  ${uniqFile};
mv ${uniqFile} ${concatFile};

echo "now rsyncing"#also dels on the dest side
rsync -avz --del ${concatFile} fs0.das4.cs.vu.nl:/var/scratch/lrd900/qTriples/$dirBasename

echo "adding file to hdfs"
ssh fs0.das4.cs.vu.nl uploadQueryTriplesToHdfs.sh $dirBasename



