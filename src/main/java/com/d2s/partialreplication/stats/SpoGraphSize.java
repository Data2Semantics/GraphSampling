package com.d2s.partialreplication.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * calc spo graph size.
 * uses 3 files as input, containing frequencies of s/p/o's
 *
 */
public class SpoGraphSize {

	File input;
	public SpoGraphSize(String file) throws IOException {
		this.input = new File(file);
		
	}
	
	public void readFile() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(input));
		String line;
		long count = 0;
		while ((line = br.readLine()) != null) {
		   if (line.length() > 0) {
			   count += calcNumberOfLinks(Long.parseLong(line.trim()));
			   
		   }
		}
		System.out.println(count);
		br.close();
	}
	
	public long calcNumberOfLinks(long size) {
		long result = 0;
		for (long i = 1; i < size; i++) {
			result += i;
		}
		return result;
		
	}
	
	public static void main(String[] args) throws IOException  {
//		String file = "/home/lrd900/spoDist/countsNoLabels/predCounts.txt";
//		String file = "/home/lrd900/spoDist/countsNoLabels/objCounts.txt";
		String file = "/home/lrd900/spoDist/countsNoLabels/subCounts.txt";
		SpoGraphSize stats = new SpoGraphSize(file);
//		stats.readFile();
		System.out.println(stats.calcNumberOfLinks(47891421));
	}
}
