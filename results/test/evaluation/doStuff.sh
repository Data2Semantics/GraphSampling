
sed 's/<a href=.http:\/\/yasgui\.laurensrietveld\.nl.*_blank.>//g' $1 -i;
sed 's/\([[:digit:]]<\/span>\)<\/a>/\1/g' $1 -i;
cat $1 | tr '\n' ' ' > output.html;
mv output.html  $1
perl -pe "s/title\='.*?'//g;  print;" $1 > output.html;
mv output.html  $1;


