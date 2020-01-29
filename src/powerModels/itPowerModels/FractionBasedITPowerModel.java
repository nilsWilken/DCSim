package powerModels.itPowerModels;

/**
 * Implements the ITPowerModel interface and thus can be used as an IT power consumption model.
 * This class implements an easy IT power consumption model that calculates the current IT power consumption on the basis of the current
 * server power consumption.
 * @author nilsw
 *
 */
public class FractionBasedITPowerModel implements ITPowerModel {
	
	/**
	 * Average fraction of the IT power consumption of the DC that is caused by the power consumption of all servers in the DC.
	 * For example if 71% of the IT power consumption would be equal to the total server power consumption, this would have to be 0.71.
	 */
	private double serverPowerFraction;
	
	public FractionBasedITPowerModel(double serverPowerFraction) {
		this.serverPowerFraction = serverPowerFraction;
	}

	@Override
	public double getITPower(double serverPower, int amountOfRunningJobs, int amountOfOccupiedServers) {
		return serverPower/this.serverPowerFraction;
	}

}
