package com.d2s.partialreplication.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class Helper {
	private static String HEADER_CONTENT = "application/x-www-form-urlencoded";
	private static String HEADER_ACCEPT_QUERY = "application/sparql-results+json";
	private static String HEADER_ACCEPT_CONSTRUCT = "text/turtle";
	private static String ADDITIONAL_ARGS = "&soft-limit=-1";

	public static InputStream executeQuery(String endpoint, String queryString) {
		InputStream result = execHttpPost(endpoint, "query=" + queryString + ADDITIONAL_ARGS, HEADER_ACCEPT_QUERY);
		return result;
	}

	public static InputStream executeConstruct(String endpoint, String queryString) {
		InputStream result = execHttpPost(endpoint, "query=" + queryString + ADDITIONAL_ARGS, HEADER_ACCEPT_CONSTRUCT);
		return result;
	}

	public static InputStream execHttpPost(String url, String content, String acceptHeader) {
		try {
			StringEntity e = new StringEntity(content, "UTF-8");
			e.setContentType(HEADER_CONTENT);
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", acceptHeader);
			// Execute
			HttpClient httpclient = new DefaultHttpClient();
			httppost.setEntity(e);
			HttpResponse response = httpclient.execute(httppost);

			return response.getEntity().getContent();

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
		return null;
	}

	public static void writeStreamToOutput(InputStream stream) throws IOException {
		writeStreamToOutput(stream, new CleanTurtle(){

			public String processLine(String input) {
				return input;
			}});
	}
	
	public static void writeStreamToOutput(InputStream stream, CleanTurtle cleanTurtle) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		while ((line = rd.readLine()) != null) {
			System.out.println(cleanTurtle.processLine(line));
		}
	}

	public static String getStreamAsString(InputStream stream) throws IOException {
		return getStreamAsString(stream, new CleanTurtle(){

			public String processLine(String input) {
				return input;
			}});
	}
	
	public static String getStreamAsString(InputStream stream, CleanTurtle cleanTurtle) throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		String result = "";
		while ((line = rd.readLine()) != null) {
			result += cleanTurtle.processLine(line) + "\n";
		}
		return result;
	}

	public static void writeStreamToFile(InputStream stream, String fileLocation) throws IOException {
		writeStreamToFile(stream, fileLocation, new CleanTurtle(){

			public String processLine(String input) {
				return input;
			}});
	}
	
	public static void writeStreamToFile(InputStream stream, String fileLocation, CleanTurtle cleanTurtle) {
		BufferedWriter writer = null;
		try {
			// write the inputStream to a FileOutputStream
			writer = new BufferedWriter(new FileWriter(fileLocation));
			BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
			String line;
			while ((line = rd.readLine()) != null) {
				writer.write(cleanTurtle.processLine(line));
				writer.newLine();
			}
			System.out.println("New file created!");
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}finally {
            //Close the BufferedWriter
            try {
                if (writer != null) {
                	writer.flush();
                	writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	}

}
