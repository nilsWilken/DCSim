package eventHandling;

import utilities.BatchJob;

/**
 * This class represents a event that is related to a batch job.
 * @author nilsw
 *
 */
public class JobEvent extends Event {

	private BatchJob affectedJob;
	
	public JobEvent(EventType type, int timestamp, BatchJob affectedJob) {
		super(type, timestamp);
		this.affectedJob = affectedJob;
	}
	
	public BatchJob getAffectedJob() {
		return this.affectedJob;
	}

}
