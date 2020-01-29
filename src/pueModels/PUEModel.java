package pueModels;

/**
 * This interface should be implemented by any class that is supposed to be used as PUE model in the simulation framework.
 * @author nilsw
 *
 */
public interface PUEModel {

	/**
	 * Retrieves the current PUE value of the DC at a specified point in simulation time.
	 * @param currentTime Point in simulation time for which the PUE value is requested.
	 * @return PUE value of the DC at the specified point in simulation time.
	 */
	public double getPUE(int currentTime);
	
}
