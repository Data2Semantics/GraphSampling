previous_pagerank = 
    LOAD 'input'
    AS ( url: chararray, pagerank: float, links:{ link: ( url: chararray ) } );
/**
Creates 
 <http://rdf.chemspider.com/3442>, 1.0,  {(<http://www.w3.org/2004/02/skos/core#exactMatch>), (<http://bla>)}
*/

outbound_pagerank = 
    FOREACH previous_pagerank 
    GENERATE 
        pagerank / COUNT ( links ) AS pagerank, 
        FLATTEN ( links ) AS to_url; 
/**
Creates:
1.0, <http://bla>
1.0, <http://www.w3.org/2004/02/skos/core#exactMatch>
*/
new_pagerank = 
    FOREACH 
        ( COGROUP outbound_pagerank BY to_url, previous_pagerank BY url INNER )
    GENERATE 
        group AS url, 
        ( 1 - 0.5 ) + 0.5 * SUM ((IsEmpty(outbound_pagerank.pagerank)? {0F}: outbound_pagerank.pagerank)) AS pagerank,
        FLATTEN ( previous_pagerank.links ) AS links,
        FLATTEN ( previous_pagerank.pagerank ) AS previous_pagerank;
/**
The COGROUP part creates:
<http://rdf.chemspider.com/3442>, {}, {(<http://rdf.chemspider.com/3442>, 1.0, {(<http://www.w3.org/2004/02/skos/core#exactMatch>), (<http://bla>)})}
*/

pagerank_diff = FOREACH new_pagerank GENERATE ABS ( previous_pagerank - pagerank );

max_diff = 
    FOREACH 
        ( GROUP pagerank_diff ALL )
    GENERATE
        MAX ( pagerank_diff );
rm pagerank/pagerank_data_1
STORE new_pagerank 
    INTO 'pagerank/pagerank_data_1';
rm pagerank/max_diff_1
STORE max_diff 
    INTO 'pagerank/max_diff_1';
