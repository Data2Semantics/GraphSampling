REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();


grouped = GROUP rdfGraph BY $COL;
counted = FOREACH group GENERATE group, COUNT(rdfGraph);
countOrdered = ORDER counted BY $1 DESC;
STORE countOrdered INTO '$COL' USING PigStorage('\t');