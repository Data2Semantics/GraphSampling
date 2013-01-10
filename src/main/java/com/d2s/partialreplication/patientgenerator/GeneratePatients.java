package com.d2s.partialreplication.patientgenerator;

import com.d2s.partialreplication.helpers.Helper;




public class GeneratePatients 
{
	private static String ENDPOINT_AERS = "http://eculture2.cs.vu.nl:5020/sparql/";
	private static String SELECT_QUERY = "" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX aers: <http://aers.data2semantics.org/resource/>\n" + 
			"PREFIX aersv: <http://aers.data2semantics.org/vocab/>\n" + 
			"PREFIX patient: <http://www.data2semantics.org/ontology/patient/>\n" + 
			"SELECT ?report ?gender ?weight ?age ?reportLabel ?diagnosis ?involvement ?drug ?drugDose ?drugRoute ?lotNumber \n" + 
			"WHERE {\n" + 
			"?report rdf:type aersv:Report.\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:gender ?gender}\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:weight ?weight}\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:age ?age}\n" + 
			"OPTIONAL {\n" + 
			"    ?report rdfs:label ?reportLabel}\n" + 
			"\n" + 
			"    ?diagnosis aersv:reaction_of ?report.\n" + 
			" OPTIONAL {\n" + 
			"  ?involvement aersv:therapy_of ?report;\n" + 
			"  		aersv:drug ?drug.\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:drug_dose ?drugDose\n" + 
			"    }\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:drug_route ?drugRoute\n" + 
			"    }\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:lot_number ?lotNumber.\n" + 
			"    }\n" + 
			"  }\n" + 
			"} LIMIT 10";
	private static String CONSTRUCT_QUERY = "" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"PREFIX aers: <http://aers.data2semantics.org/resource/>\n" + 
			"PREFIX aersv: <http://aers.data2semantics.org/vocab/>\n" + 
			"PREFIX patient: <http://www.data2semantics.org/ontology/patient/>\n" + 
			"CONSTRUCT {\n" +
			"	?report rdf:type <http://www.data2semantics.org/ontology/patient/Patient>;\n" +
			"				rdfs:label ?reportLabel;\n" +
			"				aersv:weight ?weight;\n" +
			"				aersv:age ?age;\n" +
			"				aersv:gender ?gender\n" + 
			"				patient:hasDiagnosis ?diagnosis;\n" +
			"				patient:usesMedication [\n" +
			"					aersv:drug ?drug;\n" +
			"					aersv:drug_dose ?drugDose;\n" +
			"					aersv:drug_rout ?drugRoute;\n" +
			"					aersv:lot_number ?lotNumber\n" +
			"				] \n" +
			"} " +
			"WHERE {\n" +
			"<http://aers.data2semantics.org/resource/report/4944752> rdf:type aersv:Report.\n"+
			"?report rdf:type aersv:Report.\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:gender ?gender}\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:weight ?weight}\n" + 
			"OPTIONAL {\n" + 
			"    ?report aersv:age ?age}\n" + 
			"OPTIONAL {\n" + 
			"    ?report rdfs:label ?reportLabel}\n" + 
			"\n" + 
			"    ?diagnosis aersv:reaction_of ?report.\n" + 
			" OPTIONAL {\n" + 
			"  ?involvement aersv:therapy_of ?report;\n" + 
			"  		aersv:drug ?drug.\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:drug_dose ?drugDose\n" + 
			"    }\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:drug_route ?drugRoute\n" + 
			"    }\n" + 
			"    OPTIONAL {\n" + 
			"  		?involvement aersv:lot_number ?lotNumber.\n" + 
			"    }\n" + 
			"  } " + 
			"} LIMIT 50\n"; 
//			"BIND ((REPLACE(?reportLabel, \"Report\", \"Patient\")) AS ?patientLabel)\n" +
//			"BIND ((URI(CONCAT(\"" + PATIENT_URI + "\", (REPLACE(LCASE(?patientLabel), \" \", \"_\"))))) AS ?patientUri)\n" +
//			"}";
	
	/**
	 * So, the 4store version used for AERS doesnt support BIND, so we havent been able to rename and clean some uri's labels. Do it manually!
	 */
	private CleanTurtle getCleaner() {
		return new CleanTurtle(){

			public String processLine(String line) {
				//create proper uri for patients
				line = line.replace("http://aers.data2semantics.org/resource/report/", "http://aers.data2semantics.org/resource/patient/");
				
				//create proper label for patients: "Report #5624920" --> "Patient #5624920"
				line = line.replaceAll("\"Report(\\s#\\d+)\"", "\"Patient$1\"");
				return line;
			}};
	}
	
	public void generateTurtle() {
		System.out.println(CONSTRUCT_QUERY);
		try {
			Helper.writeStreamToOutput(Helper.executeQuery(ENDPOINT_AERS, CONSTRUCT_QUERY), getCleaner());
//			Helper.writeStreamToFile(Helper.executeQuery(ENDPOINT_AERS, CONSTRUCT_QUERY), "patients.ttl", getCleaner());
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void executeSelect() {
//		System.out.println(SELECT_QUERY);
		try {
			Helper.writeStreamToOutput(Helper.executeQuery(ENDPOINT_AERS, SELECT_QUERY));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
    public static void main( String[] args )
    {
    	GeneratePatients generator = new GeneratePatients();
//    	System.out.println(CONSTRUCT_QUERY);
//    	System.out.println(SELECT_QUERY);
        
//        generator.executeSelect();
        generator.generateTurtle();
    }
}
