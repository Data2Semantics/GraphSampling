REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();

largeGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
--rdfGraph = SAMPLE largeGraph 0.0000001; --0.0000001: 76 items
rdfGraph = SAMPLE largeGraph 0.001; --0.0001: 75745 items
--dump rdfGraph;




/**
 * CREATING A NETWORK OF S->O NODES (directed, unlabelled)
 */
rewrittenGraph = FOREACH rdfGraph GENERATE sub, 1, obj ; --just use a weight of 1 for now

STORE rewrittenGraph INTO 'spoGraph' USING PigStorage('\t');