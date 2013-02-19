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
--Check conflicts in long
subListLong = FOREACH rdfGraph GENERATE LONGHASH(sub) AS sub:long;
distinctSubs = DISTINCT subListLong;
distinctSubsGrouped = GROUP distinctSubs ALL;
subCount = FOREACH distinctSubsGrouped GENERATE SIZE(distinctSubs);


subList = FOREACH rdfGraph GENERATE sub AS sub:chararray;
distinctSubs = DISTINCT subList;
distinctSubsGrouped = GROUP distinctSubs ALL;
subCount = FOREACH distinctSubsGrouped GENERATE SIZE(distinctSubs);
*/

/**
Count all triples
**/
--rdfGrouped = GROUP rdfGraph ALL;
--rdfCount = FOREACH rdfGrouped GENERATE COUNT(rdfGraph);









