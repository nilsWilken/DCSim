package de.uma.dcsim.runtimeModels;

/**
 * This class is used by the simulation core to obtain the adjusted runtime of BatchJob instances under frequency scaling.
 * Thus, if the used model is supposed to be changed, it should be changed in this class.
 * 
 * @author nilsw
 *
 */
public class RuntimeModelSelector {
	
	/**
	 * RuntimeImpactModel that is used to determine the adjusted runtime of a BatchJob.
	 */
	private static DVFSRuntimeImpactModel runtimeModel = new BetaRuntimeImpactModel(new double[] {0.754,0.775,0.768,0.776,0.774,0.777,0.78,0.793,0.798,0.802,0.806,0.811,0.813,0.815,0.819},
			new double[] {2.6,2.5,2.4,2.3,2.2,2.1,2.0,1.9,1.8,1.7,1.6,1.5,1.4,1.3,1.2}, 2.7);
	
	/**
	 * Retrieves the adjusted remaining runtime of a BatchJob dependant on the new execution frequency and the currently remaining runtime
	 * in simulation time of the job.
	 * @param adjustedFrequency New execution frequency of the job.
	 * @param currentFrequency Current execution frequency of the job.
	 * @param remainingRuntime Currently remaining runtime in simulation time of the job.
	 * @return Adjusted remaining runtime in simulation time of the job.
	 */
	public static int getAdjustedRuntime(double adjustedFrequency, double currentFrequency, int remainingRuntime) {
		return runtimeModel.getAdjustedRuntime(adjustedFrequency, currentFrequency, remainingRuntime);
	}

}
