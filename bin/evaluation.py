# -*- coding: utf-8 -*-
# <nbformat>3.0</nbformat>
 
# <codecell>
 
from bs4 import BeautifulSoup
 
html_doc = open('results.html','r').read()
 
soup = BeautifulSoup(html_doc)
 
# <codecell>
 
import re
 
cols = 15
 
# The array of graph names that we intend to check
# Needs to be in the same order as the columns in results.html
graphs = ["http://swdf_resourceContext_indegree"]
 
# The graphs with the old names
#graphs = ["http://df_so-so_unweighted_directed_indegree_max-50-47.nt",
# "http://df_so-so_unweighted_directed_outdegree_max-50-49.nt",
# "http://df_so-so_unweighted_directed_pagerank_max-50-50.nt",
# "http://df_s-o-litWithPred_unweighted_directed_indegree_max_max-50-50.nt",
# "http://df_s-o-litWithPred_unweighted_directed_outdegree_max_max-50-48.nt",
# "http://df_s-o-litWithPred_unweighted_directed_pagerank_max_max-50-50.nt",
# "http://df_s-o-litAsNode_unweighted_directed_indegree_max_max-50-48.nt",
# "http://df_s-o-litAsNode_unweighted_directed_outdegree_max_max-50-48.nt",
# "http://df_s-o-litAsNode_unweighted_directed_pagerank_max_max-50-50.nt",
# "http://df_s-o-litAsLit_unweighted_directed_indegree_max_max-50-47.nt",
# "http://df_s-o-litAsLit_unweighted_directed_outdegree_max_max-50-48.nt",
# "http://df_s-o-litAsLit_unweighted_directed_pagerank_max_max-50-48.nt",
# "http://df_s-o-noLit_unweighted_directed_indegree_max_max-50-47.nt",
# "http://df_s-o-noLit_unweighted_directed_outdegree_max_max-50-46.nt",
# "http://df_s-o-noLit_unweighted_directed_pagerank_max_max-50-50.nt"]
 
regex = r"FROM .*\nFROM .*?\n"
 
endpoint = "http://ops.few.vu.nl:8890/sparql"
 
# <codecell>
 
rows = soup.find_all('tr')
 
queries = []
 
 
for row in rows:
    #print row
    if len(row.find_all('td')) == 0 :
        continue
        
    query_id = row.find_all('td')[0].text
    # The cols array should contain all columns that we want to check
    # These should correspond 1:1 to the graphs array defined above!!!
    
    # The below is to get only a single column. For some odd reason the find_all function does not return a list of length one, 
    # but just a single element. Really annoying, but in any case, hence the extra brackets.
    #cols = [row.find_all('td')[11]]
    # The below is to get all sample-columns, excluding the random sample and the other one whose name I keep forgetting
    cols = row.find_all('td')[7:10]
    cols.extend(row.find_all('td')[11:])
 
    for col in cols :
        index = cols.index(col)
        #print "ddddddddddddddd"
        #print index;
        #print col
        recall = float(col.find('span').text)
        print recall
        query = col['title']
        
        specific_query = re.sub(regex,'FROM <{}>\n'.format(graphs[index]),query)
        
        queries.append({'id': query_id, 'sample': graphs[index], 'recall': recall, 'query': query, 'specific_query': specific_query})
 
# <codecell>
 
print "Found {} query/sample combinations".format(len(queries))
 
# <codecell>
 
from SPARQLWrapper import SPARQLWrapper, JSON
import csv
 
sparql = SPARQLWrapper(endpoint)
sparql.setReturnFormat(JSON)
 
w = csv.writer(open('evaluation.csv','w'),delimiter=';',quotechar='"',quoting=csv.QUOTE_NONNUMERIC)
 
w.writerow(['id','reported recall','original results','sample results','expected recall','sample graph','query'])
 
for q in queries:
    sparql.setQuery(q['query'])
    results = sparql.query().convert()['results']['bindings']
    
    sparql.setQuery(q['specific_query'])
    specific_results = sparql.query().convert()['results']['bindings']
    
    row = [q['id'],q['recall'], len(results), len(specific_results), float(len(specific_results))/float(len(results)), q['sample'], q['specific_query']]
    w.writerow(row)
    
    # Prints a row, but leaves out the query, as that makes things hard to understand.
    print row[:-1]