pagerank = LOAD 'pagerank/pagerank_data_4' AS ( url: chararray, pagerank: float, links:{ link: ( url: chararray ) } );

cleanedRankedResources = FOREACH pagerank GENERATE url, pagerank;


STORE cleanedRankedResources INTO 'pagerank/cleanedOutput';