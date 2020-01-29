package runtimeModels;

/**
 * This interface has to be implemented by any class that should be used as a BatchJob runtime impact model in the simulation framework.
 * 
 * @author nilsw
 *
 */
public interface DVFSRuntimeImpactModel {
	
	/**
	 * Retrieves the adjusted runtime of a BatchJob dependant on the current remaining runtime and the new execution frequency
	 * of the job.
	 * @param adjustedFrequency New execution frequency of the job.
	 * @param currentFrequency Current execution frequency of the job.
	 * @param remainingRuntime Currently remaining runtime in simulation time of the job.
	 * @return Adjusted remaining runtime of the job in simulation time.
	 */
	public int getAdjustedRuntime(double adjustedFrequency, double currentFrequency, int remainingRuntime);

}
