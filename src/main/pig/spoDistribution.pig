REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();

rdfGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);

grouped = GROUP rdfGraph BY $COL;
counted = FOREACH group GENERATE group, COUNT(rdfGraph);
countOrdered = ORDER counted BY $1 DESC;
rmf $COL
STORE countOrdered INTO '$COL' USING PigStorage('\t');