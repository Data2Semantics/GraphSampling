#!/bin/bash


aggrMethod="max"
dataset="$1"
algs=(indegree outdegree)
rewriteMethods=(s-o-litAsNode s-o-litAsLit s-o-litWithPred s-o-noLit so-so)

for rewriteMethod in "${rewriteMethods[@]}"; do
	for alg in "${algs[@]}"; do
		pig pigAnalysis/roundtrip/$rewriteMethod.py $dataset/analysis/${dataset}_${rewriteMethod}_unweighted_directed_${alg} $aggrMethod ;
	done

done











