package de.uma.dcsim.powerModels;

/**
 * This interface has to be implemented by all classes that are supposed to be used as a frequency based server power consumption model.
 * @author nils
 *
 */
public interface FrequencyBasedPowerModel {
	
	/**
	 * Determines the current power consumption of a server dependant on the current execution frequency and the job class of the job
	 * that is currently assigned to the server.
	 * @param frequency Current execution frequency of the server.
	 * @param jobClass Job class of the job that is assigned to the server.
	 * @return Current power consumption of the server.
	 */
	public double getPower(double frequency, int jobClass);

}
