package utilities;

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
 * This class can be used to parse a DR request event trace from a .csv file.
 * @author nilsw
 *
 */
public class DRRequestParser {
	
	/**
	 * Separator that is used in the .csv file that contains the DR request event trace.
	 */
	private static final String CSV_SEPARATOR = ";";
	
	/**
	 * Format of the dates that are used in the file that contains the DR request event trace.
	 */
	private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
	
	/**
	 * Parses a DR request event trace from a .csv file. 
	 * @param fileName Path of the file to parse.
	 * @param simStartDate Start date of the current simulation.
	 * @return List of the DR requests that were parsed from the specified file.
	 */
	public List<DRRequest> parseDRRequestFile(String fileName, Date simStartDate) {
		ArrayList<DRRequest> result = new ArrayList<DRRequest>();
		
		
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
				DRRequest parsedRequest;
				int lineNumber = 0;
				while((line=reader.readLine()) != null) {
					if((parsedRequest=this.parseDRRequestCSVLine(line, ++lineNumber, simStartDate)) != null) {
						result.add(parsedRequest);
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
	 * Parses CSV line from a DR request event trace file. The line has to contain the following values: timestamp;adjustmentHeight;
	 * provisonType;requestLength;compensationReward
	 * 
	 * @param csvLine
	 * @param lineNumber
	 * @return 
	 */
	private DRRequest parseDRRequestCSVLine(String csvLine, int lineNumber, Date simStartDate) {
		
		String[] split = csvLine.split(CSV_SEPARATOR);
		
		if(split.length == 5) {
			try {
				int requestLength;
				int timestamp;
				double adjustmentHeight;
				ReserveProvisionType provisionType;
				double compensationReward;
				try {
					Date timestampDate = INPUT_DATE_FORMAT.parse(split[0]);
					if(timestampDate.before(simStartDate)) {
						return null;
					}
					
					timestamp = (int)((timestampDate.getTime() - simStartDate.getTime())/1000L);
				}catch(ParseException e) {
					timestamp = Integer.parseInt(split[0]);
				}
				
				adjustmentHeight = Double.parseDouble(split[1]);
				provisionType = ReserveProvisionType.parseFromString(split[2]);
				requestLength = Integer.parseInt(split[3]);
				compensationReward = Double.parseDouble(split[4]);
				
				return new DRRequest(timestamp, adjustmentHeight, provisionType, requestLength, compensationReward);
			} catch (NumberFormatException e) {
				System.out.println("NumberFormatException occured while parsing line " + lineNumber);
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Line number " + lineNumber + " has more or less than 5 fields!");
		}
		
		return null;
	}

}
