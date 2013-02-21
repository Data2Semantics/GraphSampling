previous_pagerank = 
    LOAD 'unweightedLitAsNodeGrouped_0.001'
    AS ( url: chararray, pagerank: float, links:{ link: ( url: chararray ) } );


B1 = filter previous_pagerank by url is not null;
B2 = filter B1 by pagerank is not null;
B3 = filter B2 by links is not null;

STORE B3 
    INTO 'B3';


outbound_pagerank = 
    FOREACH previous_pagerank 
    GENERATE 
        pagerank / COUNT ( links ) AS pagerank, 
        FLATTEN ( links ) AS to_url; 

new_pagerank = 
    FOREACH 
        ( COGROUP outbound_pagerank BY to_url, previous_pagerank BY url INNER )
    GENERATE 
        group AS url, 
        ( 1 - 0.5 ) + 0.5 * SUM ( outbound_pagerank.pagerank ) AS pagerank, 
        FLATTEN ( previous_pagerank.links ) AS links,
        FLATTEN ( previous_pagerank.pagerank ) AS previous_pagerank;

STORE new_pagerank 
    INTO 'unweightedLitAsNodeGrouped_out';



pagerank_diff = FOREACH new_pagerank GENERATE ABS ( previous_pagerank - pagerank );

max_diff = 
    FOREACH 
        ( GROUP pagerank_diff ALL )
    GENERATE
        MAX ( pagerank_diff );
STORE max_diff 
    INTO 'unweightedLitAsNodeGrouped_diff';
    
    
STORE new_pagerank 
    INTO 'unweightedLitAsNodeGrouped_out';

