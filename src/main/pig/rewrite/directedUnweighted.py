#!/usr/bin/python
from org.apache.pig.scripting import Pig


inputFile = "openphacts.nt"
outputFile = "unweightedLitAsNode"
sample = "0.01" #0.0000001: 76 items, 0.0001: 75745 items
groupResults = True
useLongHash = True
if groupResults:
    outputFile += "Grouped"
if useLongHash:
    outputFile += "Hashed"
if int(float(sample)) != 1:
    outputFile += sample
longHash = "LONGHASH"
pigScript = """
REGISTER lib/datafu.jar;
DEFINE UnorderedPairs datafu.pig.bags.UnorderedPairs();
REGISTER lib/d2s4pig-1.0.jar;
DEFINE NtLoader com.data2semantics.pig.loaders.NtLoader();
DEFINE LONGHASH com.data2semantics.pig.udfs.LongHash();
"""

if int(float(sample)) == 1:
    pigScript += """rdfGraph = LOAD '$inputFile' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
    """
else:
    pigScript += """inputGraph = LOAD '$inputFile' USING NtLoader() AS (sub:chararray, pred:chararray, obj:chararray);
    rdfGraph = SAMPLE inputGraph $sample;
    """

if groupResults:
    pigScript += """rdfGraphGrouped = GROUP rdfGraph BY sub;
rewrittenGraph = FOREACH rdfGraphGrouped GENERATE $longHash(group), 1, $longHash(rdfGraph.obj);

rmf $outputFile
STORE rewrittenGraph INTO '$outputFile' USING PigStorage();"""
else:
    pigScript += """rewrittenGraph = FOREACH rdfGraph GENERATE $longHash(sub), 1, $longHash(obj)"""
    

pigScript += """
rmf $outputFile
STORE rewrittenGraph INTO '$outputFile' USING PigStorage();"""


P = Pig.compile(pigScript)
stats = P.bind().runSingle()
if not stats.isSuccessful():
    raise 'failed'
