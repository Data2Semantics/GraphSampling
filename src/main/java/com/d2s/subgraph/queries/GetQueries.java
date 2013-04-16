package com.d2s.subgraph.queries;

import java.util.ArrayList;

import com.d2s.subgraph.eval.QueryWrapper;

public interface GetQueries {
	public ArrayList<QueryWrapper> getQueries();
	public void setMaxNQueries(int maxNum);
}