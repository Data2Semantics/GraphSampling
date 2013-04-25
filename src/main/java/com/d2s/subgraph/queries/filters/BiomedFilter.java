package com.d2s.subgraph.queries.filters;

import com.d2s.subgraph.queries.QueryWrapper;

public class BiomedFilter implements QueryFilter {
	public boolean filter(QueryWrapper query) {
		if (query.toString().contains("bioportal")) {
			return false;
		} else {
			return true;
		}
	}
}
