package de.uma.dcsim.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * This class can be used to parse a PUE trace from a file.
 * 
 * @author nilsw
 *
 */
public class PUEParser {
	
	/**
	 * Separator in the csv file that is used for the PUE trace.
	 */
	private static final String CSV_SEPARATOR = ";";
	
	/**
	 * Format of the date strings within the PUE trace file.
	 */
	private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
	
	/**
	 * Parses a PUE trace from a specified file.
	 * @param fileName Name of the file to parse.
	 * @param simStartDate Start date of the current simulation.
	 * @return List of PUE values that were parsed from the file.
	 */
	public List<PUE> parsePUEFile(String fileName, Date simStartDate) {
		ArrayList<PUE> result = new ArrayList<PUE>();
		
		
		try {
			File inputFile;
			
			if(fileName.endsWith(".csv") || fileName.endsWith(".CSV")) {
				inputFile = new File(fileName);
			}
			else {
				inputFile = new File(fileName + ".csv");
			}
			
			if(inputFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(inputFile));
				
				reader.readLine();
				
				String line;
				PUE parsedPUE;
				int lineNumber = 0;
				while((line=reader.readLine()) != null) {
					if((parsedPUE=this.parsePUECSVLine(line, ++lineNumber, simStartDate)) != null) {
						result.add(parsedPUE);
					}
				}
				
				reader.close();
			}
			else {
				System.out.println("Input file does not exist!");
				return null;
			}
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Parses CSV line from a PUE trace file. The line has to contain the following values: timestep of the PUE value; PUE value.
	 * 
	 * @param csvLine String representation of the line to parse.
	 * @param lineNumber 
	 * @return Parsed PUE object.
	 */
	private PUE parsePUECSVLine(String csvLine, int lineNumber, Date simStartDate) {
		
		String[] split = csvLine.split(CSV_SEPARATOR);
		
		if(split.length == 2) {
			try {
				int timestamp;
				double pue;
				try {
					Date timestampDate = INPUT_DATE_FORMAT.parse(split[0]);
					if(timestampDate.before(simStartDate)) {
						return null;
					}
					
					timestamp = (int)((timestampDate.getTime() - simStartDate.getTime())/1000L);
				}catch(ParseException e) {
					timestamp = Integer.parseInt(split[0]);
				}
				
				pue = Double.parseDouble(split[1]);
				
				return new PUE(timestamp, pue);
			} catch (NumberFormatException e) {
				System.out.println("NumberFormatException occured while parsing line " + lineNumber);
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Line number " + lineNumber + " has more or less than 2 fields!");
		}
		
		return null;
	}

}
