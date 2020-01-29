package de.uma.dcsim.scheduling.schedulingStrategies;

import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.utilities.BatchJob;

/**
 * This interface should be implemented by any class that is supposed to be used as a scheduling strategy (for "normal" intervals) within
 * the simulation framework.
 * 
 * @author nilsw
 *
 */
public interface SchedulingStrategy {
	
	/**
	 * Schedules the workload for the upcoming "normal" interval of the specified length in simulation time.
	 * @param runningJobs List of all currently running jobs.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param intervalLength Length of the interval that should be scheduled in simulation time.
	 * @param currentTime Point in time at which the scheduled interval starts.
	 * @param nodeOccupationPlan Array that contains the blocked node sums for each timestep in the scheduled interval.
	 * @param handledDC DC to which the scheduler component that calls this strategy belongs.
	 */
	public void scheduleNextInterval(List<BatchJob> runningJobs, List<BatchJob> submittedJobs, List<BatchJob> scheduledJobs, List<BatchJob> pausedJobs, int intervalLength, int currentTime, int[] nodeOccupationPlan, DC handledDC);

}
