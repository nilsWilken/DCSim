package evaluation;

/**
 * This class defines several constants that represent different types of possible evaluation values.
 * @author nilsw
 *
 */
public enum EvaluationType {
	
	/**
	 * Creates the total some.
	 */
	TOTAL_SUM,
	
	/**
	 * Writes all records into a file.
	 */
	ALL_RECORDS,
	
	/**
	 * Builds time series data on the basis of an aggregated average over a period of time.
	 */
	AGGREGATED_AVERAGE,
	
	/**
	 * Performs the same actions as AGGREGATED_AVERAGE, but the sum statistic is used.
	 */
	AGGREGATED_SUM;
	
//	public static EvaluationType parseFromString(String type) {
//		switch(type.toLowerCase().trim()) {
//		case "totalSum":
//			return TOTAL_SUM;
//		case "allRecords":
//			return ALL_RECORDS;
//		case "aggregatedAverage":
//			return AGGREGATED_AVERAGE;
//		case "aggregatedSum":
//			return AGGREGATED_SUM;
//		default:
//			return ALL_RECORDS;
//		}
//	}
	
	public static String getStringNameFromType(EvaluationType type) {
		switch(type) {
		case AGGREGATED_AVERAGE:
			return "aggregatedAverage";
		case AGGREGATED_SUM:
			return "aggregatedSum";
		case ALL_RECORDS:
			return "allRecords";
		case TOTAL_SUM:
			return "totalSum";
		default:
			return "";
		
		}
	}
	
	public static EvaluationType parseFromString(String type) {
		for(EvaluationType eType : EvaluationType.values()) {
			if(EvaluationType.getStringNameFromType(eType).equals(type.trim())) {
				return eType;
			}
		}
		return null;
	}

}
