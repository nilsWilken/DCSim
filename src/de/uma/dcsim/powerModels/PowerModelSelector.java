package de.uma.dcsim.powerModels;

import de.uma.dcsim.powerModels.frequencyBasedServerPowerModels.WekaBasedServerPowerModel;
import de.uma.dcsim.powerModels.itPowerModels.FractionBasedITPowerModel;
import de.uma.dcsim.powerModels.itPowerModels.ITPowerModel;
import de.uma.dcsim.powerModels.pueBasedHVACPowerModels.PUEBasedHVACPowerModel;
import de.uma.dcsim.powerModels.pueBasedHVACPowerModels.SimplePUEBasedHVACPowerModel;
import de.uma.dcsim.simulationControl.Setup;


/**
 * This class is used by the simulation core to retrieve the power consumption of the server and HVAC components.
 * Thus, here is the point where a user is supposed to change the utilized power model in the case that it has to be changed.
 * 
 * @author nilsw
 *
 */

public class PowerModelSelector {
	
	/**
	 * Power model which is used to determine the server power consumption.
	 */
	private static FrequencyBasedPowerModel serverPowerModel = new WekaBasedServerPowerModel();
	
	/**
	 * Power model which is used to determine the HVAC power consumption.
	 */
	private static PUEBasedHVACPowerModel HVACPowerModel = new SimplePUEBasedHVACPowerModel();
	
	/**
	 * ITPowerModel that is used to determine the IT power consumption.
	 */
	private static ITPowerModel itPowerModel = new FractionBasedITPowerModel(Setup.SERVER_IT_POWER_FRACTION);
	
	/**
	 * This method returns the server power consumption according to the selected server power model.
	 * @param frequency Current frequency of the server.
	 * @param jobClass Job class of the job that is currently executed by the server.
	 * @return Current power consumption of the server.
	 */
	public static double getServerPower(double frequency, int jobClass) {
		return serverPowerModel.getPower(frequency, jobClass);
	}
	
	/**
	 * This method returns the HVAC power consumption according to the selected HVAC power model.
	 * @param pue Current PUE of the DC.
	 * @param itPower Current IT power consumption of the DC.
	 * @return Current power consumption of the HVAC infrastructure.
	 */
	public static double getHVACPower(double pue, double itPower) {
		return HVACPowerModel.getCoolingPower(pue, itPower);
	}
	
	/**
	 * Retrieves the current IT power consumption of the DC.
	 * @param serverPower Current total server power consumption of the DC.
	 * @param amountOfRunningJobs Current number of running jobs in the DC.
	 * @param amountOfOccupiedServers Current number of active compute nodes in the DC.
	 * @return Current IT power consumption of the DC.
	 */
	public static double getITPower(double serverPower, int amountOfRunningJobs, int amountOfOccupiedServers) {
		return itPowerModel.getITPower(serverPower, amountOfRunningJobs, amountOfOccupiedServers);
	}

}
