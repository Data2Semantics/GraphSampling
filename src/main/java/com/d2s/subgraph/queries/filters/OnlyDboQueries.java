package com.d2s.subgraph.queries.filters;

import com.d2s.subgraph.eval.QueryWrapper;

public class OnlyDboQueries implements QueryFilter {
	
	/**
	 * filter out queries. Return false when query should -not- be filtered
	 */
	public boolean filter(QueryWrapper query) {
		return !query.isOnlyDbo();
	}
}
