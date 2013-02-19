REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();

rdfGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
--rdfGraph = SAMPLE largeGraph 0.0000001; --0.0000001: 76 items
--rdfGraph = LOAD 'small.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);

rdfGraphGrouped = GROUP rdfGraph ALL;
graphWithCount = FOREACH rdfGraphGrouped GENERATE FLATTEN(rdfGraph), COUNT(rdfGraph) AS totalCount:long;
predGroup = GROUP graphWithCount BY pred;


newGraph = FOREACH predGroup GENERATE FLATTEN(graphWithCount), COUNT(graphWithCount) AS predCount:long;


weightedGraph = FOREACH newGraph GENERATE sub, (double)predCount / (double)totalCount, obj;
rmf weightedGraph
STORE weightedGraph INTO 'weightedGraph' USING PigStorage('\t');


weightedGraphInverted = FOREACH newGraph GENERATE sub, (1.0-((double)predCount / (double)totalCount)), obj;
--dump weightedGraphInverted;
rmf weightedGraphInverted
STORE weightedGraphInverted INTO 'weightedGraphInverted' USING PigStorage('\t');


