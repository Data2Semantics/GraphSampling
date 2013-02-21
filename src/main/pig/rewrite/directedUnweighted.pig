REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();
inputGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
rdfGraph = SAMPLE inputGraph 0.001;
rdfGraphHashed = FOREACH rdfGraph GENERATE sub, obj;
rdfGraphGrouped = GROUP rdfGraphHashed BY $0;


rewrittenGraph = FOREACH rdfGraphGrouped GENERATE group, 1, rdfGraphHashed.$1;



rmf unweightedLitAsNodeGrouped_0.0001
STORE rewrittenGraph INTO 'unweightedLitAsNodeGrouped_0.001' USING PigStorage();
