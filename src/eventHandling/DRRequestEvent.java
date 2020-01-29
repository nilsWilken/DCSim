package eventHandling;

import utilities.DRRequest;

/**
 * This class represents a DR request related event.
 * @author nilsw
 *
 */
public class DRRequestEvent extends Event {

	private DRRequest drRequest;
	
	public DRRequestEvent(EventType type, int timestamp, DRRequest drRequest) {
		super(type, timestamp);
		this.drRequest = drRequest;
	}
	
	public DRRequest getDRRequest() {
		return this.drRequest;
	}

}
