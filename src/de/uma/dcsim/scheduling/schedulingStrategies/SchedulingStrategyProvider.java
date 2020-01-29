package de.uma.dcsim.scheduling.schedulingStrategies;

import de.uma.dcsim.scheduling.schedulingStrategies.demandFlexStrategies.DemandFlexibilitySchedulingStrategy;
import de.uma.dcsim.scheduling.schedulingStrategies.dvfsStrategies.ScaleAllJobsStrategy;
import de.uma.dcsim.scheduling.schedulingStrategies.shiftingStrategies.ShiftLongestTimeToDeadlineFirstStrategy;
/**
 * This class is used by the simulation core to retrieve new instances of DemandFlexibilitySchedulingStrategy, which
 * are used to schedule DR event windows.
 * Thus, if the strategy that should be used is supposed to be changed, this should be changed in this class.
 * 
 * @author nilsw
 *
 */
public class SchedulingStrategyProvider {
	
	/**
	 * Returns a new instance of DemandFlexibilitySchedulingStrategy, that represents the strategy that should 
	 * be used for the scheduling of DR event windows. Note that only the minimum and maximum frequencies.
	 * @return Scheduling strategy that is supposed to be used for the scheduling of DR events within the simulation framework.
	 */
	public static DemandFlexibilitySchedulingStrategy getSchedulingStrategy() {
		return new DemandFlexibilitySchedulingStrategy(new ScaleAllJobsStrategy(), new ShiftLongestTimeToDeadlineFirstStrategy(), 1.2, 2.7, 0.5, 2.0);
	}

}
