#!/usr/bin/python
from org.apache.pig.scripting import Pig
"""
input:
www.A.com    1    { (www.B.com), (www.C.com), (www.D.com), (www.E.com) }
www.B.com    1    { (www.D.com), (www.E.com) }
www.C.com    1    { (www.D.com) }
www.D.com    1    { (www.B.com) }
www.E.com    1    { (www.A.com) }
www.F.com    1    { (www.B.com), (www.C.com) }"""
P = Pig.compile("""
previous_pagerank = 
    LOAD '$docs_in'
    AS ( url: $inputType, pagerank: float, links:{ link: ( url: $inputType ) } );
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
        ( 1 - $d ) + $d * SUM ((IsEmpty(outbound_pagerank.pagerank)? {0F}: outbound_pagerank.pagerank)) AS pagerank, 
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

STORE new_pagerank 
    INTO '$docs_out';

STORE max_diff 
    INTO '$max_diff';

""")

d = 0.5 #damping factor
docs_in= "unweightedLitAsNodeGroupedHashed"
inputType = "chararray" #use long if we have hashed urls
for i in range(10):
    docs_out = "pagerank/pagerank_data_" + str(i + 1)
    max_diff = "pagerank/max_diff_" + str(i + 1)
    Pig.fs("rmr " + docs_out)
    Pig.fs("rmr " + max_diff)
    stats = P.bind().runSingle()
    if not stats.isSuccessful():
        raise 'failed'
    max_diff_value = float(str(stats.result("max_diff").iterator().next().get(0)))
    print "    max_diff_value = " + str(max_diff_value)
    if max_diff_value < 0.01:
        print "done at iteration " + str(i)
        break
    docs_in = docs_out

