#!/bin/bash
[ -z "$PIG_SCRIPTS" ] && echo "PIG_SCRIPTS variable not set. Exiting" && exit 1;
topK="0.5"
aggrMethod="max"
dataset="dbpl"
algs=(indegree outdegree)
rewriteMethods=(s-o-litAsNode s-o-litAsLit s-o-litWithPred s-o-noLit)
#rewriteMethods=(so-so)
for rewriteMethod in "${rewriteMethods[@]}"; do
	for alg in "${algs[@]}"; do
	    inputFile=${dataset}_${rewriteMethod}_unweighted_directed_${alg}
	    if [ "$rewriteMethod" != "so-so" ]; then
		inputFile+="_max"
	    fi
		inputPath="$dataset/roundtrip/$inputFile"
	    #pig $PIG_SCRIPTS/roundtrip/selectMaxTopK.py $inputPath $topK;
	    #continue
		
#drwxr-xr-x   - lrietvld lrietvld          0 2013-05-07 19:26 /user/lrietvld/dbpl/roundtrip/dbpl_s-o-noLit_unweighted_directed_outdegree_max_max0.5.nt
#drwxr-xr-x   - lrietvld lrietvld          0 2013-05-07 01:17 /user/lrietvld/dbpl/roundtrip/dbpl_so-so_unweighted_directed_indegree
#drwxr-xr-x   - lrietvld lrietvld          0 2013-05-08 02:08 /user/lrietvld/dbpl/roundtrip/dbpl_so-so_unweighted_directed_indegree_max0.5.nt
	topKFile="$inputFile"
            topKFile+="_"
            topKFile+="max$topK.nt"
 
            tmpFile="tmp/$topKFile"
		echo "catting file  $dataset/roundtrip/$topKFile to tmp dir";
            hadoop fs -cat $dataset/roundtrip/$topKFile/part* > $tmpFile;
#echo $tmpFile;
#	exit;

		newFileSize=`cat $tmpFile | wc -l`;
		if [ "$newFileSize" == "0" ] ; then
			echo "catted file size is zero? exiting"
			exit;
		fi
            origFileSize=`cat load/$dataset/* | wc -l`
            relSize=$(echo "($newFileSize/$origFileSize) * 100" | bc -l)
            relSize=`printf %.0f $relSize`



            topKPercentage=$(echo "$topK * 100" | bc -l)
            topKPercentage=`printf %.0f $topKPercentage`
            if [ "$topKPercentage" -gt "100" ]; then
                echo "WRONG CALCULATION OF PERCENTAGE!!"
                echo "precentage: $topKPercentage"
                echo "new file size: $newFileSize"
                echo "orig file size: $origFileSize"
                exit;
            fi
            if [ ${#topKPercentage} == 1 ]; then
                    topKPercentage="0$topKPercentage"
            fi
            targetTopKFilename="$inputFile"
            targetTopKFilename+="_"
            targetTopKFilename+="max-$topKPercentage-$relSize.nt"
            
            localTargetDir="load/subgraphs/$targetTopKFilename";
            localTargetFile="$localTargetDir/$targetTopKFilename";
            if [ ! -d $localTargetDir ];then
                mkdir $localTargetDir;
            fi
		echo "moving $tmpFile to $localTargetFile";
            mv $tmpFile $localTargetFile;
            putDirInVirtuoso.sh $localTargetDir;

	done

done













