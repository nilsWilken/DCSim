package de.uma.dcsim.frequencyTimeSeriesCompletion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import de.uma.dcsim.utilities.Constants;

public class FrequencyTimeSeriesCompletion {
	
	private static boolean checkFileForSuitability(File inputFile) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			String line = in.readLine();
			String[] split = line.split(Constants.CSV_SEPARATOR);
			in.close();
			if(split[0].equals("Time") && split[1].equals("optimalScalingFrequency") && split[2].equals("optimalShiftingFraction")) {
				return true;
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private static FileEntry parseLine(String line) {
		String[] split = line.split(Constants.CSV_SEPARATOR);
		try {
			Date time = Constants.getDateFormat().parse(split[0].trim());
			double frequency = Double.parseDouble(split[1].replace(",", ".").trim());
			double shiftingFraction = Double.parseDouble(split[2].replace(",", ".").trim());
			
			return new FileEntry(time, frequency, shiftingFraction);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void completeFrequencyTimeSeriesFile(File inputFile, File outputFile, Date startDate, Date endDate, long intervallSizeInMillis, double averageFrequency) {
		BufferedReader in;
		BufferedWriter out;
		
		try {
			in = new BufferedReader(new FileReader(inputFile));
			out = new BufferedWriter(new FileWriter(outputFile));
			long startTime = startDate.getTime();
			long endTime = endDate.getTime();
			
			HashMap<Long, FileEntry> entries = new HashMap<Long, FileEntry>();
			
			String line;
	
			//Skip first row
			line = in.readLine();
			out.write(line);
			out.newLine();
			
			FileEntry entry;
			while((line = in.readLine()) != null) {
				entry = FrequencyTimeSeriesCompletion.parseLine(line);
				entries.put(entry.getTime().getTime(), entry);
			}
			in.close();
			
			while(startTime <= endTime) {
				entry = entries.get(startTime);
				if(entry == null) {
					entry = new FileEntry(new Date(startTime), averageFrequency, 0.0);
				}
				out.write(entry.toString());
				out.newLine();
				
				startTime += intervallSizeInMillis;
			}
			out.close();
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		if(args.length != 6) {
			System.out.println("Wrong amount of arguments! \nUse the following arguments: <inputDirectory> <outputDirectory> <startDate> <endDate> <intervallSizeInMillis> <averageFrequency>");
			System.exit(0);
		}
		File inputDirectory = new File(args[0]);
		File outputDirectory = new File(args[1]);
		
		if(!inputDirectory.isDirectory()) {
			System.out.println("Input directory is no directory!");
			System.exit(0);
		}
		if(!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		if(!outputDirectory.isDirectory()) {
			System.out.println("Output directory is no directory!");
			System.exit(0);
		}
		
		
		try {
			String outputPrefix = outputDirectory.getAbsolutePath();
		
			File inputFile;
			File outputFile;
			Date startDate = Constants.getDateFormat().parse(args[2]);
			Date endDate = Constants.getDateFormat().parse(args[3]);
			long intervallSizeInMillis = Long.parseLong(args[4].trim());
			double averageFrequency = Double.parseDouble(args[5].trim());
		
			for(File f : inputDirectory.listFiles()) {
				if(f.getName().endsWith(".csv") && FrequencyTimeSeriesCompletion.checkFileForSuitability(f)) {
					inputFile = f;
					outputFile = new File(outputPrefix + "\\" + f.getName().replace(".csv", "_completed.csv"));
				
					FrequencyTimeSeriesCompletion.completeFrequencyTimeSeriesFile(inputFile, outputFile, startDate, endDate, intervallSizeInMillis, averageFrequency);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

}
