#!/usr/bin/python
from org.apache.pig.scripting import Pig


inputFile = "openphacts.nt"
outputFile = "unweightedLitAsNode"
sample = "0.0001" #0.0000001: 76 items, 0.0001: 75745 items
groupResults = True
useLongHash = True
if groupResults:
    outputFile += "Grouped"
if useLongHash:
    outputFile += "Hashed"


pigScript = """
REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();
"""

if sample == 1:
    pigScript += """rdfGraph = LOAD '$inputFile' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
    """
else:
    pigScript += """inputGraph = LOAD '$inputFile' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
    rdfGraph = SAMPLE inputGraph $sample;
    """


if useLongHash:
    pigScript += """rewrittenGraph = FOREACH rdfGraph GENERATE LONGHASH(sub), 1, LONGHASH(obj) ; --just use a weight of 1 for now
"""
else:
    pigScript += """rewrittenGraph = FOREACH rdfGraph GENERATE sub, 1, obj ; --just use a weight of 1 for now
"""
if groupResults:
    pigScript += """rewrittenGraphGrouped = GROUP rewrittenGraph BY $0;

STORE rewrittenGraphGrouped INTO '$outputFile' USING PigStorage();"""
else:
    pigScript += """STORE rewrittenGraph INTO '$outputFile' USING PigStorage();"""


P = Pig.compile(pigScript)
stats = P.bind().runSingle()
if not stats.isSuccessful():
    raise 'failed'
