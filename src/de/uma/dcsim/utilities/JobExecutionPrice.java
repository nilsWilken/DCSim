package de.uma.dcsim.utilities;

public class JobExecutionPrice {

	private int startTime;
	private double costs;
	private double frequency;

	public JobExecutionPrice(int startTime, double costs, double frequency) {
		super();
		this.startTime = startTime;
		this.costs = costs;
		this.frequency = frequency;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public double getCosts() {
		return costs;
	}

	public void setCosts(double costs) {
		this.costs = costs;
	}

	public double getFrequency() {
		return frequency;
	}

	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}
	
	public String toString() {
		return "Start Time: " + this.startTime + "\t Costs: " + this.costs + "\t Frequency: " + this.frequency;
	}

}
