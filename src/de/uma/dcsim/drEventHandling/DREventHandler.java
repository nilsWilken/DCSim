package de.uma.dcsim.drEventHandling;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.scheduling.Scheduler;
import de.uma.dcsim.scheduling.SchedulingResult;
import de.uma.dcsim.utilities.PowerType;
import de.uma.dcsim.utilities.ReserveProvisionType;

/**
 * This interface should be implemented by all classes that are used as a DREventHandler component within the simulation framework.
 * @author nilsw
 *
 */
public interface DREventHandler {
	
	/**
	 * Determines the maximum reachable power demand flexibility.
	 * @param adjustmentType Value of ReserveProvisionType that indicates the adjustment type for which the maximum possible power demand flexiblity is requested.
	 * @param DRIntervalLength Length of the received DR event window.
	 * @param financialCompensation Financial compensation that is received for the participation in the DR event.
	 * @param powerType Indicates the power type in which the returned maximum possible demand flexiblity is quantified.
	 * @return Maximum possible demand flexibility expressed as a power value.
	 */
	public double getMaximumPossiblePowerDemandFlexibility(ReserveProvisionType adjustmentType, int DRIntervalLength, double financialCompensation, PowerType powerType);
	
	/**
	 * Determines the maximum reachable power demand flexibility.
	 * @param adjustmentType Value of ReserveProvisionType that indicates the adjustment type for which the maximum possible power demand flexiblity is requested.
	 * @param DRIntervalLength Length of the received DR event window.
	 * @param powerType Indicates the power type in which the returned maximum possible demand flexiblity is quantified.
	 * @return Maximum possible demand flexibility expressed as a power value
	 */
	public double getMaximumPossiblePowerDemandFlexibility(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType);
	
	/**
	 * Determines the maximum power demand flexibility which is reachable through only shifting workload.
	 * @param adjustmentType Value of ReserveProvisionType that indicates the adjustment type for which the maximum possible power demand flexiblity is requested.
	 * @param DRIntervalLength Length of the received DR event window.
	 * @param powerType Indicates the power type in which the returned maximum possible demand flexiblity is quantified.
	 * @return Maximum possible demand flexibility expressed as a power value
	 */
	public double getPossiblePowerDemandFlexibilityThroughShiftingOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType);
	
	/**
	 * Determines the maximum power demand flexibility which is reachable through only scaling the execution frequency of the workload.
	 * @param adjustmentType Value of ReserveProvisionType that indicates the adjustment type for which the maximum possible power demand flexiblity is requested.
	 * @param DRIntervalLength Length of the received DR event window.
	 * @param powerType Indicates the power type in which the returned maximum possible demand flexiblity is quantified.
	 * @return Maximum possible demand flexibility expressed as a power value
	 */
	public double getPossiblePowerDemandFlexibilityThroughDVFSOnly(ReserveProvisionType adjustmentType, int DRIntervalLength, PowerType powerType);
	
	/**
	 * Issues a DR request to the DC to which the DREventHandler component belongs.
	 * @param powerAdjustmentHeight Indicates the amount of requested power demand flexibility.
	 * @param adjustmentType Value of ReserveProvisionType that indicates the adjustment type for which the maximum possible power demand flexiblity is requested.
	 * @param DRIntervalLength Length of the received DR event window.
	 * @param financialCompensation Financial compensation that is received for the participation in the DR event.
	 * @return Instance of SchedulingResult that indicates the result of a call that was made to the scheduler to schedule the DR event window.
	 */
	public SchedulingResult issueDemandResponseRequest(double powerAdjustmentHeight, ReserveProvisionType adjustmentType, int DRIntervalLength, double financialCompensation);
	
	/**
	 * Determines the additional costs that are caused by the provision of power demand flexibility.
	 * @param powerAdjustmentHeight Indicates the amount of requested power demand flexibility.
	 * @param adjustmentType Indicates the type of the requested power demand flexibility.
	 * @param DRIntervalLength Specifies the length of the DR event window.
	 * @param compensationReward Defines the height of the financial compensation that is received for the provision of power demand flexiblity.
	 * @param shiftingFraction Indicates the shifting fraction that should be used for the scheduling of the DR event window.
	 * @param DVFSFraction Indicates the scaling frequency that should be used for the scheduling of the DR event window.
	 * @param drDC Specifies an instance of DC that corresponds to a DC for which the adjusted schedule is already set. Can be empty, in that case this will be constructed by the method itself.
	 * @return Costs of the power demand flexibility provision with the current configuration
	 */
	public double determinePowerDemandFlexibilityCost(double powerAdjustmentHeight, ReserveProvisionType adjustmentType, int DRIntervalLength, double compensationReward, double shiftingFraction, double DVFSFraction, DC drDC);
	
	/**
	 * Sets the scheduler variable within the DREventHandler component.
	 * @param dcScheduler Instance of Scheduler that corresponds to the scheduler component of the DC to which the DREventHandler component belongs.
	 */
	public void setDCScheduler(Scheduler dcScheduler);

}
