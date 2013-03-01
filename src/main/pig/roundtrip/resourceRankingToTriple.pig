REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();



triples = LOAD 'openphacts.nt_sample_0.1' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
rankedResources = LOAD 'pagerank_0.1/pagerank_data_10' AS (resource:chararray, pagerank:float);

subGroup = COGROUP triples by sub, rankedResources by resource;
rankedSubTriples = FOREACH subGroup GENERATE FLATTEN(triples), FLATTEN(rankedResources.pagerank) AS pagerank;

objGroup = COGROUP rankedSubTriples by obj, rankedResources by resource;
rankedTriples = FOREACH objGroup GENERATE triples.sub, triples.pred, triples.obj, AVG(rankedResources.pagerank, rankedSubTriples.pagerank);

STORE rankedTriples INTO 'pagerank_rankedtriples_0.1' USING PIGSTORAGE();

