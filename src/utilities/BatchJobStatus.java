package utilities;

/**
 * This enum defines several constants that correspond to the different states that a batch job can have.
 * 
 * @author nilsw
 *
 */
public enum BatchJobStatus {
	
	PARSED,
	SUBMITTED,
	SCHEDULED,
	RESCHEDULED,
	RUNNING,
	PAUSED,
	FINISHED;

}
