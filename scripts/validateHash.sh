
if [ -z "$1" ];then
	echo "at least 1 argument required (dataset). We'll check just 1 rewrite method (the most verbose one) for hashes"
	exit;
fi
dataset=$1



pig pigAnalysis/utils/validateHash.py $dataset;

echo "Counts:";
hadoop fs -cat tmp/**/**
