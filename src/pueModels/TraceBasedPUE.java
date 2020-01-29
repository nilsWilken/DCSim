package pueModels;

import java.util.ArrayList;
import java.util.TreeMap;

import utilities.PUE;

/**
 * This class implements a trace based PUE model.
 * Thus, it is not actually a model.
 * The class always returns the PUE value that was the last real observed value before the requested 
 * point in simulation time.
 * 
 * @author nilsw
 *
 */
public class TraceBasedPUE implements PUEModel {
	
	/**
	 * Tree structure that stores the PUE values of the utilized PUE trace.
	 */
	private TreeMap<Integer, Double> pueMap;

	@Override
	public double getPUE(int currentTime) {
		if(!this.pueMap.containsKey(currentTime)) {
			Integer lowerKey = this.pueMap.lowerKey(currentTime);
			if(lowerKey != null) {
				return this.pueMap.get(lowerKey);
			}
			else {
				Integer higherKey = this.pueMap.higherKey(currentTime);
				if(higherKey != null) {
					return this.pueMap.get(higherKey);
				}
				else {
					return 1.15;
				}
			}
		}
		else {
			return this.pueMap.get(currentTime);
		}
	}

	/**
	 * Initializes the tree structure that stores the PUE trace values.
	 * @param pues List of PUE trace values.
	 */
	public void initializeModel(ArrayList<PUE> pues) {
		this.pueMap = new TreeMap<Integer, Double>();
		
		for(PUE pue : pues) {
			pueMap.put(pue.getTimestamp(), pue.getPue());
		}
	}
	
}
