package subTimeModels;

import java.util.Date;

/**
 * This interface should be implemented by any class that is supposed to be used as a submission time model
 * within the simulation framework.
 * @author nilsw
 *
 */
public interface SubTimeModel {
	
	/**
	 * Creates a submission time for a provided originally scheduled start time of a job.
	 * @param originalStartTime Original start time of a job in simulation time.
	 * @return Submission time of the job.
	 */
	public Date calculateSubmissionTime(Date originalStartTime);

}
