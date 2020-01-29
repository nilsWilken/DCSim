package de.uma.dcsim.scheduling.schedulingStrategies.demandFlexStrategies;

import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.scheduling.SchedulingResult;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.ReserveProvisionType;

/**
 * This interface has to be implemented by any class that should be used as a demand flexibility provision scheduling strategy.
 * The schedule is implicitly expressed by the start/restart times of the job instances in the DC. To change the schedule, this class
 * adjusts the start/restart times within the BatchJob instances that are passed within the parameter lists. Thus, the BatchJob instances
 * in the list have to be the same BatchJob instances that the DC uses for execution.
 * 
 * @author nilsw
 *
 */
public interface DemandFlexibilityStrategy {
	
	/**
	 * This method can be used to adjust the current schedule of the DC so that the power consumption profile of the DC
	 * is adjusted to a specified power consumption bound.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param affectedSubmittedJobs List of all currently submitted jobs that were not started at the point in simulation time that 
	 * was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param consumptionBoundCalcBase Total facility power consumption that was the basis of the calculation of the power consumption bound.
	 * @param powerConsumptionBound Power consumption bound.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param provisionType Type of the requested power reserve provision.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 * @return
	 */
	public SchedulingResult adjustPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double consumptionBoundCalcBase, double powerConsumptionBound, int schedulingInterval, int currentTime, ReserveProvisionType provisionType, DC handledDC);
	
	/**
	 * Schedules the specified upcoming interval so that the maximum possible power demand is reached. 
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param affectedSubmittedJobs List of all currently submitted jobs that were not started at the point in simulation time that 
	 * was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMaximumPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC);
	
	/**
	 * Schedules the specified upcoming interval so that the maximum possible power demand, which can be achieved through workload shifting only, is reached. 
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param affectedSubmittedJobs List of all currently submitted jobs that were not started at the point in simulation time that 
	 * was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMaximumPowerConsumptionShiftingOnly(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC);
	
	/**
	 * Schedules the specified upcoming interval so that the maximum possible power demand, which can be achieved through frequency scaling only, is reached.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMaximumPowerConsumptionDVFSOnly(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) ;
	
	/**
	 * Schedules the specified upcoming interval so that the minimum possible power demand is reached.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param affectedSubmittedJobs List of all currently submitted jobs that were not started at the point in simulation time that 
	 * was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMinimumPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC);
	
	/**
	 * Schedules the specified upcoming interval so that the minimum possible power demand, which can be achieved through workload shifting only, is reached.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param affectedSubmittedJobs List of all currently submitted jobs that were not started at the point in simulation time that 
	 * was specified in the workload trace.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMinimumPowerConsumptionShiftingOnly(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC);
	
	/**
	 * Schedules the specified upcoming interval so that the minimum possible power demand, which can be achieved through frequency scaling only, is reached.
	 * @param submittedJobs List of all currently submitted jobs.
	 * @param runningJobs List of all currently running jobs.
	 * @param pausedJobs List of all currently paused jobs.
	 * @param scheduledJobs List of all currently scheduled jobs.
	 * @param schedulingInterval Length of the interval for which the power consumption should be adjusted.
	 * @param currentTime Point in simulation time at which the interval for which the power consumption should be adjusted starts.
	 * @param handledDC DC to which the scheduler that calls this strategy belongs.
	 */
	public void scheduleForMinimumPowerConsumptionDVFSOnly(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC);
}
