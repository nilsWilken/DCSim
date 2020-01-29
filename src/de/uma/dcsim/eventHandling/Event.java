package de.uma.dcsim.eventHandling;

/**
 * This class represents an event within the simulation framework.
 * @author nilsw
 *
 */
public class Event {

	/**
	 * Type of the event.
	 */
	private EventType type;
	
	/**
	 * Point in simulation time at which the event occurs.
	 */
	private int timestamp;

	public Event(EventType type, int timestamp) {
		super();
		this.type = type;
		this.timestamp = timestamp;
	}

	public EventType getType() {
		return type;
	}

	protected void setType(EventType type) {
		this.type = type;
	}

	public int getTimestamp() {
		return timestamp;
	}

	protected void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

}
