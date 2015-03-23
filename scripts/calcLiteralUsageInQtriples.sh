#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (query triple dir of dataset)"
        exit;
fi
dataset=$1

queryTripleDir=$1
totalQueryCount=0;
literalQueryCount=0;
find $queryTripleDir -maxdepth 1 -type d  -name "query-*" -print0 | while read -d $'\0' queryDir
do
	echo `basename $queryDir`;
	totalQueryCount=$(($totalQueryCount + 1))
	#find $queryDir -mindepth 2 -wholename "*optional*" -prune -o -type f -print0 | while read -d $'\0' tripleFile
	for tripleFile in $(find $queryDir -mindepth 2 -wholename "*optional*" -prune -o -type f); 
	do
		if grep -q "\"" "$tripleFile"; then
			echo "found literal in file!"
			echo $tripleFile;
			literalQueryCount=$(($literalQueryCount + 1))
			break;
		fi
	done
done
echo "$literalQueryCount / $totalQueryCount (literal queries vs all queries)"

