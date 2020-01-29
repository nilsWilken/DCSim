package de.uma.dcsim.energyPriceModels;

import java.util.ArrayList;

import de.uma.dcsim.utilities.EnergyPrice;

/**
 * This class is designed to provide users an easy way to exchange the used energy model.
 * @author nilsw
 *
 */
public class EnergyPriceModelSelector {
	
	/**
	 * Instance of a class that implements the EnergyPriceModel interface and represents the energy price model that should be used for the simulation.
	 */
	private static EnergyPriceModel energyPriceModel = new TraceBasedEnergyPrice();
	
	/**
	 * Retrieves the energy price at a specified point in simulation time.
	 * @param timestamp Point in simulation time for which the energy price is requested.
	 * @return Energy price at the specified point in simulation time.
	 */
	public static double getEnergyPriceInCentPerKWh(int timestamp) {
		return energyPriceModel.getEnergyPriceInCentPerKWH(timestamp);
	}
	
	/**
	 * This method initializes a utilized energy price model. Currently this is only necessary if an instance of TraceBasedEnergyPrice is used.
	 * @param energyPrices List of energy prices that should be used for the simulation.
	 */
	public static void initializeEnergyPriceModel(ArrayList<EnergyPrice> energyPrices) {
		if(energyPriceModel instanceof TraceBasedEnergyPrice) {
			((TraceBasedEnergyPrice) energyPriceModel).initializeModel(energyPrices);
		}
	}

}
