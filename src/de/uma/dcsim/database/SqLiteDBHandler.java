package de.uma.dcsim.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;


/**
 * This class provides an interface that can be used to store relevant simulation data in a SQLite database.
 * @author nilsw
 *
 */
public class SqLiteDBHandler {

	/**
	 * Connection to the SQLite database.
	 */
	private Connection dbConnection;
	
	/**
	 * Map of insert statements for all tables in the database.
	 */
	private HashMap<String, PreparedStatement> insertStatements;
	
	/**
	 * Map of select statements for all tables in the database.
	 */
	private HashMap<String, PreparedStatement> selectAllStatements;
	
	/**
	 * Map of statements that can be used to request all records that have timestamp values that lie between two specific dates. For each table in the 
	 * databse, the map contains a separate statement.
	 */
	private HashMap<String, PreparedStatement> selectAllBetweenDates;
	
	/**
	 * Specifies the format in which dates are formatted for the output from and input to the database.
	 */
	private SimpleDateFormat format;
	
	/**
	 * Indicates whether the createTables() method was called.
	 */
	private boolean createTablesCalled;
	
	/**
	 * 
	 */
//	private HashMap<String, TableType> tableTypePrefixes;

//	private static final ColumnType[] GENERAL_SIMULATION_MONITORING_TABLE_SCHEMA = { ColumnType.TIMESTAMP, ColumnType.NUMBER_OF_ACTIVE_NODES, ColumnType.NUMBER_OF_RUNNING_JOBS, ColumnType.TOTAL_EC, ColumnType.IT_POWER, ColumnType.HVAC_EC,
//			ColumnType.JOB_POWER, ColumnType.ENERGY_COST, ColumnType.SLA_COST };
	
//	private static final ColumnType[] DR_REQUEST_MONITORING_TABLE_SCHEMA =  {ColumnType.TIMESTAMP, ColumnType.OPTIMAL_SCALING_FREQUENCY, ColumnType.OPTIMAL_SHIFTING_FRACTION, ColumnType.MAXIMUM_POWER_PROVISION, 
//			ColumnType.AVERAGE_REMAINING_RUNTIME, ColumnType.AVERAGE_NODES_PER_JOB};

	/**
	 * Connects to an existing database or creates a new database.
	 * @param databaseName Name of the database to which the handler should connect or, in the case that no database with this name exists, the name of
	 * the newly created database.
	 */
	public SqLiteDBHandler(String databaseName) {
		String url = "jdbc:sqlite:" + databaseName;
		this.format = new SimpleDateFormat("dd_MM_yyyy");
		this.format.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.createTablesCalled = false;
		
		try {
			//Establish connection to the database.
			this.dbConnection = DriverManager.getConnection(url);
			if (this.dbConnection != null) {
				DatabaseMetaData meta = this.dbConnection.getMetaData();
				System.out.println("The driver name is " + meta.getDriverName());
				System.out.println("A new database has been created.");

				//Initialize statement lists
				this.insertStatements = new HashMap<String, PreparedStatement>();
				this.selectAllStatements = new HashMap<String, PreparedStatement>();
				this.selectAllBetweenDates = new HashMap<String, PreparedStatement>();
				
				//Create prepared statements for all tables that already exist in the database
				HashMap<String, ArrayList<String>> tableLists = new HashMap<String, ArrayList<String>>();
				String[] split;
				ArrayList<String> cList;
				for(String name : this.getTableCatalog()) {
					split = name.split("_\\d\\d_\\d\\d_\\d\\d\\d\\d");
					cList = tableLists.get(split[0]);
					if(cList == null) {
						cList = new ArrayList<String>();
						cList.add(name);
						tableLists.put(split[0], cList);
					}
					else {
						cList.add(name);
					}
				}
				
				for (String key : tableLists.keySet()) {
					if(tableLists.get(key).size() > 1) {
						this.createTablesCalled = true;
						this.updatePreparedStatements(key);
						this.createTablesCalled = false;
					}
					else {
						this.updatePreparedStatements(key);
					}
				}
				
				//Turn autocommit function off
				this.dbConnection.setAutoCommit(false);

			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Creates a new table of the specified type with the specified name within the database.
	 * @param name Name of the newly created table.
	 * @param tableType Value of TableType that indicates the type of the newly created table.
	 */
	private void createRecordTable(String name, EvaluationTable evaluationTable) {
//		name = name + "_" + TableType.getTablePrefix(tableType);
		try {
			//Create SQL queries
			Statement st = dbConnection.createStatement();
			String sql1 = "drop table if exists " + name + ";";
			String sql2 = "create table " + name + SqLiteDBHandler.createTableSchema(evaluationTable) + ";";
			
			//Execute queries on the database
			st.executeUpdate(sql1);
			st.executeUpdate(sql2);

			st.close();

			//If the createRecordTables() method was called, the prepared statements are updated within that method
			if(!this.createTablesCalled) {
				this.updatePreparedStatements(name);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new table of the specified type with the specified name within the database.
	 * @param name Name of the newly created table.
	 * @param simStart Start date of the simulation for which the database is used.
	 * @param simEnd End date of the simulation for which the database is used.
	 * @param tableType Value of TableType that corresponds to the type of the newly created table.
	 */
	public void createRecordTables(Date simStart, Date simEnd, EvaluationTable evaluationTable) {
		this.createTablesCalled = true;
		String name = EvaluationTable.getTableName(evaluationTable);
		//Due to efficiency reasons it is created a separate table per day of the simulation
		Date current = new Date(simStart.getTime()-86400000);
		Date end = new Date(simEnd.getTime());
		do {
			current.setTime(current.getTime()+86400000);
			this.createRecordTable(this.createTableNameFromPrefix(name, current.getTime()), evaluationTable);
		}while(current.before(end));
		this.updatePreparedStatements(name);
		
		this.createTablesCalled = false;
	}

	/**
	 * Inserts a record into a table of the database.
	 * @param tableName Name of the table in which the record should be inserted.
	 * @param record Instance of DatabaseRecord that corresponds to the record that should be inserted.
	 * @param evaluationTable Value of TableType that corresponds to the type of the table in which the record should be inserted.
	 */
	public void insertRecord(DatabaseRecord record) {
//		tableName += "_" + this.format.format(new Date(record.getTimestamp()));
		EvaluationTable targetEvaluationTable = record.getTargetEvaluationTable();
		String tableName = EvaluationTable.getTableName(targetEvaluationTable);
		PreparedStatement insertStatement = insertStatements.get(this.createTableNameFromPrefix(tableName, record.getLong(ColumnType.TIMESTAMP)));
		ColumnType[] tableSchema = EvaluationTable.getTableSchema(targetEvaluationTable);
		
		if (insertStatement != null) {
			try {
				ColumnType cType;
				for(int i=1; i <= tableSchema.length; i++) {
					cType = tableSchema[i-1];
					this.setValueInPreparedStatement(insertStatement, cType, record.getValueByColumnType(cType), i);
				}
				insertStatement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Perform commit on the database.
	 */
	public void commit() {
		try {
			dbConnection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Perform roll back on the database.
	 */
	public void rollBack() {
		try {
			dbConnection.rollback();
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves a list of names of all currently existing tables in the database.
	 * @return Returns an instance of ArrayList that contains the names of all tables that currently exist in the database.
	 */
	public ArrayList<String> getTableCatalog() {
		ArrayList<String> tableCatalog = new ArrayList<String>();

		try {
			ResultSet objects = this.executeQuery("select * from sqlite_master;");

			while (objects.next()) {
				tableCatalog.add(objects.getString((2)));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tableCatalog;
	}

	/**
	 * Retrieves all records from a table in the database.
	 * @param tableName Name of the table from which all records should be selected.
	 * @return An instance of ArrayList that contains all records from the specified table.
	 */
	public ArrayList<DatabaseRecord> getResultTable(EvaluationTable evaluationTable) {
		PreparedStatement selectAll = selectAllStatements.get(EvaluationTable.getTableName(evaluationTable));

		if (selectAll == null) {
			return null;
		}

		try {
			ResultSet allEntries = selectAll.executeQuery();

			return this.convertResultSet(allEntries);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieves only a part of the column values for all records in a database table.
	 * @param tableName Name of the table from which the data is requested.
	 * @param columns Instance of ArrayList that contains values of ColumnType for all columns that should be retrieved.
	 * @return An instance of ArrayList that contains all records that are stored in the specified table.
	 */
	public ArrayList<DatabaseRecord> getPartialResultTable(EvaluationTable evaluationTable, ArrayList<ColumnType> columns) {
		String cols = "(";
		for (int i = 0; i < columns.size(); i++) {
			cols += ColumnType.convertToStringName(columns.get(i));
			if (i + 1 != columns.size()) {
				cols += ",";
			}
		}
		cols += ")";

		ResultSet entries = this.executeQuery("select " + cols + " from " + this.createUnionTableQuery(EvaluationTable.getTableName(evaluationTable)) + ";");

		return this.convertResultSet(entries);
	}

	/**
	 * Retrieves all records from a database table that have a TIMESTAMP value that lies between the specified dates.
	 * @param tableName Name of the table from which the records should be retrieved.
	 * @param firstDate First date of the interval in which the selected records are located.
	 * @param lastDate Last date of the interval in which the selected records are located.
	 * @return An instance of ArrayList that contains all records that were selected based on the interval that is specified
	 * by the dates that were passed as parameters.
	 */
	public ArrayList<DatabaseRecord> getRecordsBetweenDates(EvaluationTable evaluationTable, Date firstDate, Date lastDate) {
		PreparedStatement selectBetweenDates = selectAllBetweenDates.get(EvaluationTable.getTableName(evaluationTable));

		if (selectBetweenDates == null) {
			return null;
		}

		try {
			selectBetweenDates.setLong(1, firstDate.getTime());
			selectBetweenDates.setLong(2, lastDate.getTime());
			ResultSet entries = selectBetweenDates.executeQuery();
			return this.convertResultSet(entries);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Retrieves all records from a database tables for which the values of a specified column lie in a specified filter interval.
	 * @param tableName Name of the table from which the records are requested.
	 * @param column Value of ColumnType that indicates the column that contains the values on which the selected records should be filtered.
	 * @param frame Array of Object that contains the start and end values (in this order) of the interval that is used for the filtering of the records.
	 * @return List of all records that lie in the specified filter interval.
	 */
	public ArrayList<DatabaseRecord> getRecordsBetween(EvaluationTable evaluationTable, ColumnType column, Object[] frame) {
		if (frame.length != 2) {
			return null;
		} else {
			if (column == ColumnType.TIMESTAMP) {
				return this.getRecordsBetweenDates(evaluationTable, (Date) frame[0], (Date) frame[1]);
			}

			String col = ColumnType.convertToStringName(column);
			Double lowerBound = (Double) frame[0];
			Double upperBound = (Double) frame[1];

			String sql = "select * from " + this.createUnionTableQuery(EvaluationTable.getTableName(evaluationTable)) + " where " + col + " >= " + lowerBound + " and " + col + " <= "
					+ upperBound + ";";

			ArrayList<DatabaseRecord> result = this.convertResultSet(this.executeQuery(sql));
			return result;
		}
	}

	/**
	 * Retrieves a sql statistic value from a database table.
	 * @param tableName Name of the table from which the statistic is requested.
	 * @param column Value of ColumnType that corresponds to the column for which the statistic is requested.
	 * @param statistic Value of StatisticType that indicates the statistic which is requested.
	 * @return Value of the specified sql statistic.
	 */
	public double getStatistic(EvaluationTable evaluationTable, ColumnType column, StatisticType statistic) {
		ResultSet value = this.executeQuery("select " + StatisticType.getSqLiteName(statistic) + "("
				+ ColumnType.convertToStringName(column) + ") from " + this.createUnionTableQuery(EvaluationTable.getTableName(evaluationTable)) + ";");
		double result = 0.0;

		try {
			value.next();
			result = value.getDouble(1);
			value.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Retrieves a sql statistic from all records of a database table that match one or several filtering intervals.
	 * @param tableName Name of the table for which the statistic is requested.
	 * @param statistic Value of StatisticType that indicates the type of statistic that is requested.
	 * @param statisticColumn Value of ColumnType that indicates the column for which the statistic is requested.
	 * @param filterColumns List of one or several values of ColumnType that specify the columns on which the records that are used to calculate the statistic are filtered.
	 * @param filterFrames List of Object arrays that specify the interval boundaries for the columns that are specified by the filterColumns parameter. The statistic is calculated
	 * on all records that lie within the specified filter intervals.
	 * @return Requested sql statistic value, which is calculated on the basis of all records that lie in the specified filter intervals. Thereby, the filter intervals are open intervals.
	 */
	public double getStatisticWithFilter(EvaluationTable evaluationTable, StatisticType statistic, ColumnType statisticColumn, List<ColumnType> filterColumns, List<Object[]> filterFrames) {
		//Create sql query
		String sql;
		if(filterColumns.size() > 1 && !filterColumns.contains(ColumnType.TIMESTAMP)) {
			sql = "select " + StatisticType.getSqLiteName(statistic) + "(" + ColumnType.convertToStringName(statisticColumn) + ") from " + this.createUnionTableQuery(EvaluationTable.getTableName(evaluationTable)) + " where ";
		}
		else {
			Object[] filter = filterFrames.get(0);
			sql = "select " + StatisticType.getSqLiteName(statistic) + "(" + ColumnType.convertToStringName(statisticColumn) + ") from " + this.createUnionTableQueryBetweenDates(EvaluationTable.getTableName(evaluationTable), (Long)filter[0], (Long)filter[1]) + ") where ";
		}
//		System.out.println(sql);
		
		//Add filters to sql query
		long lowerBoundL;
		long upperBoundL;
		double lowerBoundD;
		double upperBoundD;
		int lowerBoundI;
		int upperBoundI;
		ColumnType currentType;
		for(int i=0; i < filterColumns.size(); i++) {
			currentType = filterColumns.get(i);
			switch(ColumnType.getJavaVarType(currentType)) {
			case LONG:
				lowerBoundL = ((Long)filterFrames.get(i)[0]).longValue();
				upperBoundL = ((Long)filterFrames.get(i)[1]).longValue();
				if(i != 0) {
					sql += " and ";
				}
				sql += ColumnType.convertToStringName(currentType) + " >= " + lowerBoundL + " and " + ColumnType.convertToStringName(currentType) + " <= " + upperBoundL;
				break;
			case DOUBLE:
				lowerBoundD = ((Double)filterFrames.get(i)[0]).doubleValue();
				upperBoundD = ((Double)filterFrames.get(i)[1]).doubleValue();
				if(i != 0) {
					sql += " and ";
				}
				sql += ColumnType.convertToStringName(currentType) + " >= " + lowerBoundD + " and " + ColumnType.convertToStringName(currentType) + " <= " + upperBoundD;
			case INT:
				lowerBoundI = ((Integer)filterFrames.get(i)[0]).intValue();
				upperBoundI = ((Integer)filterFrames.get(i)[1]).intValue();
				if(i != 0) {
					sql += " and ";
				}
				sql += ColumnType.convertToStringName(currentType) + " >= " + lowerBoundI + " and " + ColumnType.convertToStringName(currentType) + " <= " + upperBoundI;
			}
		}
		
		//Execute sql query
		sql += ";";
		ResultSet value = this.executeQuery(sql);
		double result = 0.0;
		
		try {
			value.next();
			result = value.getDouble(1);
			value.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Retrieves the row count of a table in the database.
	 * @param tableName Name of the table for which the row count is requested. 
	 * @return Row count of the specified table.
	 */
	public int getRowCount(EvaluationTable evaluationTable) {
		ResultSet rowCount = this.executeQuery("select count(*) from " + this.createUnionTableQuery(EvaluationTable.getTableName(evaluationTable)) + ";");
		int count = 0;
		
		try {
			rowCount.next();
			count = rowCount.getInt(1);
			rowCount.close();
		}catch(SQLException e) {
			e.printStackTrace();
		}
		
		return count;
	}
	
	/**
	 * Executes a custom sql query on the database.
	 * @param query String that contains the custom sql query.
	 * @return List of records that the database result of the query contains.
	 */
	public List<DatabaseRecord> executeCustomQuery(String query) {
		return this.convertResultSet(this.executeQuery(query));
	}

	/**
	 * Creates test setup for testing purposes.
	 */
	public void createTestSetUp() {
//		this.createRecordTable("testResult");
//		this.createRecordTable("testResult2");
//		this.createRecordTable("simRun_16042018_1116");

		Date tDate1 = new Date();
		Date tDate2 = new Date();
		Date tDate3 = new Date();
		
		Date d1 = new Date();
		Date d2 = new Date();
		Date d3 = new Date();
		Date d4 = new Date();
		Date d5 = new Date();
		Date d6 = new Date();
		Date d7 = new Date();
		try {
			d1 = this.format.parse("01_01_2014");
			d2 = this.format.parse("02_01_2014");
			d3 = this.format.parse("03_01_2014");
			d4 = this.format.parse("04_01_2014");
			d5 = this.format.parse("05_01_2014");
			d6 = this.format.parse("06_01_2014");
			d7 = this.format.parse("07_01_2014");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.createRecordTables(d1, d7, EvaluationTable.GENERAL_EVALUATION_TABLE);

		tDate1.setTime(tDate1.getTime() + 20000);
		tDate2.setTime(tDate2.getTime() - 10000);
		tDate3.setTime(tDate3.getTime() + 30000000);
		
		Object[] a1 = {d1.getTime(),20,30,200.0,180.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a2 = {d2.getTime(), 40, 50, 2000.0, 200.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a3 = {d3.getTime(), 60, 70, 250.0, 300.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a4 = {d4.getTime(), 80, 90, 900.0, 400.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a5 = {d5.getTime(), 100, 110, 300.0, 500.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a6 = {d6.getTime(), 120, 130, 20000.0, 600.0, 40.0, 50.0, 60.0, 890.0};
		Object[] a7 = {d7.getTime(), 140, 150, 3500.0, 700.0, 40.0, 50.0, 60.0, 890.0};
		
		ArrayList<Object> l1 = new ArrayList<Object>();
		ArrayList<Object> l2 = new ArrayList<Object>();
		ArrayList<Object> l3 = new ArrayList<Object>();
		ArrayList<Object> l4 = new ArrayList<Object>();
		ArrayList<Object> l5 = new ArrayList<Object>();
		ArrayList<Object> l6 = new ArrayList<Object>();
		ArrayList<Object> l7 = new ArrayList<Object>();
		
		for(int i=0; i < a1.length; i++) {
			l1.add(a1[i]);
			l2.add(a2[i]);
			l3.add(a3[i]);
			l4.add(a4[i]);
			l5.add(a5[i]);
			l6.add(a6[i]);
			l7.add(a7[i]);
		}

		TableType type = TableType.GENERAL_SIMULATION_MONITORING_TABLE;
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a1), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a2), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a3), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a4), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a5), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a6), type);
//		this.insertRecord("splitTest", new DatabaseRecord(TableType.getTableSchema(type), a7), type);
//		this.insertRecord("splitTest", l2);
//		this.insertRecord("splitTest", l3);
//		this.insertRecord("splitTest", l4);
//		this.insertRecord("splitTest", l5);
//		this.insertRecord("splitTest", l6);
//		this.insertRecord("splitTest", l7);
//		this.commit();
	}
	
	/**
	 * Creates the internal database table name (where for each "public" table name a separate table for each day in the simulation exists) from
	 * the external table name and a timestamp in simulation time.
	 * @param tablePrefix External table name of the table.
	 * @param timestamp Timestamp in simulation time for which records are requested.
	 * @return Internal table name.
	 */
	private String createTableNameFromPrefix(String tablePrefix, long timestamp) {
		Date time = new Date(timestamp);
		
		return tablePrefix + "_" + this.format.format(time);
	}
	
	/**
	 * Creates a union of all tables that exist for an external table name.
	 * @param tablePrefix External table name.
	 * @return Union of all tables that exist for the external table name as sql query.
	 */
	private String createUnionTableQuery(String tablePrefix) {
		ArrayList<String> tableNames = this.getTableCatalog();
		StringBuffer result = new StringBuffer();
		result.append("(");
		
		String cName;
		int counter = 0;
		for(int i=0; i < tableNames.size(); i++) {
			cName = tableNames.get(i);
			if(cName.contains(tablePrefix)) {
				if(counter != 0) {
					result.append(" union ");
				}
				result.append("select * from " + cName);
				counter++;
			}
			
		}
		result.append(")");
		
		return result.toString();
	}
	
	/**
	 * Creates union of all tables that exist for an external table name and contain records from the specified time interval.
	 * @param tablePrefix External table name of the requested table.
	 * @param start Start date of the interval for the union.
	 * @param end End date of the interval for the union.
	 * @return Union of all tables that exist for the external table name and contain records from the specified time interval as sql query.
	 */
	private String createUnionTableQueryBetweenDates(String tablePrefix, long start, long end) {
		Date startDate = new Date(start);
		Date endDate = new Date(end);
		
//		System.out.println(format.format(startDate)+ " " + format.format(endDate));
		
		StringBuffer result = new StringBuffer();
		result.append("(");
		int counter = 0;
		do {
			if(counter != 0) {
				result.append(" union ");
			}
			result.append("select * from " + this.createTableNameFromPrefix(tablePrefix, startDate.getTime()));
			counter++;
			startDate.setTime(startDate.getTime() + 86400000);
		}while(startDate.before(endDate));
		
		if(!result.toString().contains(this.format.format(endDate))) {
			result.append(" union select * from " + this.createTableNameFromPrefix(tablePrefix, end));
		}
		
		return result.toString();
	}

	/**
	 * Executes a sql query on the database.
	 * @param sqlQuery Sql query that is executed as string representation.
	 * @return Database result for the executed query.
	 */
	private ResultSet executeQuery(String sqlQuery) {
		try {
			Statement stm = dbConnection.createStatement();
			ResultSet result = stm.executeQuery(sqlQuery);
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Updates the maps of prepared statements for a specified database table.
	 * @param tableName Name of the table for which the prepared statements are updated.
	 */
	private void updatePreparedStatements(String tableName) {
		EvaluationTable evaluationTable = EvaluationTable.parseFromString(tableName);
//		for(TableType type : TableType.values()) {
//			if(tableName.contains(TableType.getTablePostfix(type))) {
//				evaluationTable = type;
//				break;
//			}
//		}
		
		if (insertStatements.get(tableName) == null) {
			this.prepareInsertStatement(tableName, evaluationTable);
		}
		if (selectAllStatements.get(tableName) == null) {
			this.prepareSelectAllStatement(tableName);
		}
		if (selectAllBetweenDates.get(tableName) == null) {
			this.prepareSelectAllBetweenDates(tableName);
		}
	}

	private void prepareInsertStatement(String tableName, EvaluationTable evaluationTable) {
		ArrayList<String> tableNames = new ArrayList<String>();
		ColumnType[] tableSchema = EvaluationTable.getTableSchema(evaluationTable);
		
		if(this.createTablesCalled) {
			for(String name : this.getTableCatalog()) {
				if(name.contains(tableName)) {
					tableNames.add(name);
				}
			}
		}
		else {
			tableNames.add(tableName);
		}
		
		for (String name : tableNames) {
			String first = " (";
			String second = " (";

			for (int i = 0; i < tableSchema.length; i++) {
				first += ColumnType.convertToStringName(tableSchema[i]);
				second += "?";

				if (i + 1 != tableSchema.length) {
					first += ",";
					second += ",";
				}
			}
			first += ") ";
			second += ")";

			String sql = "insert into " + name + first + "values" + second + ";";

			try {
				insertStatements.put(name, dbConnection.prepareStatement(sql));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void prepareSelectAllStatement(String tableName) {
		String sql = "select * from " + this.createUnionTableQuery(tableName) + " ;";
		try {
			selectAllStatements.put(tableName, dbConnection.prepareStatement(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void prepareSelectAllBetweenDates(String tableName) {
		String sql = "select * from " + this.createUnionTableQuery(tableName) + " where timestamp >= ? and timestamp <= ?;";
		try {
			selectAllBetweenDates.put(tableName, dbConnection.prepareStatement(sql));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a database result into a list of DatabaseRecord instances.
	 * @param entries Database result that is converted.
	 * @return List of DatabaseRecord instances that represent all records that are contained in the passed database result.
	 */
	private ArrayList<DatabaseRecord> convertResultSet(ResultSet entries) {
		ArrayList<DatabaseRecord> result = new ArrayList<DatabaseRecord>();
		try {
			while (entries.next()) {
				result.add(new DatabaseRecord(entries));
			}
			entries.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	/**
	 * Sets the variable values in an instance of PreparedStatement.
	 * @param statement Prepared statement for which the variable values are set.
	 * @param cType Value of ColumnType that indicates the column type of the variable value that is set.
	 * @param value Actual value that is set in the prepared statement.
	 * @param index Indicates the index of the variable value that is set in the prepared statement.
	 */
	private void setValueInPreparedStatement(PreparedStatement statement, ColumnType cType, Object value, int index) {
		if(statement != null) {
			try {
				switch(ColumnType.getJavaVarType(cType)) {
				case DOUBLE:
					statement.setDouble(index, (Double)value);
					break;
				case INT:
					statement.setInt(index, (Integer)value);
					break;
				case LONG:
					statement.setLong(index, (Long)value);
					break;
				default:
					break;
				
				}
			}catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates the table schema as sql representation for a value of TableType.
	 * @param evaluationTable Value of TableType that indicates the type schema that is created.
	 * @return Database table schema in sql respresentation.
	 */
	private static String createTableSchema(EvaluationTable evaluationTable) {
		String schema = " (";
		
		ColumnType[] tableSchema = EvaluationTable.getTableSchema(evaluationTable);

		for (int i = 0; i < tableSchema.length; i++) {
			schema += ColumnType.convertToStringName(tableSchema[i]) + " "
					+ ColumnType.getDatabaseVarType(tableSchema[i]);
			if (i + 1 != tableSchema.length) {
				schema += ",";
			}
		}
		schema += ")";

		return schema;
	}
	

}
