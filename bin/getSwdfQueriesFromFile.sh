#!/bin/bash
if [ -z "$2" ];then
	echo "at least 2 arguments required (input and output)"
	exit;
fi
grep "/sparql?" $1 | sed -n 's/.*sparql?query=\([^[:space:]^"]*\).*/\1/p' | xargs -n1 echo '----____' | perl -pe 's/%([0-9a-f]{2})/sprintf("%s", pack("H2",$1))/eig' | sed 's/\+/ /g' > $2

