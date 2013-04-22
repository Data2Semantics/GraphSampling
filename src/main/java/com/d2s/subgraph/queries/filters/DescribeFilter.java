package com.d2s.subgraph.queries.filters;

import com.d2s.subgraph.queries.QueryWrapper;

public class DescribeFilter implements QueryFilter {
	public boolean filter(QueryWrapper query) {
		if (query.getQuery().isDescribeType()) {
			return true;
		} else {
			return false;
		}
	}
}
