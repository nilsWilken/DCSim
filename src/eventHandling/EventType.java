package eventHandling;

/**
 * This enum defines several constants that correspond to the different event types that are available in the simulation framework.
 * @author nilsw
 *
 */
public enum EventType {
	/**
	 * Start of a job.
	 */
	JOB_START,
	
	/**
	 * Finish of a job.
	 */
	JOB_FINISH,
	
	/**
	 * A job gets paused.
	 */
	JOB_PAUSE,
	
	/**
	 * Restart of a job.
	 */
	JOB_RESTART,
	
	/**
	 * Submission of a job.
	 */
	JOB_SUBMISSION,
	
	/**
	 * Increase of the cooling setpoint of the HVAC system. (Currently not used)
	 */
	INCREASE_COOLING_SETPOINT,
	
	/**
	 * Decrease of the cooling setpoint of the HVAC system. (Currently not used)
	 */
	DECREASE_COOLING_SETPOINT,
	
	/**
	 * Update of a server.
	 */
	SERVER_UPDATE,
	
	/**
	 * DR request received.
	 */
	DR_REQUEST;
}
