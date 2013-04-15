package com.d2s.subgraph.queries.filters;

import com.d2s.subgraph.eval.QueryWrapper;

public class OnlyDboQueries implements QueryFilter {
	public boolean filter(QueryWrapper query) {
		if (query.isOnlyDbo()) {
			return true;
		} else {
			return false;
		}
	}
}
