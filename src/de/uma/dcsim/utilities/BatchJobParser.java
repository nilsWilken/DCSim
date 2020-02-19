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

import de.uma.dcsim.simulationControl.Setup;

/**
 * This class can be used to parse a workload trace from a .csv file.
 * @author nilsw
 *
 */
public class BatchJobParser {
	
	/**
	 * Separator that is used in the file that contains the workload trace.
	 */
	private static final String CSV_SEPARATOR = ";";
	
	/**
	 * Format of the dates that is used in the file that contains the workload trace.
	 */
	private static final SimpleDateFormat INPUT_DATE_FORMAT = Constants.getDateFormat();
	
	/**
	 * Maximum runtime of the jobs in the workload trace.
	 */
	private static int maxDuration = Integer.MIN_VALUE;
	
	/**
	 * Minimum difference between the submission time and the start time of all jobs in the workload trace.
	 */
	private static int minSubTimeDiff = Integer.MAX_VALUE;
	
	private static long subTimeDiffSum;
	
	/**
	 * Parses a workload trace from a .csv file.
	 * @param fileName Path of the file to parse.
	 * @param simStartDate Start date of the current simulation.
	 * @return List of all jobs that were parsed from the provided file.
	 */
	public List<BatchJob> parseJobFile(String fileName, Date simStartDate) {
		ArrayList<BatchJob> result = new ArrayList<BatchJob>();
		
		
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
//				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("slaDeadlines.csv")));
				
				//Skip first line
				reader.readLine();
				
				String line;
				BatchJob parsedJob;
				int lineNumber = 0;
				while((line=reader.readLine()) != null) {
					if((parsedJob=this.parseJobCSVLine(line, ++lineNumber, simStartDate)) != null) {
						result.add(parsedJob);
//						writer.write(INPUT_DATE_FORMAT.format(new Date(simStartDate.getTime() + ((long)parsedJob.getSLADeadline()*1000L))));
//						writer.newLine();
					}
				}
				
				Setup.maximumRuntime = maxDuration;
				System.out.println("Minimum difference between submission and start time: " + minSubTimeDiff);
//				System.out.println("Average time between submission time and start time: " + (subTimeDiffSum/lineNumber));
				
				reader.close();
//				writer.close();
			}
			else {
				System.out.println("Input file does not exist!");
				return null;
			}
			
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println("Max runtime: " + Setup.maximumRuntime);
		return result;
	}
	
	/**
	 * Parses CSV line from workload trace file. 
	 * 
	 * @param csvLine
	 * @param lineNumber
	 * @return 
	 */
	private BatchJob parseJobCSVLine(String csvLine, int lineNumber, Date simStartDate) {
		
		String[] split = csvLine.split(CSV_SEPARATOR);
		
		if(split.length == 9) {
			try {
				int duration;
				int startTime;
				int endTime;
				int submissionTime;
				int slaDeadline;
				int subTimeDiff;
				try {
					Date submissionDate = INPUT_DATE_FORMAT.parse(split[4]);
					Date startDate = INPUT_DATE_FORMAT.parse(split[5]);
					if(startDate.before(simStartDate)) {
						return null;
					}
					Date endDate = INPUT_DATE_FORMAT.parse(split[6]);
					Date slaDeadlineDate = INPUT_DATE_FORMAT.parse(split[7]);
					
					
					startTime = (int)((startDate.getTime() - simStartDate.getTime())/1000L);
					endTime = (int)((endDate.getTime() - simStartDate.getTime())/1000L);
					submissionTime = (int)((submissionDate.getTime() - simStartDate.getTime())/1000L);
					slaDeadline = (int)((slaDeadlineDate.getTime() - simStartDate.getTime())/1000L);
					if(submissionTime < 0) {
						submissionTime = 0;
					}
//					System.out.println(split[4] + " Start: " + startTime + "; End: " + endTime);
					duration = endTime - startTime;
					if(duration > maxDuration) {
						maxDuration = duration;
					}
					subTimeDiff = startTime - submissionTime;
					if(subTimeDiff < 0) {
						submissionTime = startTime;
						if(submissionTime < 0) {
							submissionTime = 0;
						}
						subTimeDiff = 0;
					}
					subTimeDiffSum += subTimeDiff;
					if(subTimeDiff < minSubTimeDiff) {
						minSubTimeDiff = subTimeDiff;
					}
					Setup.secondsPerSimulationTimestep = 1;
				}catch(ParseException e) {
					submissionTime = Integer.parseInt(split[4]);
					startTime = Integer.parseInt(split[5]);
					endTime = Integer.parseInt(split[6]);
					slaDeadline = Integer.parseInt(split[7]);
					duration = endTime - startTime;
				}
				if(duration == 0) {
					duration = 1;
				}
				
//				StandardSLAModel model = new StandardSLAModel();
//				slaDeadline = model.createDeadline(startTime, duration);
				
				return new BatchJob(split[0], Double.parseDouble(split[1]),
						Integer.parseInt(split[2]), Double.parseDouble(split[3]),
						duration, BatchJobStatus.PARSED, submissionTime, startTime, slaDeadline, Integer.parseInt(split[8]));
			} catch (NumberFormatException e) {
				System.out.println("NumberFormatException occured while parsing line " + lineNumber);
				e.printStackTrace();
			}
		}
		else if(split.length == 8) {
			int duration;
			int submissionTime;
			int slaDeadline;
			try {
				Date submissionDate = INPUT_DATE_FORMAT.parse(split[4]);
				Date slaDeadlineDate = INPUT_DATE_FORMAT.parse(split[6]);
				
				
				submissionTime = (int)((submissionDate.getTime() - simStartDate.getTime())/1000L);
				slaDeadline = (int)((slaDeadlineDate.getTime() - simStartDate.getTime())/1000L);
				if(submissionTime < 0) {
					submissionTime = 0;
				}
//				System.out.println(split[4] + " Start: " + startTime + "; End: " + endTime);
				duration = Integer.parseInt(split[5]);
				if(duration > maxDuration) {
					maxDuration = duration;
				}
			}catch(ParseException e) {
				submissionTime = Integer.parseInt(split[4]);
				slaDeadline = Integer.parseInt(split[6]);
				duration = Integer.parseInt(split[5]);
			}
			if(duration == 0) {
				duration = 1;
			}
			
//			StandardSLAModel model = new StandardSLAModel();
//			slaDeadline = model.createDeadline(startTime, duration);
			
			return new BatchJob(split[0], Double.parseDouble(split[1]),
					Integer.parseInt(split[2]), Double.parseDouble(split[3]),
					duration, BatchJobStatus.PARSED, submissionTime, 0, slaDeadline, Integer.parseInt(split[7]));
			
		}
		else {
			System.out.println("BatchJobParser: Line number " + lineNumber + " has wrong amount of fields!");
		}
		
		return null;
	}

}
