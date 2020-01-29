package scheduling;

import java.util.ArrayList;
import java.util.List;

import eventHandling.EventType;
import eventHandling.ServerEvent;
import hardware.DC;
import hardware.Server;
import scheduling.schedulingStrategies.ScheduleForMinimumCost;
import scheduling.schedulingStrategies.SchedulingStrategy;
import scheduling.schedulingStrategies.demandFlexStrategies.DemandFlexibilitySchedulingStrategy;
import scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import scheduling.schedulingStrategies.timeSortScheduling.FirstInFirstOutScheduling;
import scheduling.schedulingStrategies.timeSortScheduling.ShortestTimeToDeadlineFirst;
import simulationControl.Setup;
import utilities.BatchJob;
import utilities.BatchJobStatus;
import utilities.ReserveProvisionType;
/**
 * This class represents the scheduler component within the simulation framework.
 * 
 * @author nilsw
 *
 */
public class Scheduler {
	
	/**
	 * Scheduling strategy that is used to schedule power consumption adjustments during DR events.
	 */
	private DemandFlexibilitySchedulingStrategy demandFlexSchedulingStrategy;
	
	/**
	 * Scheduling strategy that is used to schedule to schedule the jobs when no power adjustments are required.
	 */
	private SchedulingStrategy schedulingStrategy;
	
	/**
	 * DC instance to which this scheduler component belongs.
	 */
	private DC handledDC;
	
	/**
	 * List of all scheduled jobs.
	 */
	private ArrayList<BatchJob> scheduledJobs;
	
	/**
	 * List of all jobs that were submitted to the scheduler component.
	 */
	private ArrayList<BatchJob> submittedJobs;
	
	/**
	 * List of all jobs that were submitted to the scheduler and were not able to be scheduled at the point in simulation
	 * time that is specified in the workload trace (only used when superMUCMode is on).
	 */
	private ArrayList<BatchJob> affectedSubmittedJobs;
	
	
	public Scheduler(DemandFlexibilitySchedulingStrategy schedulingStrategy, DC handledDC) {
		this.demandFlexSchedulingStrategy = schedulingStrategy;
		this.handledDC = handledDC;
		this.scheduledJobs = new ArrayList<BatchJob>();
		this.submittedJobs = new ArrayList<BatchJob>();
//		this.schedulingStrategy = new FirstInFirstOutScheduling();
		this.schedulingStrategy = new ShortestTimeToDeadlineFirst();
//		this.schedulingStrategy = new ScheduleForMinimumCost(86400, 300);
		
//		if(Setup.superMUCMode) {
			this.affectedSubmittedJobs = new ArrayList<BatchJob>();
//		}
	}
	
	/**
	 * Schedules the upcoming scheduling interval. This method is used for both, the scheduling of "normal"
	 * intervals and the scheduling of DR events.
	 * 
	 * @param powerAdjustmentHeight Requested power adjustment height (in comparison to the current power consumption of the DC). 0 when a 
	 * "normal" interval is scheduled.
	 * @param provisionType Requested reserve provision type.
	 * @param schedulingIntervalLength Length of the interval that is supposed to be scheduled.
	 * @return Result of the scheduling process.
	 */
	public SchedulingResult scheduleNextInterval(double powerAdjustmentHeight, ReserveProvisionType provisionType, int schedulingIntervalLength) {
		int currentTime = this.handledDC.getClock();
		
		//If no DR event is scheduled, the frequency of all jobs is set back to their original frequency
		if(powerAdjustmentHeight == 0) {
			ArrayList<BatchJob> tmp = new ArrayList<BatchJob>(this.handledDC.getRunningJobs());
			tmp.addAll(this.scheduledJobs);
			for(BatchJob job : tmp) {
				if(job.getFrequency() != job.getOriginalFrequency()) {
					job.setFrequency(job.getOriginalFrequency());
					if(job.getStatus() == BatchJobStatus.RUNNING) {
						for(Server s : job.getAssignedServers()) {
							this.handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, this.handledDC.getClock(), s));
						}
						this.handledDC.rescheduleEvent(job.getFinishEvent(), job.getCalculatedFinishTime());
					}
				}
			}
		}


		//Calculate power consumption bound
		double powerConsumptionBound = 0;
		double consumptionBoundCalcBase = this.handledDC.getOverallCurrentPC();
		if(provisionType == ReserveProvisionType.POSITIVE) {
			if(powerAdjustmentHeight != Double.MAX_VALUE) {
				powerConsumptionBound = consumptionBoundCalcBase - powerAdjustmentHeight;
			}
			else {
				powerConsumptionBound = Double.MAX_VALUE;
			}
		}
		else if(provisionType == ReserveProvisionType.NEGATIVE) {
			if(powerAdjustmentHeight != Double.MIN_VALUE) {
				powerConsumptionBound = consumptionBoundCalcBase + powerAdjustmentHeight;
			}
			else {
				powerConsumptionBound = Double.MIN_VALUE;
			}
		}

		//job lists
		List<BatchJob> occupationPlanRelevantJobs = new ArrayList<BatchJob>();
		occupationPlanRelevantJobs.addAll(this.scheduledJobs);
		occupationPlanRelevantJobs.addAll(this.handledDC.getRunningJobs());

		//Create node occupation plan, which contains the amounts of nodes that are blocked by the currently running and scheduled workload
		int[] nodeOccupationPlan = SchedulingStrategyUtilities.getNodeOccupationPlan(occupationPlanRelevantJobs, schedulingIntervalLength, currentTime);
		
		
		//If superMUCMode is on, schedule all jobs that were not started at the point in simulation time that was specified in the workload trace
		if(Setup.superMUCMode) {
			this.schedulingStrategy.scheduleNextInterval(this.handledDC.getRunningJobs(), this.affectedSubmittedJobs, this.scheduledJobs, this.handledDC.getPausedJobs(), schedulingIntervalLength, currentTime, nodeOccupationPlan, this.handledDC);
		}
		else {
			if(this.affectedSubmittedJobs.size() > 0) {
				this.schedulingStrategy.scheduleNextInterval(this.handledDC.getRunningJobs(), this.affectedSubmittedJobs, this.scheduledJobs, this.handledDC.getPausedJobs(), schedulingIntervalLength, currentTime, nodeOccupationPlan, this.handledDC);
			}
			if(this.affectedSubmittedJobs.size() == 0) {
				this.schedulingStrategy.scheduleNextInterval(this.handledDC.getRunningJobs(), this.submittedJobs, this.scheduledJobs, this.handledDC.getPausedJobs(), schedulingIntervalLength, currentTime, nodeOccupationPlan, this.handledDC);
		
			}
		}
		
		//If superMUCMode is on, try to schedule the jobs at the point in simulation time that is specified in the workload trace
		if(Setup.superMUCMode) {
//			Retrieve backfilling deadline from the scheduling strategy
			int backfillingDeadline = ((ShortestTimeToDeadlineFirst)this.schedulingStrategy).getBackfillingFinishingDeadline();
//			int backfillingDeadline = ((FirstInFirstOutScheduling)this.schedulingStrategy).getBackfillingFinishingDeadline();
			
			//date object that represents the end of the scheduling interval
			int intervalEnd = currentTime + schedulingIntervalLength;
		
			ArrayList<BatchJob> tmpS = new ArrayList<BatchJob>(this.submittedJobs);
			
			//fill lists for scheduled and queued jobs from the allJobs list
			int tmp;
			boolean scheduled;
			for(BatchJob j : tmpS) {
				if(j.getStartTime() >= currentTime && j.getStartTime() < intervalEnd && j.getStatus() == BatchJobStatus.SUBMITTED) {
					tmp = j.getStartTime();
					scheduled = false;

					while (tmp < intervalEnd) {
						j.setStartTime(tmp);
						if(backfillingDeadline != 0 && j.getStartTime() >= backfillingDeadline) {
							break;
						}
						if (SchedulingStrategyUtilities.checkSchedulingFeasibilityForJob(nodeOccupationPlan, j, currentTime)) {
							SchedulingStrategyUtilities.updateNodeOccupationPlan(nodeOccupationPlan, j, currentTime);

							this.scheduledJobs.add(j);
							this.submittedJobs.remove(j);
							j.setStatus(BatchJobStatus.SCHEDULED);
							this.handledDC.rescheduleEvent(j.getStartEvent(), j.getStartTime());
							scheduled = true;
							if(powerAdjustmentHeight > 0)
							break;
						}
						tmp++;
					}
					if(!scheduled) {
						if(backfillingDeadline == 0) {
							List<BatchJob> relevantJobs = new ArrayList<BatchJob>(this.scheduledJobs);
							relevantJobs.addAll(this.handledDC.getRunningJobs());
							SchedulingStrategyUtilities.determineEarliestTimeForNodeAvailability(relevantJobs, j.getAmountOfServers(), currentTime);
						}
						j.setStartTime(intervalEnd);
						this.submittedJobs.remove(j);
						this.affectedSubmittedJobs.add(j);
					}
				}
			}
		}
		

		
		//schedule and scale jobs until the power consumption fits the specified maximum/minimum
		if(powerAdjustmentHeight > 0) {
			if(!this.checkPowerConsumptionBound(this.handledDC, powerConsumptionBound, provisionType, schedulingIntervalLength)) {
				return this.demandFlexSchedulingStrategy.adjustPowerConsumption(this.submittedJobs, this.affectedSubmittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, consumptionBoundCalcBase, powerConsumptionBound, schedulingIntervalLength, currentTime, provisionType, handledDC);
			}
		}
		return new SchedulingResult(true, 0, 0);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the maximum power demand is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMaximumPowerDemand(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMaximumPowerConsumption(this.submittedJobs, this.affectedSubmittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the maximum power demand, which can be reached only through workload shifting, is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMaximumPowerDemandShiftingOnly(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMaximumPowerConsumptionShiftingOnly(this.submittedJobs, this.affectedSubmittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the maximum power demand, which can be reached only through frequency scaling, is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMaximumPowerDemandDVFSOnly(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMaximumPowerConsumptionDVFSOnly(this.submittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the minimum power demand  is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMinimumPowerDemand(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMinimumPowerConsumption(this.submittedJobs, this.affectedSubmittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the minimum power demand, which can be reached only through workload shifting, is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMinimumPowerDemandShiftingOnly(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMinimumPowerConsumptionShiftingOnly(this.submittedJobs, this.affectedSubmittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	/**
	 * Schedules the upcoming interval of the specified length, so that the minimum power demand, which can be reached only through frequency scaling, is reached.
	 * @param schedulingIntervalLength Length of the interval for that the workload should be scheduled in simulation time.
	 */
	public void scheduleNextIntervalForMinimumPowerDemandDVFSOnly(int schedulingIntervalLength) {
		this.demandFlexSchedulingStrategy.scheduleForMinimumPowerConsumptionDVFSOnly(this.submittedJobs, handledDC.getRunningJobs(), handledDC.getPausedJobs(), this.scheduledJobs, schedulingIntervalLength, this.handledDC.getClock(), handledDC);
	}
	
	public ArrayList<BatchJob> getAffectedSubmittedJobs() {
		return this.affectedSubmittedJobs;
	}
	
	public void addAffectedSubmittedJob(BatchJob affectedSubmittedJob) {
		this.affectedSubmittedJobs.add(affectedSubmittedJob);
	}
	
	public void setAffectedSubmittedJobs(ArrayList<BatchJob> affectedSubmittedJobs) {
		this.affectedSubmittedJobs = affectedSubmittedJobs;
	}
	
	public ArrayList<BatchJob> getSubmittedJobs() {
		return this.submittedJobs;
	}
	
	public void addSubmittedJob(BatchJob submittedJob) {
		this.submittedJobs.add(submittedJob);
	}
	
	public void setSubmittedJobs(ArrayList<BatchJob> submittedJobs) {
		this.submittedJobs = submittedJobs;
	}
	
	public ArrayList<BatchJob> getScheduledJobs() {
		return this.scheduledJobs;
	}
	
	public void setScheduledJobs(ArrayList<BatchJob> scheduledJobs) {
		this.scheduledJobs = scheduledJobs;
	}
	
	/**
	 * Sets the shifting fraction parameter of the power demand flexibility provision configuration of the 
	 * scheduling strategy that is used to schedule DR events.
	 * @param shiftingFraction Shifting fraction that should be configured.
	 */
	public void setShiftingPowerFraction(double shiftingFraction) {
		this.demandFlexSchedulingStrategy.setShiftingPowerFraction(shiftingFraction);
	}
	
	/**
	 * Retrieves the shifting fraction that is currently configured in the scheduling strategy that is used to schedule
	 * DR events.
	 * @return Currently configured shifting fraction.
	 */
	public double getShiftingPowerFraction() {
		return this.demandFlexSchedulingStrategy.getShiftingPowerFraction();
	}

	/**
	 * Sets the scaling frequency parameter of the power demand flexibility provision configuration of the 
	 * scheduling strategy that is used to schedule DR events.
	 * @param scalingFrequency Scaling frequency that should be configured.
	 */
	public void setDVFSScalingFrequency(double scalingFrequency) {
		this.demandFlexSchedulingStrategy.setScalingFrequency(scalingFrequency);
	}
	
	/**
	 * Retrieves the scaling frequency that is currently configured in the scheduling strategy that is used to schedule
	 * DR events.
	 * @return Currently configured scaling frequency.
	 */
	public double getScalingFrequency() {
		return this.demandFlexSchedulingStrategy.getScalingFrequency();
	}
	
	/**
	 * Sets the amount of node steps that were shifted in the previously tried configuration of the power demand flexibility provision.
	 * This method is only used by the DREventHandler component, during the optimization of the additional costs that are caused
	 * by power demand flexibility provision.
	 * @param shiftedNodeSteps Amount of node steps that were shifted in the last tried combination.
	 */
	public void setPreviouslyShiftedNodeStepSum(int shiftedNodeSteps) {
		this.demandFlexSchedulingStrategy.setPreviouslyShiftedNodeSteps(shiftedNodeSteps);
	}
	
	private boolean checkPowerConsumptionBound(DC handledDC, double powerConsumptionBound, ReserveProvisionType provisionType, int schedulingInterval) {
		if(powerConsumptionBound == Double.MAX_VALUE || powerConsumptionBound == Double.MIN_VALUE) {
			return false;
		}
		DC copied = handledDC.deepCopy(this.handledDC.getClock() + schedulingInterval);
		for(int i=0; i < schedulingInterval; i++) {
			copied.scheduleJobs();
			copied.updateJobAllocation(true, true);
			if(provisionType == ReserveProvisionType.POSITIVE) {
				if(copied.getOverallCurrentPC() > powerConsumptionBound) {
					return false;
				}
			}
			else if(provisionType == ReserveProvisionType.NEGATIVE) {
				if(copied.getOverallCurrentPC() < powerConsumptionBound) {
					return false;
				}
			}
		}
		return true;
	}
	
}
