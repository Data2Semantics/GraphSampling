#!/bin/bash

if [ -z "$1" ];then
        echo "at least 1 argument required (file pattern for analysis dirs)"
        exit;
fi
rDir="${HOME}/rProject"
scriptsFile="$rDir/scripts/runAllAlgs.R"
logFile="$HOME/logs/runAnalysisAsDaemon_"
logFile+=`date +"%Y%m%d"`
for dir in "$@"; do
	outputRunScript="${HOME}/tmp/rRunScript_$RANDOM.R"
	if [ -d "$dir" ]; then
	    echo "Running analysis for $dir";
	    echo "setwd(\"$dir\")" > $outputRunScript;
	    cat $scriptsFile >> $outputRunScript;
	    R -f $outputRunScript >> $logFile.log 2>> $logFile.err &
	else
		if [ -f "$dir" ]; then
			echo "Pattern matches file $dir . This cant be right. exiting"
			exit;
		else 
			echo "$dir does not exist"
			exit
		fi
	fi
done
