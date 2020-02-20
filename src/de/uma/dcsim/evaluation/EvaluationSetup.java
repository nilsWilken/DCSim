package de.uma.dcsim.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.uma.dcsim.database.ColumnType;
import de.uma.dcsim.database.EvaluationTable;
import de.uma.dcsim.database.StatisticType;

/**
 * This class can be used to access the monitoring data from the SQLite database and automatically perform some aggregations on them.
 * The values that are currently monitored during a simulation correspond to the different values of ColumnType.
 * 
 * @author nilsw
 *
 */
public class EvaluationSetup {
	
	/**
	 * Indicates the path at which the databases from the simulation runs that should be evaluated are located.
	 */
	private String dbPath;
	
	/**
	 * Specifies the path at which the output files should be located.
	 */
	private String outputPath;
	
	
	/**
	 * Specifies the names of all databases for which the configured evaluation should be executed.
	 */
	private List<String> dbNames;
	
	
	/**
	 * Specifies the date format that is used for all date strings that are used in the evaluation process.
	 */
	private SimpleDateFormat dateFormat;
	
	/**
	 * Start and end dates that are used to select the values that are relevant for the evaluation.
	 */
	private String startDateString;
	private String endDateString;
	

	private SimulationEvaluator evaluator;
	
	/**
	 * Specifies the amount of milliseconds that each aggregation interval of possible evaluations that produce aggregated values has.
	 */
	private int millisecondsPerAggregationInterval;
	
	/**
	 * Name of the directory in which the output files will be stored.
	 */
	private String evaluationName;
	
	/**
	 * List of evaluation elements.
	 */
	private List<EvaluationSpecification> evaluationColumns;
	
	
	public EvaluationSetup(String evaluationName, String dbPath, String outputPath, List<String> dbNames, String dateFormatString, String startDate, String endDate, String millisecondsPerAggregationInterval, List<EvaluationSpecification> specifications) {
		this.dbPath = dbPath;
		this.outputPath = outputPath;
		this.dbNames = dbNames;
		this.dateFormat = new SimpleDateFormat(dateFormatString, Locale.GERMAN);
		this.startDateString = startDate;
		this.endDateString = endDate;
		this.millisecondsPerAggregationInterval = Integer.parseInt(millisecondsPerAggregationInterval);
		this.evaluationName = evaluationName;
		this.evaluationColumns = specifications;
	}

	public void evaluate() {
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		//If specified directory does not exist, create it!
		if(!(new File(outputPath+evaluationName).exists())) {
			(new File(outputPath+evaluationName)).mkdir();
		}
		
		//Create date strings for aggregated time series data
		Date start;
		Date end;
		ArrayList<String> columnNames = new ArrayList<String>();
		columnNames.add("Time");
		ArrayList<String> times = new ArrayList<String>();
		try {
			start = this.dateFormat.parse(startDateString);
			end = this.dateFormat.parse(endDateString);
			
			Date tmp = new Date(start.getTime());
			while(tmp.before(end)) {
				times.add(this.dateFormat.format(tmp));
				tmp.setTime(tmp.getTime()+millisecondsPerAggregationInterval);
			}
			
		} catch (ParseException e) {
			start = new Date();
			end = new Date();
			e.printStackTrace();
		}
				
		
		//Carry out evaluation
		ArrayList<String> totalSums = new ArrayList<String>();
		ArrayList<ArrayList<Double>> valueLists = new ArrayList<ArrayList<Double>>();
		ArrayList<Double> valueList;
		for(int i=0; i < this.dbNames.size(); i ++) {
			evaluator = new SimulationEvaluator(start, this.dbPath + this.dbNames.get(i) + ".db");
			
			for(EvaluationSpecification specification : evaluationColumns) {
				switch(specification.getEvaluationType()) {
				case AGGREGATED_AVERAGE:
					valueList = evaluator.getStatistic(StatisticType.AVERAGE, specification.getEvaluationTable(), start, end, specification.getColumn(), millisecondsPerAggregationInterval);
					valueLists.add(valueList);
					columnNames.add(this.dbNames.get(i) + specification.getEvaluationColumnName());
					break;
				case AGGREGATED_SUM:
					valueList = evaluator.getStatistic(StatisticType.SUM, specification.getEvaluationTable(), start, end, specification.getColumn(), millisecondsPerAggregationInterval);
					valueLists.add(valueList);
					columnNames.add(this.dbNames.get(i) + specification.getEvaluationColumnName());
					break;
				case ALL_RECORDS:
					ArrayList<ArrayList<String>> recordsLists = new ArrayList<ArrayList<String>>();
					ArrayList<String> recordsList;
					for(ColumnType cType : specification.getColumnTypes()) {
						recordsList = evaluator.getAllRecords(specification.getEvaluationTable(), start, end, cType, this.dateFormat);
						recordsLists.add(recordsList);
					}
					EvaluationSetup.writeLists(recordsLists, specification.getEvaluationColumnNames(), this.outputPath + "/" + this.evaluationName + "/" + this.dbNames.get(i) + "_" + EvaluationTable.getTableName(specification.getEvaluationTable()) + "_allRecords.csv");
//					writeList(evaluator.getAllRecords(specification.getEvaluationTable(), start, end, specification.getColumn(), this.dateFormat), outputPath + "/" + evaluationName + "/" + this.dbNames.get(i) + "_" + specification.getEvaluationColumnName() + "_allRecords.csv");
					break;
				case TOTAL_SUM:
					double sum = evaluator.getTotalSum(specification.getEvaluationTable(), start, end, specification.getColumn());
					totalSums.add(specification.getEvaluationColumnName() + "_" + this.dbNames.get(i) + ";" + sum);
					break;
				default:
					break;
				
				}
			}

		}
		//Write output files
		if(valueLists.size() > 0) {
			EvaluationSetup.writeLists(valueLists, columnNames, times, outputPath + "/" + evaluationName + "/aggregatedStatistics_" + (millisecondsPerAggregationInterval/60000) + "min.csv");
		}
		if(totalSums.size() > 1) {
			EvaluationSetup.writeList(totalSums, outputPath + "/" + evaluationName + "/totalSums.csv");
		}

	}
	
	private static void writeList(ArrayList<String> values, String path) {
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(new File(path)));
			
			for(String value : values) {
//				writer.write(value.replace(".", ","));
				writer.write(value);
				writer.newLine();
			}
			
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeLists(ArrayList<ArrayList<Double>> valueLists, ArrayList<String> columnNames, ArrayList<String> timeStrings, String path) {
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(new File(path)));
			
			StringBuffer buff = new StringBuffer();
			for(int i=0; i < columnNames.size(); i++) {
				buff.append(columnNames.get(i));
				if(i+1 != columnNames.size()) {
					buff.append(";");
				}
			}
			writer.write(buff.toString());
			writer.newLine();
			
			for(int i=0; i < valueLists.get(0).size(); i++) {
				buff = new StringBuffer();
				if(timeStrings != null) {
					buff.append(timeStrings.get(i) + ";");
				}
				for(int j = 0; j < valueLists.size(); j++) {
					buff.append(("" + valueLists.get(j).get(i)).replace(".", ","));
					if(j+1 != valueLists.size()) {
						buff.append(";");
					}
				}
				writer.write(buff.toString());
				writer.newLine();
			}
			
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeLists(ArrayList<ArrayList<String>> valueLists, List<String> columnNames, String path) {
		BufferedWriter writer;
		
		try {
			writer = new BufferedWriter(new FileWriter(new File(path)));
			
			StringBuffer buff = new StringBuffer();
			for(int i=0; i < columnNames.size(); i++) {
				buff.append(columnNames.get(i));
				if(i+1 != columnNames.size()) {
					buff.append(";");
				}
			}
			writer.write(buff.toString());
			writer.newLine();
			
			for(int i=0; i < valueLists.get(0).size(); i++) {
				buff = new StringBuffer();
				for(int j = 0; j < valueLists.size(); j++) {
					buff.append(("" + valueLists.get(j).get(i)));
					if(j+1 != valueLists.size()) {
						buff.append(";");
					}
				}
				writer.write(buff.toString());
				writer.newLine();
			}
			
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

}
