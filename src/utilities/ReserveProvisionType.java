package utilities;

/**
 * This enum defines several constants that correspond to the possible reserve provision types.
 * 
 * @author nilsw
 *
 */
public enum ReserveProvisionType {

	POSITIVE,
	NEGATIVE;
	
	/**
	 * Parse a constant of ReserveProvisionType from a string representation.
	 * "positive" will result in the POSITIVE value and "negative" in the NEGATIVE value.
	 * @param provisionType Text representation of the reserve provision type.
	 * @return Value of ReserveProvisionType that corresponds to the passed text representation.
	 */
	public static ReserveProvisionType parseFromString(String provisionType) {
		if(provisionType.toLowerCase().equals("positive")) {
			return POSITIVE;
		}
		else if(provisionType.toLowerCase().equals("negative")) {
			return NEGATIVE;
		}
		else {
			return POSITIVE;
		}
	}
}
