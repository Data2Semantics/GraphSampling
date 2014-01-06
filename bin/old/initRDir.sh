#!/bin/bash
if [ -z "$1" ];then
	echo "at least 1 argument required (path to r analysis dir)"
	exit
fi
rAnalysisDir="$1"
subdirs=(output)
if [ ! -d "$rAnalysisDir" ]; then
        echo "creating main directory"
        mkdir $rAnalysisDir;
fi


for subdir in "${subdirs[@]}"; do
	if [ ! -d "$rAnalysisDir/$subdir" ]; then
		echo "creating subdir $subdir"
		mkdir $rAnalysisDir/$subdir;
	fi
done