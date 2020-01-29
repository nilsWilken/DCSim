package de.uma.dcsim.scheduling.schedulingStrategies.demandFlexStrategies;

import java.util.ArrayList;
import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.scheduling.SchedulingResult;
import de.uma.dcsim.scheduling.schedulingStrategies.dvfsStrategies.DVFSStrategy;
import de.uma.dcsim.scheduling.schedulingStrategies.optimization.LPOptimizer;
import de.uma.dcsim.scheduling.schedulingStrategies.shiftingStrategies.ShiftingStrategy;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobStatus;
import de.uma.dcsim.utilities.ReserveProvisionType;

/**
 * This class implements a scheduling strategy that can be used by the scheduler component of the DC
 * to schedule the power adjustment that is required during a DR event.
 * 
 * @author nilsw
 *
 */
public class DemandFlexibilitySchedulingStrategy implements DemandFlexibilityStrategy {
	
	/**
	 * Strategy that is used to determine the jobs for which the execution frequency is scaled.
	 */
	protected DVFSStrategy dvfsStrategy;
	
	/**
	 * Strategy that is used to determine the jobs that are shifted.
	 */
	protected ShiftingStrategy shiftingStrategy;
	
	/**
	 * Minimum possible frequency (GHz) to which the CPUs can be scaled.
	 */
	protected double minimumFrequency;
	
	/**
	 * Maximum possible frequency (GHz) to which the CPUs can be scaled.
	 */
	protected double maximumFrequency;
	
	/**
	 * Solver to optimize the schedule within a DR event.
	 */
	private LPOptimizer lpSolver;
	
	/**
	 * Fraction of the shiftable amount of node steps that will be shifted.
	 */
	private double shiftingPowerFraction;
	
	/**
	 * Frequency to which the execution frequency is scaled for the scaled jobs.
	 */
	private double scalingFrequency;
	
	/**
	 * Indicates how much node steps were shifted through the previously tested configuration of shifting fraction and scaling frequency.
	 */
	private int previouslyShiftedNodeSteps;
	
	public DemandFlexibilitySchedulingStrategy(DVFSStrategy dvfsStrategy, ShiftingStrategy shiftingStrategy, double minimumFrequency, double maximumFrequency, double shiftingPowerFraction, double scalingFrequency) {
		this.dvfsStrategy = dvfsStrategy;
		this.shiftingStrategy = shiftingStrategy;
		this.minimumFrequency = minimumFrequency;
		this.maximumFrequency = maximumFrequency;
		this.lpSolver = new LPOptimizer();
		this.shiftingPowerFraction = shiftingPowerFraction;
		this.scalingFrequency = scalingFrequency;
		this.previouslyShiftedNodeSteps = -1;
	}
	
	public SchedulingResult adjustPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double consumptionBoundCalcBase, double powerConsumptionBound, int schedulingInterval, int currentTime, ReserveProvisionType provisionType, DC handledDC) {
		int amountOfNodeStepsToShift;
		List<BatchJob> allJobs = new ArrayList<BatchJob>(submittedJobs);
		allJobs.addAll(affectedSubmittedJobs);
		allJobs.addAll(runningJobs);
		allJobs.addAll(pausedJobs);
		allJobs.addAll(scheduledJobs);
		
		//If positive reserve power is requested
		if(provisionType == ReserveProvisionType.POSITIVE) {

			//Calculate amount of node steps that have to be shifted according to the current configuration
			amountOfNodeStepsToShift = this.getNodeAmountToPostpone(this.shiftingStrategy.getPostponableJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval), currentTime, currentTime+schedulingInterval);
			
			//Actually shift the workload
			int shiftedNodeSum = this.shiftingStrategy.postponeJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, amountOfNodeStepsToShift, currentTime, schedulingInterval, handledDC);	
			
			//If the actually shifted amount of node steps equals the amount of node steps that were shifted for the previously tested configuration
			if(shiftedNodeSum == this.previouslyShiftedNodeSteps) {
				return new SchedulingResult(false, -1, -1);
			}
			
			//Scale execution frequencies
			this.dvfsStrategy.scaleFrequencies(submittedJobs, runningJobs, pausedJobs, scheduledJobs, ReserveProvisionType.POSITIVE, -1, this.getScalingFrequency(), this.maximumFrequency, this.minimumFrequency, currentTime, schedulingInterval, handledDC);
			
			//Optimize schedule within the DR event
			this.lpSolver.scheduleRelevantJobs(this.dvfsStrategy.getScalableJobs(allJobs, currentTime + schedulingInterval, currentTime), currentTime, currentTime+schedulingInterval, handledDC);
			
			//Determine the maximum amount of reserve power that could have been provided (minimal difference between power profile and consumption bound)
			//and check whether the requested power consumption bound is satisfied
			double minDiff = Double.MAX_VALUE;
			double diff;
			DC boundCheck = handledDC.deepCopy(handledDC.getClock() + schedulingInterval);

			for(int i=0; i < schedulingInterval; i++) {
				boundCheck.updateJobAllocation(true, true);
				diff = consumptionBoundCalcBase - boundCheck.getOverallCurrentPC();
				if(diff < 0) {
					diff = 0;
				}
				if(diff < minDiff) {
					minDiff = diff;
				}
				if(boundCheck.getOverallCurrentPC() > powerConsumptionBound) {
					return new SchedulingResult(false, -1, 0);
				}

			}
			double fraction = 0;
			if(amountOfNodeStepsToShift > 0) {
				fraction = (double)shiftedNodeSum/((double)amountOfNodeStepsToShift/this.shiftingPowerFraction);
			}
			return new SchedulingResult(true, shiftedNodeSum, fraction);
		}
		//If negative reserve power is requested
		else if(provisionType == ReserveProvisionType.NEGATIVE) {
			
			//Calculate amount of node steps that have to be shifted according to the current configuration
			amountOfNodeStepsToShift = this.getNodeAmountToPrepone(this.shiftingStrategy.getPreponableJobs(submittedJobs, affectedSubmittedJobs, pausedJobs, scheduledJobs, currentTime), currentTime, currentTime+schedulingInterval);
			
			//Actually shift workload
			int shiftedNodeSum = this.shiftingStrategy.preponeJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, amountOfNodeStepsToShift, currentTime, schedulingInterval, handledDC);
			
			//Scale execution frequencies
			this.dvfsStrategy.scaleFrequencies(submittedJobs, runningJobs, pausedJobs, scheduledJobs, ReserveProvisionType.NEGATIVE, -1, this.getScalingFrequency(), this.maximumFrequency, this.minimumFrequency, currentTime, schedulingInterval, handledDC);
			
			//Check whether the requested power consumption bound is satisfied
			DC boundCheck = handledDC.deepCopy(handledDC.getClock() + schedulingInterval);
			for(int i=0; i < schedulingInterval; i++) {
				boundCheck.updateJobAllocation(true, true);
				if(boundCheck.getOverallCurrentPC() < powerConsumptionBound) {
					return new SchedulingResult(false, shiftedNodeSum, (double)shiftedNodeSum/((double)amountOfNodeStepsToShift/this.shiftingPowerFraction));
				}
			}
			double fraction = 0;
			if(amountOfNodeStepsToShift > 0) {
				fraction = (double)shiftedNodeSum/((double)amountOfNodeStepsToShift/this.shiftingPowerFraction);
			}
			return new SchedulingResult(true, shiftedNodeSum, fraction);
		}
		
		return new SchedulingResult(false, 0, 0.0);
		
	}
	
	public void scheduleForMaximumPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.shiftingStrategy.scheduleForMaximumPowerDemand(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval, handledDC);
		this.dvfsStrategy.scheduleForMaximumPowerDemand(submittedJobs, runningJobs, pausedJobs, scheduledJobs, this.maximumFrequency, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMaximumPowerConsumptionShiftingOnly(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.shiftingStrategy.scheduleForMaximumPowerDemand(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMaximumPowerConsumptionDVFSOnly(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.dvfsStrategy.scheduleForMaximumPowerDemand(submittedJobs, runningJobs, pausedJobs, scheduledJobs, this.maximumFrequency, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMinimumPowerConsumption(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.shiftingStrategy.scheduleForMinimumPowerDemand(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval, handledDC);
		this.dvfsStrategy.scheduleForMinimumPowerDemand(submittedJobs, runningJobs, pausedJobs, scheduledJobs, this.minimumFrequency, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMinimumPowerConsumptionShiftingOnly(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.shiftingStrategy.scheduleForMinimumPowerDemand(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMinimumPowerConsumptionDVFSOnly(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int schedulingInterval, int currentTime, DC handledDC) {
		this.dvfsStrategy.scheduleForMinimumPowerDemand(submittedJobs, runningJobs, pausedJobs, scheduledJobs, this.minimumFrequency, currentTime, schedulingInterval, handledDC);
	}
	
	public double getScalingFrequency() {
		return this.scalingFrequency;
	}
	
	public void setShiftingPowerFraction(double shiftingFraction) {
		this.shiftingPowerFraction = shiftingFraction;
	}
	
	public double getShiftingPowerFraction() {
		return this.shiftingPowerFraction;
	}
	
	public void setScalingFrequency(double scalingFrequency) {
		this.scalingFrequency = scalingFrequency;
	}
	
	public void setPreviouslyShiftedNodeSteps(int shiftedNodeSteps) {
		this.previouslyShiftedNodeSteps = shiftedNodeSteps;
	}
	
	/**
	 * Calculates the postponable amount of node steps in the interval for which the power consumption should be adjusted.
	 * @param postponableJobs List of all jobs that can be postponed.
	 * @param intervalStart Start of the interval for which the power consumption should be adjusted in simulation time.
	 * @param intervalEnd End of the interval for which the power consumption should be adjusted in simulation time.
	 * @return The amount of node steps in the specified interval that are postponable.
	 */
	private int getNodeAmountToPostpone(List<BatchJob> postponableJobs, int intervalStart, int intervalEnd) {
		int nodeAmount = 0;
		
		int tmp = -1;
		int amountOfNodes;
		for(BatchJob job : postponableJobs) {
			if(job.getStatus() == BatchJobStatus.RUNNING) {
				tmp = intervalStart;
			}
			else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
				tmp = job.getStartTime();
			}
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
				tmp = job.getScheduledRestartTime();
			}
			if(tmp != -1) {
				amountOfNodes = job.getAmountOfServers();
				while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
					nodeAmount += amountOfNodes;
					tmp++;
				}
			}
			tmp = -1;
		}
		
		return (int)((double)(nodeAmount*this.shiftingPowerFraction));
	}
	
	/**
	 * Calculates the preponable amount of node steps in the interval for which the power consumption should be adjusted.
	 * @param preponableJobs List of all jobs that can be postponed.
	 * @param intervalStart Start of the interval for which the power consumption should be adjusted in simulation time.
	 * @param intervalEnd End of the interval for which the power consumption should be adjusted in simulation time.
	 * @return The amount of node steps in the specified interval that are preponable.
	 */
	private int getNodeAmountToPrepone(List<BatchJob> preponableJobs, int intervalStart, int intervalEnd) {
		int nodeAmount = 0;
		int intervalLength = intervalEnd - intervalStart;
		
		int amountOfNodes;
		int remainingSteps;
		for(BatchJob job : preponableJobs) {
			remainingSteps = job.getRemainingRuntimeInSimulationTime();
			if(remainingSteps > intervalLength) {
				amountOfNodes = intervalLength * job.getAmountOfServers();
			}
			else {
				amountOfNodes = remainingSteps * job.getAmountOfServers();
			}
			nodeAmount += amountOfNodes;
		}
		
		return nodeAmount;
	}

}
