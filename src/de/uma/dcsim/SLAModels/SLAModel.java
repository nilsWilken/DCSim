package de.uma.dcsim.SLAModels;

/**
 * This interface should be implemented by all classes that are supposed to be used as SLA model within the simulation framework.
 * 
 * @author nilsw
 *
 */
public interface SLAModel {
	
	/**
	 * Creates a deadline for the provided original start time and the runtime of the job
	 * at its original run configuration.
	 * @param scheduledStartTime Originally scheduled start time.
	 * @param duration Runtime of a job with its original run configuration.
	 * @return SLA deadline in simulation time.
	 */
	public int createDeadline(int scheduledStartTime, double duration, double durationFactor);
	
	/**
	 * Calculates the SLA fee of a job.
	 * @param delay Delay of the job (in % of the original runtime).
	 * @param usagePrice Usage price of the job (total usage price).
	 * @return SLA fee of the job.
	 */
	public double calculateSLAFee(double delay, double usagePrice);

}
