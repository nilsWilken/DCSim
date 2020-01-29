package de.uma.dcsim.runtimeModels;

import java.util.HashMap;

/**
 * This class implements a model that determines the adjusted runtime of a BachJob on the basis of a 
 * measure of memory-boundedness (beta) of the job.
 *
 * @author nilsw
 *
 */
public class BetaRuntimeImpactModel implements DVFSRuntimeImpactModel {
	
	/**
	 * Maximum possible CPU frequency.
	 */
	private double fMax;
	
	/**
	 * Map that stores the beta values for the impact modeling of all (f_max,f) pairs.
	 */
	private HashMap<Double, Double> betaMap;
	
	
	public BetaRuntimeImpactModel(double[] betas, double[] availableFrequencies, double fMax) {
		this.fMax = fMax;
		this.betaMap = new HashMap<Double, Double>();
	
		for(int i=0; i < availableFrequencies.length; i++) {
			this.betaMap.put(availableFrequencies[i], betas[i]);
		}
		this.betaMap.put(this.fMax, 0.0);
	}

	@Override
	public int getAdjustedRuntime(double adjustedFrequency, double currentFrequency, int remainingRuntime) {
		int adjustedRuntime = 0;
		int tFmax = (int)((double)remainingRuntime / this.calculateFraction(currentFrequency));
		
		adjustedRuntime = (int)((double)tFmax * this.calculateFraction(adjustedFrequency));
		
		if(adjustedRuntime < 0) {
			System.out.println("Adjusted Runtime: " + adjustedRuntime);
		}
		
		return adjustedRuntime;
	}
	
	private double calculateFraction(double f) {
		return (this.betaMap.get(f) * ((this.fMax/f) - 1.0)) + 1.0;
	}

}
