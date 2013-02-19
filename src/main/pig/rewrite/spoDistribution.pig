REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();

rdfGraph = LOAD 'openphacts.nt' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);

predGrouped = GROUP rdfGraph BY pred;
predCount = FOREACH predGrouped GENERATE COUNT(rdfGraph);
--predCountOrdered = ORDER predCount BY $1 DESC;



subGrouped = GROUP rdfGraph BY sub;
subCount = FOREACH subGrouped GENERATE COUNT(rdfGraph);
--subCountOrdered = ORDER subCount BY $1 DESC;



objGrouped = GROUP rdfGraph BY obj;
objCount = FOREACH objGrouped GENERATE COUNT(rdfGraph);
--objCountOrdered = ORDER objCount BY $1 DESC;


rmf predCount
rmf subcount
rmf objcount
STORE predCount INTO 'predCount' USING PigStorage('\t');
STORE subCount INTO 'subcount' USING PigStorage('\t');
STORE objCount INTO 'objcount' USING PigStorage('\t');

