package de.uma.dcsim.eventHandling;

/**
 * This class represents a cooling related event.
 * @author nilsw
 *
 */
public class CoolingEvent extends Event {
	
	private double newTemperatureSetpoint;

	public CoolingEvent(EventType type, int timestamp, double newTemperatureSetpoint) {
		super(type, timestamp);
		this.newTemperatureSetpoint = newTemperatureSetpoint;
	}
	
	public double getNewTemperatureSetpoint() {
		return this.newTemperatureSetpoint;
	}

}
