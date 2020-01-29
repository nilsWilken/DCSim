package de.uma.dcsim.scheduling.schedulingStrategies.dvfsStrategies;

import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.ReserveProvisionType;

/**
 * This interface has to be implemented by any class that is supposed to be used as a strategy that determines the jobs for which
 * the execution frequency is scaled, to reach an adjustment of the power consumption.
 * @author nilsw
 *
 */
public interface DVFSStrategy {

	/**
	 * Scales the execution frequencies of selected jobs to achieve adjustment of the power consumption profile.
	 * @param submittedJobs List of all jobs that are currently submitted to the scheduler component of the DC.
	 * @param runningJobs List of all jobs that currently run.
	 * @param pausedJobs List of all jobs that are currently paused.
	 * @param scheduledJobs List of all jobs that are currentl scheduled.
	 * @param provisionType Requested reserve power provision type.
	 * @param amountOfNodeStepsToScale Amount of node steps that are requested to be scaled.
	 * @param requestedFrequency CPU frequency to which the selected jobs are requested to be scaled.
	 * @param maximumFrequency Maximum possible CPU frequency.
	 * @param minimumFrequency Minimum possible CPU frequency.
	 * @param currentTime Start of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param schedulingInterval Lenght of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 */
	public void scaleFrequencies(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, ReserveProvisionType provisionType, int amountOfNodeStepsToScale, double requestedFrequency, double maximumFrequency, double minimumFrequency, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Adjusts the run configurations of the jobs, so that the maximum possible power consumption demand is reached.
	 * @param submittedJobs List of all jobs that are currently submitted to the scheduler component of the DC.
	 * @param runningJobs List of all jobs that currently run.
	 * @param pausedJobs List of all jobs that are currently paused.
	 * @param scheduledJobs List of all jobs that are currentl scheduled.
	 * @param maximumFrequency Maximum possible CPU frequency.
	 * @param currentTime Start of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param schedulingInterval Lenght of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 */
	public void scheduleForMaximumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double maximumFrequency, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Adjusts the run configurations of the jobs, so that the minimum possible power consumption demand is reached.
	 * @param submittedJobs List of all jobs that are currently submitted to the scheduler component of the DC.
	 * @param runningJobs List of all jobs that currently run.
	 * @param pausedJobs List of all jobs that are currently paused.
	 * @param scheduledJobs List of all jobs that are currentl scheduled.
	 * @param minimumFrequency Maximum possible CPU frequency.
	 * @param currentTime Start of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param schedulingInterval Lenght of the interval for which the power consumption profile should be adjusted in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 */
	public void scheduleForMinimumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double minimumFrequency, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Returns a list that contains all jobs for which a change of the execution frequency would have an impact onto the power consumption
	 * profile in the specified interval.
	 * @param allJobs List of all jobs.
	 * @param intervalEnd Point in simulation time at which the interval for which the power consumption profile should be adjusted ends.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption profile should be adjusted starts.
	 * @return List of jobs for which the change of the execution frequency would have an impact onto the power consumption profile of the DC
	 * in the specified interval.
	 */
	public List<BatchJob> getScalableJobs(List<BatchJob> allJobs, int intervalEnd, int currentTime);
}
