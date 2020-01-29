package eventHandling;

import hardware.Server;

/**
 * This class represents an event that is related to a server.
 * @author nilsw
 *
 */
public class ServerEvent extends Event {

	private Server affectedServer;
	
	public ServerEvent(EventType type, int timestamp, Server affectedServer) {
		super(type, timestamp);
		this.affectedServer = affectedServer;
	}
	
	public Server getAffectedServer() {
		return this.affectedServer;
	}

}
