package de.uma.dcsim.energyPriceModels;

/**
 * Interface that should be implemented by all classes that are used as an energy price model within the simulation framework.
 * @author nilsw
 *
 */
public interface EnergyPriceModel {
	
	/**
	 * Retrieves the energy price at the specific point in simulation time.
	 * @param currentTime Point in simulation time for which the energy price is requested.
	 * @return Energy price at the specified point in simulation time.
	 */
	public double getEnergyPriceInCentPerKWH(int currentTime);

}
