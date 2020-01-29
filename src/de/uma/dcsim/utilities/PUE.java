package de.uma.dcsim.utilities;

/**
 * This class encapsulates a PUE value.
 * 
 * @author nilsw
 *
 */
public class PUE {
	
	/**
	 * Timestamp of the PUE value.
	 */
	private int timestamp;
	
	/**
	 * Actual PUE value.
	 */
	private double pue;
	
	public PUE(int timestamp, double pue) {
		this.timestamp = timestamp;
		this.pue = pue;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public double getPue() {
		return pue;
	}

	public void setPue(double pue) {
		this.pue = pue;
	}

}
