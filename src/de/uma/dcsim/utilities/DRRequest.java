package de.uma.dcsim.utilities;

/**
 * This class encapsulates a DR request.
 * 
 * @author nilsw
 *
 */
public class DRRequest {

	/**
	 * Timestamp of the DR request.
	 */
	private int timestamp;
	
	/**
	 * Requested power adjustment height (in W).
	 */
	private double adjustmentHeight;
	
	/**
	 * Requested reserve provision type.
	 */
	private ReserveProvisionType provisionType;
	
	/**
	 * Length of the requested DR event.
	 */
	private int length;
	
	/**
	 * Financial reward that is offered for the response to the DR request.
	 */
	private double reward;

	public DRRequest(int timestamp, double adjustmentHeight, ReserveProvisionType provisionType, int length, double reward) {
		super();
		this.timestamp = timestamp;
		this.adjustmentHeight = adjustmentHeight;
		this.provisionType = provisionType;
		this.length = length;
		this.reward = reward;
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public double getAdjustmentHeight() {
		return adjustmentHeight;
	}

	public void setAdjustmentHeight(double adjustmentHeight) {
		this.adjustmentHeight = adjustmentHeight;
	}

	public ReserveProvisionType getProvisionType() {
		return provisionType;
	}

	public void setProvisionType(ReserveProvisionType provisionType) {
		this.provisionType = provisionType;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getReward() {
		return reward;
	}

	public void setReward(double reward) {
		this.reward = reward;
	}

}
