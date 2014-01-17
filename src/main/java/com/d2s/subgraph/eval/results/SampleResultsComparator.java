package com.d2s.subgraph.eval.results;

import java.util.Comparator;

public class SampleResultsComparator implements Comparator<SampleResults> {
    public int compare(SampleResults o1, SampleResults o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
