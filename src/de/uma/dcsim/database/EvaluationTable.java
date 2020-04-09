package de.uma.dcsim.database;

public enum EvaluationTable {
	
	GENERAL_EVALUATION_TABLE,
	DR_REQUEST_RECORD_TABLE,
	FINISHED_JOB_INFO_TABLE;
	
	public static String getTableName(EvaluationTable type) {
		switch(type) {
		case GENERAL_EVALUATION_TABLE:
			return "General_Simulation_Records" + TableType.getTablePostfix(EvaluationTable.getTableType(GENERAL_EVALUATION_TABLE));
		case DR_REQUEST_RECORD_TABLE:
			return "DR_Request_Records" + TableType.getTablePostfix(EvaluationTable.getTableType(DR_REQUEST_RECORD_TABLE));
		case FINISHED_JOB_INFO_TABLE:
			return "Finished_Jobs_Info" + TableType.getTablePostfix(EvaluationTable.getTableType(FINISHED_JOB_INFO_TABLE));
		default:
			return "";
		}
	}
	
	public static TableType getTableType(EvaluationTable table) {
		switch(table) {
		case GENERAL_EVALUATION_TABLE:
			return TableType.GENERAL_SIMULATION_MONITORING_TABLE;
		case DR_REQUEST_RECORD_TABLE:
			return TableType.DR_REQUEST_MONITORING_TABLE;
		case FINISHED_JOB_INFO_TABLE:
			return TableType.FINISHED_JOB_MONITORING_TABLE;
		default:
			return null;
		}
	}
	
	public static EvaluationTable parseFromString(String tableName) {
		for(EvaluationTable table : EvaluationTable.values()) {
			if(EvaluationTable.getTableName(table).equals(tableName)) {
				return table;
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the schema of a table type.
	 * @param evaluationTable Value of TableType that indicates the table type for which the schema is requested.
	 * @return Array of ColumnType that contains the ColumnType values that specify the type of all columns of the requested table type.
	 * The order of the ColumnType values specifies the schema of the requested table type.
	 */
	public static ColumnType[] getTableSchema(EvaluationTable evaluationTable) {
		switch(evaluationTable) {
		case DR_REQUEST_RECORD_TABLE:
			return new ColumnType[] {ColumnType.TIMESTAMP, ColumnType.OPTIMAL_SCALING_FREQUENCY, ColumnType.OPTIMAL_SHIFTING_FRACTION, ColumnType.ACTUALLY_SHIFTED_NODESTEP_FRACTION, ColumnType.MAXIMUM_POWER_PROVISION, 
					ColumnType.AVERAGE_REMAINING_RUNTIME, ColumnType.AVERAGE_NODES_PER_JOB};
		case GENERAL_EVALUATION_TABLE:
			return new ColumnType[] { ColumnType.TIMESTAMP, ColumnType.NUMBER_OF_ACTIVE_NODES, ColumnType.NUMBER_OF_RUNNING_JOBS, ColumnType.TOTAL_EC, ColumnType.IT_POWER, ColumnType.HVAC_EC,
					ColumnType.JOB_POWER, ColumnType.ENERGY_COST, ColumnType.SLA_COST };
		case FINISHED_JOB_INFO_TABLE:
			return new ColumnType[] {ColumnType.TIMESTAMP, ColumnType.JOB_ID, ColumnType.JOB_START_TIME, ColumnType.JOB_FINISH_TIME, ColumnType.JOB_LENGTH_IN_SECONDS, ColumnType.JOB_DELAY_IN_SECONDS, ColumnType.JOB_FREQUENCY};
		default:
			return null;
		
		}
	}

}
