package de.uma.dcsim.powerModels.frequencyBasedServerPowerModels;

/**
 * This class implements a server power model of the following form: P_serv = A*f^3 + P_idle.
 * Where A is a fitting parameter that depends on the capacitance of the modeled server and the executed application type, f^3 is the current CPU
 * frequency of the server, and P_idle is the idle power consumption of the server.
 * 
 * @author nilsw
 *
 */
public class ServerPowerModelFrequency {

	/**
	 * Retrieves the current power consumption of a server.
	 * @param scalingConstant Value for capacitance parameter A.
	 * @param maxFrequency Current CPU frequency of the server.
	 * @param idlePower Idle power consumption of the modeled server.
	 * @return Current power consumption of the server.
	 */
	public int getPower(double scalingConstant, double maxFrequency, int idlePower) {
		return (int) ((scalingConstant * Math.pow(maxFrequency, 3)) + idlePower);
	}

}
