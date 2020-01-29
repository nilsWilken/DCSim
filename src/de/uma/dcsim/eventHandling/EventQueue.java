package de.uma.dcsim.eventHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.uma.dcsim.utilities.BatchJob;

/**
 * This class implements a queue datastructure that is used for the events within the simulation framework.
 * @author nilsw
 *
 */
public class EventQueue {	
	/**
	 * Maps that store the scheduled events.
	 */
	private HashMap<EventType, HashMap<Long, ArrayList<Event>>> eventMaps;
	
	/**
	 * Maps that store all handled events.
	 */
	private HashMap<EventType, HashMap<Long, ArrayList<Event>>> handledEventMaps;
	
	/**
	 * Creates empty event list.
	 */
	public EventQueue() {
		this.eventMaps = new HashMap<EventType, HashMap<Long, ArrayList<Event>>>();
		this.handledEventMaps = new HashMap<EventType, HashMap<Long, ArrayList<Event>>>();
		
		HashMap<Long, ArrayList<Event>> cMap;
		for(EventType type : EventType.values()) {
			cMap = new HashMap<Long, ArrayList<Event>>();
			this.eventMaps.put(type, cMap);
			
			cMap = new HashMap<Long, ArrayList<Event>>();
			this.handledEventMaps.put(type, cMap);
		}

	}
	
	/**
	 * Creates event list and schedules submission events for all jobs in the passed list.
	 * @param initialJobs List of all jobs for which a submission event should be scheduled during the initialization of the queue.
	 */
	public EventQueue(ArrayList<BatchJob> initialJobs) {
		this.eventMaps = new HashMap<EventType, HashMap<Long, ArrayList<Event>>>();
		this.handledEventMaps = new HashMap<EventType, HashMap<Long, ArrayList<Event>>>();
		
		HashMap<Long, ArrayList<Event>> cMap;
		for(EventType type : EventType.values()) {
			cMap = new HashMap<Long, ArrayList<Event>>();
			this.eventMaps.put(type, cMap);
			
			cMap = new HashMap<Long, ArrayList<Event>>();
			this.handledEventMaps.put(type, cMap);
		}
		
		BatchJob cJob;
		for(int i=0; i < initialJobs.size(); i++) {
			cJob = initialJobs.get(i);
			this.scheduleEvent(new JobEvent(EventType.JOB_SUBMISSION, cJob.getSubmissionTime(), cJob));
		}
	}
	
	/**
	 * Schedules an event in the event queue.
	 * @param event Event that should be scheduled.
	 */
	public void scheduleEvent(Event event) {
		this.scheduleEvent(event, this.eventMaps.get(event.getType()));
	}
	
	/**
	 * Removes an event from the event queue.
	 * @param event Event that should be removed.
	 */
	public void unscheduleEvent(Event event) {
		this.unscheduleEvent(event, this.eventMaps.get(event.getType()));
	}
	
	/**
	 * Reschedules an event from one point in simulation time to another. The timestamp of the passed event will be automatically adjusted.
	 * @param event Event that should be rescheduled.
	 * @param newTimestamp New point in simulation time at which the event should be scheduled.
	 */
	public void rescheduleEvent(Event event, int newTimestamp) {
		this.rescheduleEvent(event, newTimestamp, this.eventMaps.get(event.getType()));
	}
	
	/**
	 * Moves an event from the event queue to the list of handled events.
	 * @param event Event that should be moved.
	 */
	public void handledEvent(Event event) {
		this.eventHandled(event, this.eventMaps.get(event.getType()), this.handledEventMaps.get(event.getType()));
	}
	
	/**
	 * Retrieves all events of a specific type that are scheduled at a specified point in simulation time.
	 * @param type Type of events that are requested.
	 * @param time Point in simulation time for which the events are requested.
	 * @return List of all events of the specified times that are scheduled at the specified point in simulation time.
	 */
	public List<Event> getEvents(EventType type, int time) {
		return this.eventMaps.get(type).get((long)time);
	}
	
//	/**
//	 * Retrieves all events of a specific type that were at a specified point in simulation time.
//	 * @param type Type of handled events that are requested.
//	 * @param time Point in simulation time for which the handled events are requested.
//	 * @return List of all handled events of the specified times that are scheduled at the specified point in simulation time.
//	 */
//	public List<Event> getHandledEvents(EventType type, int time) {
//		return this.handledEventMaps.get(type).get((long)time);
//	}
	
	/**
	 * Retrieves all events that are scheduled at a specified point in simulation time.
	 * @param timestamp Point in simulation time for which all scheduled events are requested.
	 * @return List of all events that are scheduled at the specified point in simulation time.
	 */
	public List<Event> getAllEvents(long timestamp) {
		ArrayList<Event> result = new ArrayList<Event>();
		
		for(EventType type : EventType.values()) {
			result.addAll(this.eventMaps.get(type).get(timestamp));
		}
		return result;
	}
	
	/**
	 * Removes the specified key from the map that stores the scheduled events.
	 * @param timestamp Key that should be removed from the map.
	 */
	public void removeKey(int timestamp) {
		for(EventType type : EventType.values()) {
			this.eventMaps.get(type).remove((long)timestamp);
		}
	}
	
	/**
	 * Retrieves all timestamps at which events of the specified type are scheduled.
	 * @param type Type of event for which the scheduled times are requested.
	 * @return Set of all points in simulation time at which events of the specified type are scheduled.
	 */
	public Set<Long> getAllKeys(EventType type) {
		return this.eventMaps.get(type).keySet();
	}
	
	private void scheduleEvent(Event event, HashMap<Long, ArrayList<Event>> map) {
		long timestamp = event.getTimestamp();
		ArrayList<Event> eventList = map.get(timestamp);
		
		if(eventList == null) {
			eventList = new ArrayList<Event>();
			eventList.add(event);
			map.put(timestamp, eventList);
		}
		else {
			if(!eventList.contains(event)) {
				eventList.add(event);
			}
		}
	}
		
	private void unscheduleEvent(Event event, HashMap<Long, ArrayList<Event>> map) {
		if(map.get((long)event.getTimestamp()) != null) {
			map.get((long)event.getTimestamp()).remove(event);
		}
	}
	
	private void rescheduleEvent(Event event, int newTimestamp, HashMap<Long, ArrayList<Event>> map) {
		this.unscheduleEvent(event, map);
		
		event.setTimestamp(newTimestamp);
		
		this.scheduleEvent(event, map);
	}
	
	private void eventHandled(Event event, HashMap<Long, ArrayList<Event>> map, HashMap<Long, ArrayList<Event>> handledMap) {
		this.unscheduleEvent(event, map);
		
//		ArrayList<Event> eventList = handledMap.get((long)event.getTimestamp());
		
//		if(eventList == null) {
//			eventList = new ArrayList<Event>();
//			eventList.add(event);
//			handledMap.put(event.getTimestamp().getTime(), eventList);
//		}
//		else {
//			eventList.add(event);
//		}
	}

}
