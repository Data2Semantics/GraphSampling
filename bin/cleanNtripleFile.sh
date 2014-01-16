#!/bin/bash
if [ -z "$1" ];then
        echo "at least 1 arguments required (nt file to clean)"
        exit;
fi
input=$1
output="tmpOutput"
finalOutput=$input
if [ -n "$2" ]; then
	finalOutput="$2"
fi

#replace: line breaks, and unicode stuff
sed 's/\(\\u[[:alnum:]]\{4\}\|\\n\)//g' $input > $output

mv $output $finalOutput;


