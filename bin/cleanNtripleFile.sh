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

#replace: double backslashes (makes latter things easier), line breaks, and unicode stuff
sed 's/\([\\]\{2\}\|\\u[[:alnum:]]\{4\}\|\\n\)//g' $input > $output

mv $output $finalOutput;


