#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (graph, without < and >)"
        exit
fi
source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;
isqlFile="${HOME}/.isqlCmdFile.sql"
graph=$1

echo "Clearing graph $graph"
echo "SPARQL CLEAR GRAPH <$graph>;" > $isqlFile;
echo "EXIT;" >> $isqlFile;
echo "" >> $isqlFile;
cat $isqlFile | isql;
