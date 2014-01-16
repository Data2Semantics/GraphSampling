#!/bin/bash


if [ -z "$1" ];then
        echo "at least 1 argument required: the directory of the dataset containing the qtriples (this can be a glob)"
        exit;
fi

dirs=($@)
for dir in ${dirs[@]}
do
	dirBasename=`basename $dir`
	echo "processing $dirBasename";
	concatFile=${dir}/../${dirBasename}AllQtriples;
	rm -f $concatFile;
	find . -type f | grep qs | xargs cat >> ${concatFile};
	uniqFile=${concatFile}_uniq; 
	sort $concatFile | uniq >>  ${uniqFile};
	mv ${uniqFile} ${concatFile};
	
	echo "now rsyncing"
	rsync -avz ${concatFile} fs0.das4.cs.vu.nl:qTriples/$dirBasename
done




