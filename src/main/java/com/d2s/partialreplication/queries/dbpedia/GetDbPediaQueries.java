package com.d2s.partialreplication.queries.dbpedia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import com.d2s.partialreplication.queries.EvalQuery;
import com.d2s.partialreplication.queries.GetQueries;


public class GetDbPediaQueries implements GetQueries {
	
	public GetDbPediaQueries() throws Exception {
		parseXml();
	}
	
	ArrayList<EvalQuery> queries = new ArrayList<EvalQuery>();
	public void parseXml() throws FileNotFoundException, XMLStreamException {
		FileInputStream fis = null;
        fis = new FileInputStream("src/main/resources/dbpedia-train.xml");
        XMLInputFactory xmlInFact = XMLInputFactory.newInstance();
        XMLStreamReader reader = xmlInFact.createXMLStreamReader(fis);
        while(reader.hasNext()) {
            reader.next(); // do something here
            if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                if (reader.getLocalName().equals("query")) {
                	queries.add(new EvalQuery(reader.getElementText()));
                }
            }
        }
	}

	public ArrayList<EvalQuery> getQueries() {
		return this.queries;
	}
	
	
	public static void main(String[] args)  {
		
		try {
			GetDbPediaQueries getQueries = new GetDbPediaQueries();
			getQueries.parseXml();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
