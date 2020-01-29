package energyPriceModels;

import java.util.ArrayList;
import java.util.TreeMap;

import utilities.EnergyPrice;

/**
 * This class implements the EnergyPriceModel interface and thus can be used as energy price model. The class uses a 
 * energy price trace to determine the returned energy prices.
 * @author nilsw
 *
 */
public class TraceBasedEnergyPrice implements EnergyPriceModel {
	
	/**
	 * Tree structure that stores all energy prices.
	 */
	private TreeMap<Integer, Double> priceMap;
	
	public double getEnergyPriceInCentPerKWH(int currentTime) {
		//If the requested key is not contained in the tree
		if(!this.priceMap.containsKey(currentTime)) {
			//Corresponds to the key of the energy price that has the next lowest key below the requested key
			Integer lowerKey = this.priceMap.lowerKey(currentTime);
			if(lowerKey != null) {
				return this.priceMap.get(lowerKey);
			}
			else {
				Integer higherKey = this.priceMap.higherKey(currentTime);
				if(higherKey != null) {
					return this.priceMap.get(higherKey);
				}
				//If no next highest and next lowest keys are available
				else {
					return 0.16;
				}
			}
		}
		//If the requested key is directly contained in the tree
		else {
			return this.priceMap.get(currentTime);
		}
	}
	
	/**
	 * Initializes the tree structure that stores the energy prices.
	 * @param energyPrices List of the energy prices that are stored in the model.
	 */
	public void initializeModel(ArrayList<EnergyPrice> energyPrices) {
		 this.priceMap = new TreeMap<Integer, Double>();
		 
		 for(EnergyPrice price : energyPrices) {
			 priceMap.put(price.getTimestamp(), price.getPrice());
		 }
	}

}
