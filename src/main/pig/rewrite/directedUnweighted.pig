--pagerank: www.A.com    1    { (www.B.com), (www.C.com), (www.D.com), (www.E.com) }

REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();
inputGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);


rdfGraph = SAMPLE inputGraph 0.00001;

rdfGraphGrouped = GROUP rdfGraph BY sub;

rewrittenGraph = FOREACH rdfGraphGrouped GENERATE group, 1, rdfGraph.obj;


rewrittenGraph = FOREACH rdfGraph GENERATE LONGHASH(sub), 1, LONGHASH(obj) ; --just use a weight of 1 for now



rdfGraph = GROUP rdfGraph BY sub;



rmf 'unweightedLitAsNodeGroupedHashed'
STORE rewrittenGraphGrouped INTO 'unweightedLitAsNodeGroupedHashed' USING PigStorage();
