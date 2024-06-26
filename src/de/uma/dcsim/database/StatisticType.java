package de.uma.dcsim.database;

/**
 * This enum defines several types of statistics that are available in the SQLite database system.
 * @author nilsw
 *
 */
public enum StatisticType {

	/**
	 * Average statistic.
	 */
	AVERAGE,
	
	/**
	 * Maximum statistic.
	 */
	MAXIMUM,
	
	/**
	 * Minimum statistic.
	 */
	MINIMUM,
	
	/**
	 * Sum statistic.
	 */
	SUM;
	
	/**
	 * Retrieves the name of a value of StatisticType as sql representation.
	 * @param type Value of StatisticType for which the sql name is requested.
	 * @return Name of the requested statistic type in the SQLite database system.
	 */
	public static String getSqLiteName(StatisticType type) {
		switch(type) {
		case AVERAGE:
			return "avg";
		case MAXIMUM:
			return "max";
		case MINIMUM:
			return "min";
		case SUM:
			return "sum";
		default:
			return "";
		}
	}

	public static StatisticType parseFromString(String type) {
		for(StatisticType sType : StatisticType.values()) {
			if(StatisticType.getSqLiteName(sType).equals(type.trim())) {
				return sType;
			}
		}
		return null;
	}
	
//	public static StatisticType parseFromString(String type) {
//		switch(type.toLowerCase().trim()) {
//		case "average":
//			return AVERAGE;
//		case "maximum":
//			return MAXIMUM;
//		case "minimum":
//			return MINIMUM;
//		case "sum":
//			return SUM;
//		default:
//			return null;
//		}
//	}
}
