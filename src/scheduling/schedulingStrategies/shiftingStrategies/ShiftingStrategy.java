package scheduling.schedulingStrategies.shiftingStrategies;

import java.util.List;

import hardware.DC;
import utilities.BatchJob;

/**
 * This interface should be implemented by any class that is supposed to be used as a strategy for determining the jobs that should be shifted
 * during a DR event.
 * @author nilsw
 *
 */
public interface ShiftingStrategy {
	
	/**
	 * Prepones jobs that are selceted according to the implemented startegy, to adjust the power consumption profile of the DC.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param amountOfNodesToShift Amount of node steps that are requested to be shifted according to the current configuration of the 
	 * power demand flexibility provision techniques. 
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @param schedulingInterval Length of the currently scheduled DR event in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 * @return Amount of node steps that were actually shifted.
	 */
	public int preponeJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int amountOfNodesToShift, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Postpone jobs that are selceted according to the implemented startegy, to adjust the power consumption profile of the DC.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param amountOfNodesToShift Amount of node steps that are requested to be shifted according to the current configuration of the 
	 * power demand flexibility provision techniques. 
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @param schedulingInterval Length of the currently scheduled DR event in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 * @return Amount of node steps that were actually shifted.
	 */
	public int postponeJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int amountOfNodesToShift, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Shifts the jobs in the considered interval so that the maximum possible power demand is reached.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @param schedulingInterval Length of the currently scheduled DR event in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 */
	public void scheduleForMaximumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Shifts the jobs in the considered interval so that the minimum possible power demand is reached.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @param schedulingInterval Length of the currently scheduled DR event in simulation time.
	 * @param handledDC DC to which the scheduler that uses this strategy belongs.
	 */
	public void scheduleForMinimumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingInterval, DC handledDC);
	
	/**
	 * Retrieves a list of all jobs that are postponable in the currently considered interval.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @param schedulingIntervalLength Length of the interval for which the power consumption should be adjusted in simulation time.
	 * @return List of all jobs that are actually postponable.
	 */
	public List<BatchJob> getPostponableJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingIntervalLength);
	
	/**
	 * Retrieves a list of all jobs that are preponable in the currently considered interval.
	 * @param submittedJobs List of all jobs that were already submitted to the scheduler (excluding affected submitted Jobs).
	 * @param affectedSubmittedJobs List of all jobs that were already submitted to the scheduler, but were not started at the
	 * point in simulation time that was specified in the workload trace.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param currentTime Point in simulation time at which the interval for which the power adjustment should be adjusted starts.
	 * @return List of all jobs that are actually preponable.
	 */
	public List<BatchJob> getPreponableJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime);
	
}
