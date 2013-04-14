#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 argument required (the directory to add to virtuoso)"
        exit
fi
source /home/OpenPHACTS-Virtuoso/virtuoso-environment.sh;
isqlFile="${HOME}/.isqlCmdFile.sql"
dirPath=$1



if [ ! -d $dirPath ]; then
        echo "dir $dirPath does not exist"
        exit;
fi

basename=`basename $dirPath`
graphname="http://$basename"

echo "clearing graph";
clearVirtuosoGraph.sh $graphname;


removeDirFromLdList.sh $dirPath;
addDirToVirtuoso.sh $dirPath;
