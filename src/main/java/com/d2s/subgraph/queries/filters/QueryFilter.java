package com.d2s.subgraph.queries.filters;


import com.d2s.subgraph.queries.QueryWrapper;

public interface QueryFilter {
	
	public boolean filter(QueryWrapper query);
}
