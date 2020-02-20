package de.uma.dcsim.database;

/**
 * This class intends to represent the different types of columns that are used by the monitoring database.
 * 
 * @author nilsw
 *
 */

public enum ColumnType {
	
	/**
	 * Timestamp.
	 */
	TIMESTAMP,
	
	/**
	 * Total facility power consumption.
	 */
	TOTAL_EC,
	
	/**
	 * IT power consumption.
	 */
	IT_POWER,
	
	/**
	 * HVAC power consumption.
	 */
	HVAC_EC,
	
	/**
	 * Server/job power consumption.
	 */
	JOB_POWER,
	
	/**
	 * Energy cost.
	 */
	ENERGY_COST,
	
	/**
	 * SLA cost.
	 */
	SLA_COST,
	
	/**
	 * Number of active nodes.
	 */
	NUMBER_OF_ACTIVE_NODES,
	
	/**
	 * Number of running jobs.
	 */
	NUMBER_OF_RUNNING_JOBS,
	
	/**
	 * Optimal scaling frequency (power demand flexibility configuration).
	 */
	OPTIMAL_SCALING_FREQUENCY,
	
	/**
	 * Optimal shifting fraction (power demand flexibility configuration).
	 */
	OPTIMAL_SHIFTING_FRACTION,
	
	/**
	 * Maximum power provision (DR event request response).
	 */
	MAXIMUM_POWER_PROVISION,
	
	/**
	 * Average remaining runtime of jobs that ran at the beginning of a DR event.
	 */
	AVERAGE_REMAINING_RUNTIME,
	
	/**
	 * Average amount of nodes per job of the jobs that ran at the beginning of a DR event.
	 */
	AVERAGE_NODES_PER_JOB,
	
	/**
	 * Fraction of node steps that were actually shifted during a DR event response.
	 */
	ACTUALLY_SHIFTED_NODESTEP_FRACTION,
	
	JOB_LENGTH_IN_SECONDS,
	
	JOB_DELAY_IN_SECONDS,
	
	JOB_START_TIME,
	
	JOB_FINISH_TIME;
	
	/**
	 * This method converts a value of ColumnType to the text name that should be used within the database.
	 * @param type Value of ColumnType for which the text name is requested.
	 * @return Text string name of the ColumnType value that was passed as parameter.
	 */
	public static String convertToStringName(ColumnType type) {
		switch (type) {
		case TIMESTAMP:
			return "timestamp";
		case TOTAL_EC:
			return "totalEC";
		case IT_POWER:
			return "itPower";
		case HVAC_EC:
			return "hvacEC";
		case JOB_POWER:
			return "serverEC";
		case ENERGY_COST:
			return "energyCost";
		case SLA_COST:
			return "slaCost";
		case NUMBER_OF_ACTIVE_NODES:
			return "numberOfActiveNodes";
		case NUMBER_OF_RUNNING_JOBS:
			return "numberOfRunningJobs";
		case OPTIMAL_SCALING_FREQUENCY:
			return "optimalScalingFrequency";
		case OPTIMAL_SHIFTING_FRACTION:
			return "optimalShiftingFraction";
		case MAXIMUM_POWER_PROVISION:
			return "optimalPowerProvision";
		case AVERAGE_REMAINING_RUNTIME:
			return "averageRemainingRuntime";
		case AVERAGE_NODES_PER_JOB:
			return "averageNodesPerJob";
		case ACTUALLY_SHIFTED_NODESTEP_FRACTION:
			return "actuallyShiftedNodestepFraction";
		case JOB_LENGTH_IN_SECONDS:
			return "jobLengthInSeconds";
		case JOB_DELAY_IN_SECONDS:
			return "jobDelayInSeconds";
		case JOB_START_TIME:
			return "jobStartTime";
		case JOB_FINISH_TIME:
			return "jobFinishTime";
		default:
			return "";
		}
	}
	
	public static ColumnType parseFromString(String type) {
		for(ColumnType cType : ColumnType.values()) {
			if(ColumnType.convertToStringName(cType).equals(type.trim())) {
				return cType;
			}
		}
		return null;
	}
	
//	public static ColumnType parseFromString(String type) {
//		switch(type.toLowerCase().trim()) {
//		case "timestamp":
//			return ColumnType.TIMESTAMP;
//		case "totalEC":
//			return ColumnType.TOTAL_EC;
//		case "itPower":
//			return ColumnType.IT_POWER;
//		case "hvacEC":
//			return ColumnType.HVAC_EC;
//		case "serverEC":
//			return ColumnType.JOB_POWER;
//		case "jobPower":
//			return ColumnType.JOB_POWER;
//		case "energyCost":
//			return ColumnType.ENERGY_COST;
//		case "slaCost":
//			return ColumnType.SLA_COST;
//		case "numberOfActiveNodes":
//			return ColumnType.NUMBER_OF_ACTIVE_NODES;
//		case "numberOfRunningJobs":
//			return ColumnType.NUMBER_OF_RUNNING_JOBS;
//		case "optimalScalingFrequency":
//			return ColumnType.OPTIMAL_SCALING_FREQUENCY;
//		case "optimalShiftingFraction":
//			return ColumnType.OPTIMAL_SHIFTING_FRACTION;
//		case "optimalPowerProvision":
//			return ColumnType.MAXIMUM_POWER_PROVISION;
//		case "averageRemainingRuntime":
//			return ColumnType.AVERAGE_REMAINING_RUNTIME;
//		case "averageNodesPerJob":
//			return ColumnType.AVERAGE_NODES_PER_JOB;
//		case "actuallyShiftedNodestepFraction":
//			return ColumnType.ACTUALLY_SHIFTED_NODESTEP_FRACTION;
//		default:
//			return null;
//		}
//	}
	
	/**
	 * Converts a value of ColumnType to the string representation of the datatype that the corresponding database column has in SQL.
	 * All returned strings are valid text representations of datatypes that are available in the SQLite database system.
	 * @param type Value of ColumnType for which the SQL datatype is requested.
	 * @return SQL datatype of the ColumnType that is indicated by the passed parameter as text representation.
	 */
	public static String getDatabaseVarType(ColumnType type) {
		switch(type) {
		case TIMESTAMP:
			return "int(8)";
		case TOTAL_EC:
			return "real";
		case IT_POWER:
			return "real";
		case HVAC_EC:
			return "real";
		case JOB_POWER:
			return "real";
		case ENERGY_COST:
			return "real";
		case SLA_COST:
			return "real";
		case NUMBER_OF_ACTIVE_NODES:
			return "int(4)";
		case NUMBER_OF_RUNNING_JOBS:
			return "int(4)";
		case OPTIMAL_SCALING_FREQUENCY:
			return "real";
		case OPTIMAL_SHIFTING_FRACTION:
			return "real";
		case MAXIMUM_POWER_PROVISION:
			return "real";
		case AVERAGE_REMAINING_RUNTIME:
			return "real";
		case AVERAGE_NODES_PER_JOB:
			return "real";
		case ACTUALLY_SHIFTED_NODESTEP_FRACTION:
			return "real";
		case JOB_LENGTH_IN_SECONDS:
			return "int(8)";
		case JOB_DELAY_IN_SECONDS:
			return "int(8)";
		case JOB_START_TIME:
			return "int(8)";
		case JOB_FINISH_TIME:
			return "int(8)";
		default:
			return "";
		}
	}
	
	/**
	 * Converts a value of ColumnType to a value of VarType, which represents the java datatype of the corresponding database column.
	 * @param type A value of ColumnType that represents the database column for which the java datatype is requested.
	 * @return A value of VarType that represents the java datatype of the database column that corresponds to the value of ColumnType that was passed as parameter.
	 */
	public static VarType getJavaVarType(ColumnType type) {
		switch(type) {
		case TIMESTAMP:
			return VarType.LONG;
		case TOTAL_EC:
			return VarType.DOUBLE;
		case IT_POWER:
			return VarType.DOUBLE;
		case HVAC_EC:
			return VarType.DOUBLE;
		case JOB_POWER:
			return VarType.DOUBLE;
		case ENERGY_COST:
			return VarType.DOUBLE;
		case SLA_COST:
			return VarType.DOUBLE;
		case NUMBER_OF_ACTIVE_NODES:
			return VarType.INT;
		case NUMBER_OF_RUNNING_JOBS:
			return VarType.INT;
		case OPTIMAL_SCALING_FREQUENCY:
			return VarType.DOUBLE;
		case OPTIMAL_SHIFTING_FRACTION:
			return VarType.DOUBLE;
		case MAXIMUM_POWER_PROVISION:
			return VarType.DOUBLE;
		case AVERAGE_REMAINING_RUNTIME:
			return VarType.DOUBLE;
		case AVERAGE_NODES_PER_JOB:
			return VarType.DOUBLE;
		case ACTUALLY_SHIFTED_NODESTEP_FRACTION:
			return VarType.DOUBLE;
		case JOB_LENGTH_IN_SECONDS:
			return VarType.LONG;
		case JOB_DELAY_IN_SECONDS:
			return VarType.LONG;
		case JOB_START_TIME:
			return VarType.LONG;
		case JOB_FINISH_TIME:
			return VarType.LONG;
		default:
			return null;
		}
	}
	
	public static boolean isDateValue(ColumnType type) {
		switch(type) {
		case TIMESTAMP:
			return true;
		case TOTAL_EC:
			return false;
		case IT_POWER:
			return false;
		case HVAC_EC:
			return false;
		case JOB_POWER:
			return false;
		case ENERGY_COST:
			return false;
		case SLA_COST:
			return false;
		case NUMBER_OF_ACTIVE_NODES:
			return false;
		case NUMBER_OF_RUNNING_JOBS:
			return false;
		case OPTIMAL_SCALING_FREQUENCY:
			return false;
		case OPTIMAL_SHIFTING_FRACTION:
			return false;
		case MAXIMUM_POWER_PROVISION:
			return false;
		case AVERAGE_REMAINING_RUNTIME:
			return false;
		case AVERAGE_NODES_PER_JOB:
			return false;
		case ACTUALLY_SHIFTED_NODESTEP_FRACTION:
			return false;
		case JOB_LENGTH_IN_SECONDS:
			return false;
		case JOB_DELAY_IN_SECONDS:
			return false;
		case JOB_START_TIME:
			return true;
		case JOB_FINISH_TIME:
			return true;
		default:
			return false;
		}
	}
	
	
	public static EvaluationTable getEvaluationTable(ColumnType type) {
		for(EvaluationTable table : EvaluationTable.values()) {
			for(ColumnType cType : EvaluationTable.getTableSchema(table)) {
				if(cType == type) {
					return table;
				}
			}
		}
		return null;
	}

}
