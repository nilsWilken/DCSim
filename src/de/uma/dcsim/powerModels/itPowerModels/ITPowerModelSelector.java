//package de.uma.dcsim.powerModels.itPowerModels;
//
///**
// *	This class is used by the simulation core to retrieve the IT power consumption of the DC.
// *	Thus, if this model has to be changed, it should be changed in this class.
// * @author nilsw
// *
// */
//public class ITPowerModelSelector {
//	
//	/**
//	 * ITPowerModel that is used to determine the IT power consumption.
//	 */
//	private static final ITPowerModel itPowerModel = new FractionBasedITPowerModel(0.714286);
//	
//	/**
//	 * Retrieves the current IT power consumption of the DC.
//	 * @param serverPower Current total server power consumption of the DC.
//	 * @param amountOfRunningJobs Current number of running jobs in the DC.
//	 * @param amountOfOccupiedServers Current number of active compute nodes in the DC.
//	 * @return Current IT power consumption of the DC.
//	 */
//	public static double getITPower(double serverPower, int amountOfRunningJobs, int amountOfOccupiedServers) {
//		return itPowerModel.getITPower(serverPower, amountOfRunningJobs, amountOfOccupiedServers);
//	}
//
//}
