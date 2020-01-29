package utilities;

/**
 * This class encapsulates an energy price.
 * 
 * @author nilsw
 *
 */
public class EnergyPrice {
	
	/**
	 * Timestamp of the energy price value.
	 */
	private int timestamp;
	
	/**
	 * Actual energy price.
	 */
	private double price;
	
	public EnergyPrice(int timestamp, double price) {
		this.timestamp = timestamp;
		this.price = price;
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}

}
