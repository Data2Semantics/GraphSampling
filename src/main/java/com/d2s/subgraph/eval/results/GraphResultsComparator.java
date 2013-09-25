package com.d2s.subgraph.eval.results;

import java.util.Comparator;

public class GraphResultsComparator implements Comparator<GraphResults> {
    public int compare(GraphResults o1, GraphResults o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
