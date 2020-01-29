package powerModels;

import powerModels.frequencyBasedServerPowerModels.WekaBasedServerPowerModel;
import powerModels.pueBasedHVACPowerModels.PUEBasedHVACPowerModel;
import powerModels.pueBasedHVACPowerModels.SimplePUEBasedHVACPowerModel;


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
	private static PUEBasedHVACPowerModel hvacPowerModel = new SimplePUEBasedHVACPowerModel();
	
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
	public static double getHvacPower(double pue, double itPower) {
		return hvacPowerModel.getCoolingPower(pue, itPower);
	}

}
