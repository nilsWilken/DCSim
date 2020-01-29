package scheduling;

/**
 * This class is used to encapsule several values that are obtained during the scheduling process.
 * 
 * @author nilsw
 *
 */
public class SchedulingResult {

	/**
	 * Indicates whether the power bound, which was requested for the scheduling process, is fitted by the resulting schedule.
	 */
	private boolean powerBoundFitted;
	
	/**
	 * Amount of node steps that were shifted out of the scheduled interval (only used when a DR event was scheduled).
	 */
	private int shiftedNodeSteps;
	
	/**
	 * Fraction of the total shiftable amount of node steps that was actually shifted (only used when a DR event was scheduled).
	 */
	private double actuallyShiftedNodeFraction;

	public SchedulingResult(boolean powerBoundFitted, int shiftedNodeSteps, double actuallyShiftedNodeFraction) {
		super();
		this.powerBoundFitted = powerBoundFitted;
		this.shiftedNodeSteps = shiftedNodeSteps;
		this.actuallyShiftedNodeFraction = actuallyShiftedNodeFraction;
	}

	public boolean isPowerBoundFitted() {
		return powerBoundFitted;
	}

	public void setPowerBoundFitted(boolean powerBoundFitted) {
		this.powerBoundFitted = powerBoundFitted;
	}

	public int getShiftedNodeSteps() {
		return shiftedNodeSteps;
	}

	public void setShiftedNodeSteps(int shiftedNodeSteps) {
		this.shiftedNodeSteps = shiftedNodeSteps;
	}
	
	public double getActuallyShiftedNodeFraction() {
		return this.actuallyShiftedNodeFraction;
	}
	
	public void setActuallyShiftedNodeFraction(double actuallyShiftedNodeFraction) {
		this.actuallyShiftedNodeFraction = actuallyShiftedNodeFraction;
	}

}
