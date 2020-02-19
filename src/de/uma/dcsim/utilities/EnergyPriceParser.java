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
 * This class can be used to parse an energy price trace from a .csv file.
 * 
 * @author nilsw
 *
 */
public class EnergyPriceParser {
	
	/**
	 * Separator that is used in the csv file that contains the energy price trace.
	 */
	private static final String CSV_SEPARATOR = ";";
	
	/**
	 * Format of the dates that are used in the csv file that contains the energy price trace.
	 */
	private static final SimpleDateFormat INPUT_DATE_FORMAT = Constants.getDateFormat();;
	
	/**
	 * Parses the energy trace from a specified file.
	 * @param fileName Path of the file to parse.
	 * @param simStartDate Start date of the current simulation.
	 * @return List of energy prices that were parsed.
	 */
	public List<EnergyPrice> parseEnergyPriceFile(String fileName, Date simStartDate) {
		ArrayList<EnergyPrice> result = new ArrayList<EnergyPrice>();
		
		
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
				
				String line;
				EnergyPrice parsedEnergyPrice;
				int lineNumber = 0;
				reader.readLine();
				while((line=reader.readLine()) != null) {
					if((parsedEnergyPrice=this.parseEnergyPriceCSVLine(line, ++lineNumber, simStartDate)) != null) {
						result.add(parsedEnergyPrice);
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
	 * Parses CSV line from an energy price trace file. The line has to contain the following values: timestamp;energyPrice.
	 * 
	 * @param csvLine
	 * @param lineNumber
	 * @return 
	 */
	private EnergyPrice parseEnergyPriceCSVLine(String csvLine, int lineNumber, Date simStartDate) {
		
		String[] split = csvLine.split(CSV_SEPARATOR);
		
		if(split.length == 2) {
			try {
				int timestamp;
				double price;
				try {
					Date timestampDate = INPUT_DATE_FORMAT.parse(split[0]);
					if(timestampDate.before(simStartDate)) {
						return null;
					}
					
					timestamp = (int)((timestampDate.getTime() - simStartDate.getTime())/1000L);
				}catch(ParseException e) {
					timestamp = Integer.parseInt(split[0]);
				}
				
				price = Double.parseDouble(split[1].replace(",", "."));
				
				return new EnergyPrice(timestamp, price);
			} catch (NumberFormatException e) {
				System.out.println("NumberFormatException occured while parsing line " + lineNumber);
				e.printStackTrace();
			}
		}
		else {
			System.out.println("EnergyPriceParser: Line number " + lineNumber + " has more or less than 2 fields!");
		}
		
		return null;
	}

}
