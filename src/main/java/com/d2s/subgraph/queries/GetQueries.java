package com.d2s.subgraph.queries;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public interface GetQueries {
	public ArrayList<QueryWrapper> getQueries();
	public void setMaxNQueries(int maxNum);
	public void saveCsvCopy(File file) throws IOException;
}
