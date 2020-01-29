package de.uma.dcsim.database;

/**
 * This enum defines different types of tables that can be used to store monitoring values of the simulation.
 * @author nilsw
 *
 */
public enum TableType {
	
	/**
	 * Table type for general simulation values (e.g., power consumption related values, SLA costs, energy costs)
	 */
	GENERAL_SIMULATION_MONITORING_TABLE,
	
	/**
	 * Table type for DR request response monitoring values (e.g., used shifting fraction, used scaling frequency)
	 */
	DR_REQUEST_MONITORING_TABLE;
	
	
	public static TableType parseTableTypeFromString(String type) {
		String[] split = type.split("\\_");
		switch(split[split.length-1].toLowerCase().trim()) {
		case "gsm":
			return GENERAL_SIMULATION_MONITORING_TABLE;
		case "drm":
			return DR_REQUEST_MONITORING_TABLE;
		default:
			return null;
		}
	}
	
	/**
	 * Retrieves a string that uniquely identifies a requested table type.
	 * @param tableType Value of TableType that indicates the type of table for which the unique identifier is requested.
	 * @return Unique string identifier of the requested table type.
	 */
	public static String getTablePostfix(TableType tableType) {
		switch(tableType) {
		case DR_REQUEST_MONITORING_TABLE:
			return "_DRM";
		case GENERAL_SIMULATION_MONITORING_TABLE:
			return "_GSM";
		default:
			return null;
		}
	}
	
//	/**
//	 * Retrieves the schema of a table type.
//	 * @param tableType Value of TableType that indicates the table type for which the schema is requested.
//	 * @return Array of ColumnType that contains the ColumnType values that specify the type of all columns of the requested table type.
//	 * The order of the ColumnType values specifies the schema of the requested table type.
//	 */
//	public static ColumnType[] getTableSchema(TableType tableType) {
//		switch(tableType) {
//		case DR_REQUEST_MONITORING_TABLE:
//			return new ColumnType[] {ColumnType.TIMESTAMP, ColumnType.OPTIMAL_SCALING_FREQUENCY, ColumnType.OPTIMAL_SHIFTING_FRACTION, ColumnType.ACTUALLY_SHIFTED_NODESTEP_FRACTION, ColumnType.MAXIMUM_POWER_PROVISION, 
//					ColumnType.AVERAGE_REMAINING_RUNTIME, ColumnType.AVERAGE_NODES_PER_JOB};
//		case GENERAL_SIMULATION_MONITORING_TABLE:
//			return new ColumnType[] { ColumnType.TIMESTAMP, ColumnType.NUMBER_OF_ACTIVE_NODES, ColumnType.NUMBER_OF_RUNNING_JOBS, ColumnType.TOTAL_EC, ColumnType.IT_POWER, ColumnType.HVAC_EC,
//					ColumnType.JOB_POWER, ColumnType.ENERGY_COST, ColumnType.SLA_COST };
//		default:
//			return null;
//		
//		}
//	}
	
//	/**
//	 * Retrieves a unique column of a table type.
//	 * @param tableType Value of TableType that indicates the table type for which the unique column is requested.
//	 * @return Value of ColumnType that indicates the column that is unique for the requested table type.
//	 */
//	public static ColumnType getTypeUniqueColumn(TableType tableType) {
//		switch(tableType) {
//		case DR_REQUEST_MONITORING_TABLE:
//			return ColumnType.OPTIMAL_SCALING_FREQUENCY;
//		case GENERAL_SIMULATION_MONITORING_TABLE:
//			return ColumnType.JOB_POWER;
//		default:
//			return null;
//		
//		}
//	}

}
