package drEventHandling;

import java.util.ArrayList;

import energyPriceModels.EnergyPriceModelSelector;
import hardware.DC;
import powerModels.PowerModelSelector;
import runtimeModels.RuntimeModelSelector;
import scheduling.Scheduler;
import scheduling.SchedulingResult;
import simulationControl.Setup;
import utilities.BatchJob;
import utilities.BatchJobStatus;
import utilities.PowerType;
import utilities.ReserveProvisionType;

/**
 * This class is used as the DREventHandler component of the current implementation of the simulation framework. It implements the DREventHandler interface.
 * @author nilsw
 *
 */
public class SimpleDREventHandler implements DREventHandler {
	
	/**
	 * Scheduler component of the DC to which this DREventHandler belongs.
	 */
	private Scheduler dcScheduler;
	
	/**
	 * Instance of DC that corresponds to the DC to which this DREventHandler belongs.
	 */
	private DC handledDC;
	
	public SimpleDREventHandler(Scheduler scheduler, DC handledDC) {
		this.dcScheduler = scheduler;
		this.handledDC = handledDC;
	}

	@Override
	public double getMaximumPossiblePowerDemandFlexibility(ReserveProvisionType adjustmentType, int DRIntervalLength, double compensationReward, PowerType powerType) {
		double maximumPowerDemandFlexibility = this.determineMaximumPowerDemandFlexibility(adjustmentType, DRIntervalLength, powerType);
		return maximumPowerDemandFlexibility;
	}

	@Override
	public double getMaximumPossiblePowerDemandFlexibility(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		return this.getMaximumPossiblePowerDemandFlexibility(adjustmentType, DRIntervalLength, 0, powerType);
	}
	
	public double getPossiblePowerDemandFlexibilityThroughShiftingOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		return this.determineMaximumPowerDemandFlexibilityShiftingOnly(adjustmentType, DRIntervalLength, powerType);
	}
	
	public double getPossiblePowerDemandFlexibilityThroughDVFSOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		return this.determineMaximumPowerDemandFlexibilityDVFSOnly(adjustmentType, DRIntervalLength, powerType);
	}

	@Override
	public SchedulingResult issueDemandResponseRequest(double powerAdjustmentHeight, ReserveProvisionType adjustmentType, int DRIntervalLength, double financialCompensation) {
		//For the case that one of the two variable is the MAX/MIN value, this indicates that all possible configurations of the power demand flexibility provision techniques should be used and 
		//this check is not necessary.
		if(powerAdjustmentHeight != Double.MAX_VALUE && powerAdjustmentHeight != Double.MIN_VALUE) {
			
			//If requested power adjustment height is above the maximum reachable power demand flexibility
			if(powerAdjustmentHeight > this.getMaximumPossiblePowerDemandFlexibility(adjustmentType, DRIntervalLength, PowerType.TOTAL_FACILITY_POWER) ) {
				System.out.println("Requested power adjustment height is above maximum power flexibility!");
				return new SchedulingResult(false, 0, 0);
			}
		}
		SchedulingResult schedulingResult = this.dcScheduler.scheduleNextInterval(powerAdjustmentHeight, adjustmentType, DRIntervalLength);
		
		//If the current schedule of the DC fits the requested power boundary
		if(schedulingResult.isPowerBoundFitted()) {
			int nextSchedulerCall = (this.handledDC.getClock()) + DRIntervalLength;
			this.handledDC.setNextSchedulerCall(nextSchedulerCall);
			return schedulingResult;
		}
		else {
			return new SchedulingResult(false, schedulingResult.getShiftedNodeSteps(), schedulingResult.getActuallyShiftedNodeFraction());
		}
	}
	
	public double[] optimizePowerDemandFlexibilityCost(double powerAdjustmentHeight, ReserveProvisionType adjustmentType, int DRIntervalLength, double compensationReward) {
		DC cCopy;
		double cCost;
		double minI = 0;
		double minJ = 0;
		double minimumCost = Double.MAX_VALUE;
		double shiftingFraction = 0;
		double scalingFrequency = 2.3;
		SimpleDREventHandler cEventHandler;
		int previousNodeSum = -1;
		int currentNodeSum = 0;
		
		//Try all possible combinations of shifting fraction and scaling frequency
		for(int i=0; i <= 100 ; i += 1) {
			shiftingFraction = (double)i/100.0;
			currentNodeSum = -1;

			for(int j=27; j >= 12; j -= 1) {
			
				scalingFrequency = (double)j/10.0;
				
				//Create copy of the handled DC on which the current configuration is tested
				cCopy = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
				cEventHandler = (SimpleDREventHandler) cCopy.getDREventHandler();
				cEventHandler.setShiftingFraction(shiftingFraction);
				cEventHandler.setScalingFrequency(scalingFrequency);
				cEventHandler.setPreviouslyShiftedNodeSteps(previousNodeSum);
				
				//Schedule DR event window for copied DC
				SchedulingResult schedulingResult = cEventHandler.issueDemandResponseRequest(powerAdjustmentHeight, adjustmentType, DRIntervalLength, compensationReward);
				
				//If the amount of shifted node steps equals the amount of shifted node steps from the previously tested configuration (in this case the costs will be exactly the same)
				if(schedulingResult.getActuallyShiftedNodeFraction() == -1 && schedulingResult.getShiftedNodeSteps() == -1) {
					break;
				}
				//If the schedule of the copied DC fits the requested power bound (otherwise this combination is not relevant for the optimization)
				if(schedulingResult.isPowerBoundFitted()) {
					cCost = this.determinePowerDemandFlexibilityCost(powerAdjustmentHeight, adjustmentType, DRIntervalLength, compensationReward, shiftingFraction, scalingFrequency, cCopy);
					//Capture values of optimal configuration
					if(cCost < minimumCost) {
						minimumCost = cCost;
						minI = shiftingFraction;
						minJ = scalingFrequency;
					}
					currentNodeSum = schedulingResult.getShiftedNodeSteps();
					System.out.println(currentNodeSum);
					System.out.println("Shifting fraction: " + shiftingFraction + ", DVFS fraction: " + scalingFrequency + ", cost: " + cCost + "\n");
				}
				
			}
			//If at least one tested configuration combination fitted the requested power consumption bound
			if(currentNodeSum != -1) {
				previousNodeSum = currentNodeSum;
			}
		}
		System.out.println("Minimum cost: " + minimumCost + ", for shifting fraction: " + minI + " and DVFS fraction: " + minJ + "\n");
		return new double[] {minI, minJ};
	}
	
	public double determinePowerDemandFlexibilityCost(double powerAdjustmentHeight, ReserveProvisionType adjustmentType, int DRIntervalLength, double compensationReward, double shiftingFraction, double DVFSFraction, DC drDC) {
		double additionalCost = 0;
		//Copy of the handled DC
		DC comparisonDC = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
		
		//If the drDC parameter is empty
		if(drDC == null) {
			drDC = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
			SimpleDREventHandler handler = (SimpleDREventHandler)drDC.getDREventHandler();
			handler.setShiftingFraction(shiftingFraction);
			handler.setScalingFrequency(DVFSFraction);
			drDC.getDREventHandler().issueDemandResponseRequest(powerAdjustmentHeight, adjustmentType, DRIntervalLength, compensationReward);
		}
		
		//If it is assumed that the DC has future knowledge on the energy price and the upcoming workload
		if (Setup.useFutureKnowledge) {
			//Simulate the DR event window for the comparison DC and the dr DC
			for (int i = 0; i < DRIntervalLength; i++) {
				drDC.scheduleJobs();
				drDC.updateJobAllocation(true, true);
				comparisonDC.scheduleJobs();
				comparisonDC.updateJobAllocation(true, true);
			}

			//Continue simulation of comparison DC and drDC until they are in equal states again
			int counter = 0;
			while (counter <= Setup.maximumRuntime) {
				if (drDC.getRunningJobs().size() == comparisonDC.getRunningJobs().size()
						&& drDC.getOccupiedServer().size() == comparisonDC.getOccupiedServer().size()
						&& drDC.getOverallCurrentPC() == comparisonDC.getOverallCurrentPC()
						&& drDC.getFinishedJobs().size() == comparisonDC.getFinishedJobs().size()) {
					counter++;
				} else {
					counter = 0;
				}

				drDC.scheduleJobs();
				drDC.updateJobAllocation(true, true);
				comparisonDC.scheduleJobs();
				comparisonDC.updateJobAllocation(true, true);
			}
			additionalCost = drDC.getCostsInSpecifiedInterval(this.handledDC.getClock(), drDC.getClock())
					- comparisonDC.getCostsInSpecifiedInterval(this.handledDC.getClock(), comparisonDC.getClock());
			additionalCost -= compensationReward;
		}
		//If no future knowledge is available
		else {
			ArrayList<BatchJob> comparisonRelevantJobs = new ArrayList<BatchJob>(comparisonDC.getAffectedSubmittedJobs());
			comparisonRelevantJobs.addAll(comparisonDC.getSubmittedJobs());
			comparisonRelevantJobs.addAll(comparisonDC.getRunningJobs());
			comparisonRelevantJobs.addAll(comparisonDC.getScheduledJobs());
			comparisonRelevantJobs.addAll(comparisonDC.getPausedJobs());
			
			ArrayList<BatchJob> drRelevantJobs = new ArrayList<BatchJob>(drDC.getAffectedSubmittedJobs());
			drRelevantJobs.addAll(drDC.getSubmittedJobs());
			drRelevantJobs.addAll(drDC.getRunningJobs());
			drRelevantJobs.addAll(drDC.getScheduledJobs());
			drRelevantJobs.addAll(drDC.getPausedJobs());
			
			double comparisonCurrentSLACost = 0;
			double comparisonCausedEnergyCost = 0;
			double drCurrentSLACost = 0;
			double drCausedEnergyCost = 0;
			
			//Calculate additional SLA and energy costs for comparison DC
			for(BatchJob job : comparisonRelevantJobs) {
				comparisonCurrentSLACost += job.getCurrentlyCausedSLACosts(Setup.usagePrice);
				comparisonCausedEnergyCost += this.determineJobEnergyCostUnderDREventImpact(job, EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(this.handledDC.getClock()), DRIntervalLength, job.getOriginalFrequency(), this.handledDC.getClock());
			}
			//Calculate additional SLA and energy costs for dr DC
			for(BatchJob job : drRelevantJobs) {
				drCurrentSLACost += job.getCurrentlyCausedSLACosts(Setup.usagePrice);
				drCausedEnergyCost += this.determineJobEnergyCostUnderDREventImpact(job, EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(this.handledDC.getClock()), DRIntervalLength, job.getOriginalFrequency(), this.handledDC.getClock());
			}
			additionalCost = (drCurrentSLACost+drCausedEnergyCost) - (comparisonCurrentSLACost+comparisonCausedEnergyCost);
			additionalCost -= compensationReward;
		}
		
		return additionalCost;
	}
	
	public void setDCScheduler(Scheduler nScheduler) {
		this.dcScheduler = nScheduler;
	}
	
	/**
	 * Sets the configured shifting fraction in the scheduler component of the DC to which this DREventHandler belongs.
	 * @param shiftingFraction Shifting fraction to set.
	 */
	public void setShiftingFraction(double shiftingFraction) {
		this.dcScheduler.setShiftingPowerFraction(shiftingFraction);
	}
	
	/**
	 * @return Shifting fraction that is currently configured in the scheduler component of the DC to which this DREventHandler belongs.
	 */
	public double getShiftingFraction() {
		return this.dcScheduler.getShiftingPowerFraction();
	}
	
	/**
	 * Sets the configured scaling frequency in the scheduler component of the DC to which this DREventHandler belongs.
	 * @param scalingFrequency Scaling frequency to set.
	 */
	public void setScalingFrequency(double scalingFrequency) {
		this.dcScheduler.setDVFSScalingFrequency(scalingFrequency);
	}
	
	/**
	 * 
	 * @return Scaling frequency that is currently configured in the scheduler component of the DC to which this DREventHandler belongs.
	 */
	public double getScalingFrequency() {
		return this.dcScheduler.getScalingFrequency();
	}
	
	/**
	 * Sets the amount of previously shifted node steps in the scheduler component of the DC to which this DREventHandler belongs.
	 * @param shiftedNodeSteps Amount of shifted nodes steps to set.
	 */
	public void setPreviouslyShiftedNodeSteps(int shiftedNodeSteps) {
		this.dcScheduler.setPreviouslyShiftedNodeStepSum(shiftedNodeSteps);
	}
	
	
	private double determineMaximumPowerDemandFlexibility(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		DC extremeDemandDC = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
		double powerBoundCalcBase = this.handledDC.getOverallCurrentPC();
		
		double[] powerConsumptionDifferences = new double[DRIntervalLength];
		double maximumDemandFlexibility = 0;
		if(adjustmentType == ReserveProvisionType.POSITIVE) {
			extremeDemandDC.scheduleJobsForMinimumPowerDemand(DRIntervalLength);
			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.updateJobAllocation(true, true);

				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOccupiedServerPC();
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				default:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				}
				
			}
		}
		else if(adjustmentType == ReserveProvisionType.NEGATIVE) {
			extremeDemandDC.scheduleJobsForMaximumPowerDemand(DRIntervalLength);
			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.scheduleJobs();
				extremeDemandDC.updateJobAllocation(true, true);
				
				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOccupiedServerPC() - powerBoundCalcBase;
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				default:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				
				}
			}
		}
		
		maximumDemandFlexibility = Double.MAX_VALUE;
		for(double d : powerConsumptionDifferences) {
			if(d < maximumDemandFlexibility) {
				maximumDemandFlexibility = d;
			}
		}
		
		
		return maximumDemandFlexibility;
	}
	
	/**
	 * Determines the additional energy costs that a specific run configuration during a DR event causes.
	 * @param job Job for which the additional energy costs are calculated.
	 * @param energyPrice Energy price that is used to calculate the additional costs.
	 * @param DREventLength Length of the considered DR event in simulation time.
	 * @param frequencyAfterDREvent Execution frequency of the job after the DR event.
	 * @param currentTime Start of the DR event in simulation time.
	 * @return Additional energy costs that are caused by a specific run configuration during a DR event (the run configuration for the DR event has to be 
	 * currently specified in the BatchJob instance).
	 */
	private double determineJobEnergyCostUnderDREventImpact(BatchJob job, double energyPrice, int DREventLength, double frequencyAfterDREvent, int currentTime) {
		int intervalEnd = currentTime+DREventLength;
		if(job.getOriginalFrequency() == job.getFrequency()) {
			double realTimeHours = ((double)job.getRemainingRuntimeInSimulationTime()*(double)Setup.secondsPerSimulationTimestep)/3600.0;
			double kWPerStep = (job.getTotalPowerConsumption()/1000.0);
			return realTimeHours*kWPerStep*energyPrice;
		}
		else {
			int timestepsDuringDREvent = 0;
			int tmp = intervalEnd;
			if(job.getStatus() == BatchJobStatus.RUNNING) {
				tmp = currentTime;
			}
			else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
				tmp = job.getStartTime();
			}
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
				tmp = job.getScheduledRestartTime();
			}
			
			while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
				timestepsDuringDREvent++;
				tmp++;
			}
			
			double drEventRealTime = ((double)timestepsDuringDREvent*(double)Setup.secondsPerSimulationTimestep)/3600.0;
			double drEventkW = (job.getTotalPowerConsumption()/1000.0);
			
			int remainingTimeAfterDREvent = job.getRemainingRuntimeInSimulationTime()-timestepsDuringDREvent;
			remainingTimeAfterDREvent = RuntimeModelSelector.getAdjustedRuntime(frequencyAfterDREvent, job.getFrequency(), remainingTimeAfterDREvent);
			
			double afterEventRealTime = ((double)remainingTimeAfterDREvent*(double)Setup.secondsPerSimulationTimestep)/3600.0;
			double afterEventkW = ((PowerModelSelector.getServerPower(frequencyAfterDREvent, job.getJobClass())*job.getAmountOfServers())/1000.0);
			
			return ((drEventRealTime*drEventkW) + (afterEventRealTime*afterEventkW))*energyPrice;
		}
	}
	
	private double determineMaximumPowerDemandFlexibilityShiftingOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		DC extremeDemandDC = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
		double powerBoundCalcBase = this.handledDC.getOverallCurrentPC();
		
		double[] powerConsumptionDifferences = new double[DRIntervalLength];
		double maximumDemandFlexibility = 0;
		if(adjustmentType == ReserveProvisionType.POSITIVE) {
			extremeDemandDC.scheduleJobsForMinimumPowerDemandShiftingOnly(DRIntervalLength);
			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.updateJobAllocation(true, true);

				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOccupiedServerPC();
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				default:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				}
			}
		}
		else if(adjustmentType == ReserveProvisionType.NEGATIVE) {
			extremeDemandDC.scheduleJobsForMaximumPowerDemandShiftingOnly(DRIntervalLength);

			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.scheduleJobs();
				extremeDemandDC.updateJobAllocation(true, true);

				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOccupiedServerPC() - powerBoundCalcBase;
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				default:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				
				}
			}
		}
		
		maximumDemandFlexibility = Double.MAX_VALUE;
		for(double d : powerConsumptionDifferences) {
			if(d < maximumDemandFlexibility) {
				maximumDemandFlexibility = d;
			}
		}
		
		
		return maximumDemandFlexibility;
	}
	
	private double determineMaximumPowerDemandFlexibilityDVFSOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType) {
		DC extremeDemandDC = this.handledDC.deepCopy(this.handledDC.getClock() + DRIntervalLength);
		double powerBoundCalcBase = this.handledDC.getOverallCurrentPC();
		
		double[] powerConsumptionDifferences = new double[DRIntervalLength];
		double maximumDemandFlexibility = 0;
		if(adjustmentType == ReserveProvisionType.POSITIVE) {
			extremeDemandDC.scheduleJobsForMinimumPowerDemandDVFSOnly(DRIntervalLength);
			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.updateJobAllocation(true, true);

				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOccupiedServerPC();
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				default:
					powerConsumptionDifferences[i] = powerBoundCalcBase - extremeDemandDC.getOverallCurrentPC();
					break;
				}
			}
		}
		else if(adjustmentType == ReserveProvisionType.NEGATIVE) {
			extremeDemandDC.scheduleJobsForMaximumPowerDemandDVFSOnly(DRIntervalLength);

			for(int i=0; i < DRIntervalLength; i++) {
				extremeDemandDC.scheduleJobs();
				extremeDemandDC.updateJobAllocation(true, true);
				
				switch(powerType) {
				case HVAC_POWER:
					break;
				case IT_POWER:
					break;
				case JOB_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOccupiedServerPC() - powerBoundCalcBase;
					break;
				case TOTAL_FACILITY_POWER:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				default:
					powerConsumptionDifferences[i] = extremeDemandDC.getOverallCurrentPC() - powerBoundCalcBase;
					break;
				
				}
			}
		}
		
		maximumDemandFlexibility = Double.MAX_VALUE;
		for(double d : powerConsumptionDifferences) {
			if(d < maximumDemandFlexibility) {
				maximumDemandFlexibility = d;
			}
		}
		
		return maximumDemandFlexibility;
	}

}
