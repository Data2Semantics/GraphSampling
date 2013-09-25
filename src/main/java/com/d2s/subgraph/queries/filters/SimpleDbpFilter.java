package com.d2s.subgraph.queries.filters;

import java.util.List;
import org.data2semantics.query.Query;
import org.data2semantics.query.filters.QueryFilter;
import com.hp.hpl.jena.sparql.core.Var;

public class SimpleDbpFilter implements QueryFilter {
	public boolean filter(Query query) {
		boolean isSimpleDbp = false;
		List<Var> projVars = query.getProjectVars();
		if (projVars.size() == 3) {
			isSimpleDbp = true;
			for (Var var: projVars) {
				if (!var.toString().equals("?name") && !var.toString().equals("?description") && !var.toString().equals("?thumbnail")) {
					isSimpleDbp = false;
					return isSimpleDbp;
				}
			}
			return true;
		} else if (projVars.size() == 1 && projVars.get(0).toString().equals("?cat")) {
			return true;
		} else {
			return false;
		}
	}
}
