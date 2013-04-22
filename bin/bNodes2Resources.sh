#!/bin/bash
if [ -z "$1" ];then
        echo "at least 2 arguments required (input and output)"
        exit;
fi
input=$1
output=$2
sed 's/_:\([^ ]*\)/<http:\/\/\1>/g' $input > $output
