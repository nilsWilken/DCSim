package evaluation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.ColumnType;
import database.DatabaseRecord;
import database.EvaluationTable;
import database.SqLiteDBHandler;
import database.StatisticType;

/**
 * This class can be used to retrieve several values from the databases that store the simulation monitoring values.
 * @author nilsw
 *
 */
public class SimulationEvaluator {
	
	/**
	 * Database handler that is used to access the databases.
	 */
	private SqLiteDBHandler dbHandler;
	
	/**
	 * Start time of the evaluation.
	 */
	private Date simStartTime;
	
	public SimulationEvaluator(Date simStartTime, String dbPath) {
		this.dbHandler = new SqLiteDBHandler(dbPath);
		this.simStartTime = new Date(simStartTime.getTime());
	}
	
	/**
	 * Retrieves a list of names of all tables within the connected database.
	 * @return List of names of all tables within the connected database.
	 */
	public ArrayList<String> getAvailableRecordTableNames() {
		return this.dbHandler.getTableCatalog();
	}
	
	public Date getSimStartTime() {
		return this.simStartTime;
	}
	
	/**
	 * Retrieves all records of a specific column of a database table that has TIMESTAMP values between the specified start and end dates.
	 * @param tableName Name of the table from which the records are requested.
	 * @param startDate Start date of the interval for the selection of records.
	 * @param endDate End date of the interval for the selection of records.
	 * @param column Column of the table for which all recorded values in the specified interval are returned.
	 * @return List of strings, where each string contains the evaluation record (including times of the records) entry for all data values of the specified column that lie between the specified dates.
	 */
	public ArrayList<String> getAllRecords(EvaluationTable evaluationTable, Date startDate, Date endDate, ColumnType column, SimpleDateFormat dateFormat) {
		ArrayList<DatabaseRecord> records = this.dbHandler.getRecordsBetweenDates(evaluationTable, startDate, endDate);
		ArrayList<String> result = new ArrayList<String>();
		StringBuffer buff;
		for(DatabaseRecord record : records) {
			buff = new StringBuffer();
			buff.append(dateFormat.format(new Date(record.getLong(ColumnType.TIMESTAMP))) + ";");
			switch(ColumnType.getJavaVarType(column)) {
			case DOUBLE:
				buff.append(""+record.getDouble(column));
				break;
			case INT:
				buff.append(""+record.getInt(column));
				break;
			case LONG:
				buff.append(""+record.getLong(column));
				break;
			default:
				break;
				
			}
			result.add(buff.toString());
		}
		
		return result;
	}
	
	/**
	 * Retrieves the total sum of all record values of a column of a database table. Thereby, the records are filtered by their timestamps.
	 * @param tableName Name of the table for which the total sum of records from one column is requested.
	 * @param startDate Start date of the interval that is used for the selection of summed records.
	 * @param endDate End date of the interval that is used for the selection of summed records.
	 * @param column Column from which the values are summed.
	 * @return Sum of all selected data values from the specified column. 
	 */
	public double getTotalSum(EvaluationTable evaluationTable, Date startDate, Date endDate, ColumnType column) {
		List<ColumnType> filterColumns = new ArrayList<ColumnType>();
		filterColumns.add(ColumnType.TIMESTAMP);
		
		List<Object[]> filterFrames = new ArrayList<Object[]>();
		filterFrames.add(new Object[] {startDate.getTime(), endDate.getTime()});
		
		return this.dbHandler.getStatisticWithFilter(evaluationTable, StatisticType.SUM, column, filterColumns, filterFrames);
	}
	
	/**
	 * Uses a specific statistic to aggregate the data values of a specified column of a database table.
	 * @param statisticType Statistic that is used to aggregate the data values.
	 * @param tableName Name of the table from which the data is retrieved.
	 * @param startDate Start date of the interval in which the TIMESTAMP values of the selected records have to be.
	 * @param endDate End date of the interval in which the TIMESTAMP values of the selected records have to be.
	 * @param statisticColumn Column from which the data values are aggregated.
	 * @param millisecondsPerTimestep Amount of milliseconds that each aggregation interval contains.
	 * @return Time series data of the data value from the specified column in the specified database table. The TIMESTAMP values of the records from which the
	 * data values are taken have to be in the specified interval. The time series data interval basis is defined by the millisecondsPerTimestep parameter.
	 */
	public ArrayList<Double> getStatistic(StatisticType statisticType, EvaluationTable evaluationTable, Date startDate, Date endDate, ColumnType statisticColumn, long millisecondsPerTimestep) {
		ArrayList<Double> result = new ArrayList<Double>();
		
		List<ColumnType> filterColumns = new ArrayList<ColumnType>();
		filterColumns.add(ColumnType.TIMESTAMP);
		
		Date tmp = new Date(startDate.getTime());
		Date tmp2 = new Date(tmp.getTime() + millisecondsPerTimestep);
		List<Object[]> filterFrame;
		while(tmp.before(endDate)) {
			filterFrame = new ArrayList<Object[]>();
			filterFrame.add(new Object[] {tmp.getTime(), tmp2.getTime()});
			
			result.add(this.dbHandler.getStatisticWithFilter(evaluationTable, statisticType, statisticColumn, filterColumns, filterFrame));
			
			if((tmp.getTime()-startDate.getTime())%86400000 == 0) {
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				System.out.println(format.format(tmp));
			}

			
			tmp.setTime(tmp.getTime() + millisecondsPerTimestep);
			tmp2.setTime(tmp.getTime() + (millisecondsPerTimestep-1000));
		}
		
		return result;
	}

}
