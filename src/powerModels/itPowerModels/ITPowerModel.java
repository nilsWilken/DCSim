package powerModels.itPowerModels;

/**
 * Interface that should be implemented by all classes that are used as IT power consumption model in the simulation framework.
 * @author nilsw
 *
 */
public interface ITPowerModel {
	
	/**
	 * Determines the current IT power consumption of the DC.
	 * @param serverPower Current power consumption of all servers in the DC.
	 * @param amountOfRunningJobs Current number of jobs that are running.
	 * @param amountOfOccupiedServers Current number of compute nodes that are occupied by jobs.
	 * @return IT power consumption of the DC.
	 */
	public double getITPower(double serverPower, int amountOfRunningJobs, int amountOfOccupiedServers);

}
