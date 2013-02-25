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

cogrpd = cogroup outbound_pagerank by to_url, previous_pagerank by url;
/**
creates:
<http://rdf.chemspider.com/3442>, {}, {(<http://rdf.chemspider.com/3442>, 1.0, {(<http://www.w3.org/2004/02/skos/core#exactMatch>), (<http://bla>)})}
*/                   
                      
new_pagerank = 
    FOREACH 
       cogrpd
    GENERATE 
        group AS url, 
        ( 1 - $d ) + $d * SUM (outbound_pagerank.pagerank) AS pagerank, 
        FLATTEN ( previous_pagerank.links ) AS links,
        FLATTEN ( previous_pagerank.pagerank ) AS previous_pagerank;
STORE new_pagerank 
    INTO '$docs_out';
nonulls = filter new_pagerank by previous_pagerank is not null and pagerank is not null;
pagerank_diff = FOREACH nonulls GENERATE ABS ( previous_pagerank - pagerank );

grpall = group pagerank_diff all;
max_diff = foreach grpall generate MAX (pagerank_diff);

STORE max_diff INTO '$max_diff';

""")

d = 0.5 #damping factor
docs_in= "unweightedLitAsNodeGroupedHashed"
out_dir = "pagerank/"
inputType = "chararray" #use long if we have hashed urls
for i in range(10):
    docs_out = out_dir + "pagerank_data_" + str(i + 1)
    max_diff = out_dir + "max_diff_" + str(i + 1)
    Pig.fs("rmr " + docs_out)
    Pig.fs("rmr " + max_diff)
    stats = P.bind().runSingle()
    if not stats.isSuccessful():
        raise 'failed'
    max_diff_value = float(str(stats.result("max_diff").iterator().next().get(0)))
    print "    max_diff_value = " + str(max_diff_value)
    if max_diff_value < 0.01:
        print "done at iteration " + str(i) + ". Cleaning output"
#        docs_in = docs_out
#        formatted_out = out_dir + "pagerank_cleaned"
#        Pig.fs("rmr " + formatted_out)
#        pigClean = Pig.compile("""
#pagerank = LOAD '$docs_in' AS ( url: chararray, pagerank: float, links:{ link: ( url: chararray ) } );
#cleanedRankedResources = FOREACH pagerank GENERATE url, pagerank;
#STORE cleanedRankedResources INTO '$formatted_out';
#        """)
#        pigClean.bind().runSingle()
        break
    #max_diff of previous iterations never used, so clean it up
    Pig.fs("rmr " + max_diff) 
    if i > 1:
        #never for 1st iteration! (otherwise we delete original input...
        Pig.fs("rmr " + docs_in)
        
    docs_in = docs_out

