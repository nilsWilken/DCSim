package de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities;

import de.uma.dcsim.utilities.BatchJob;
/**
 * This class encapsulate all information that are used by the JobComparator class to sort the jobs according to their theta values (see thesis).
 * 
 * @author nilsw
 *
 */
public class JobSortContainer {

	/**
	 * Job instance.
	 */
	private BatchJob job;
	
	/**
	 * Theta value for the stored job instance.
	 */
	private double theta;
	
	public JobSortContainer(BatchJob job, double theta) {
		this.job = job;
		this.theta = theta;
	}

	public BatchJob getJob() {
		return job;
	}

	public void setJob(BatchJob job) {
		this.job = job;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}
	
}
