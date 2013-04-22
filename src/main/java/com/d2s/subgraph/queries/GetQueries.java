package com.d2s.subgraph.queries;

import java.util.ArrayList;


public interface GetQueries {
	public ArrayList<QueryWrapper> getQueries();
	public void setMaxNQueries(int maxNum);
}
