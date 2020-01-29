package de.uma.dcsim.eventHandling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.hardware.Server;
import de.uma.dcsim.simulationControl.Setup;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobStatus;
import de.uma.dcsim.utilities.ServerStatus;

/**
 * This class implements the EventHandler component of the simulation framework.
 * @author nilsw
 *
 */
public class EventHandler {
	
	/**
	 * List of currently occupied servers.
	 */
	private ArrayList<Server> occupiedServer;
	
	/**
	 * List of currently idle servers.
	 */
	private ArrayList<Server> idleServer;
	
	
	/**
	 * List of currently running jobs.
	 */
	private ArrayList<BatchJob> runningJobs;
	
	/**
	 * List of currently finished jobs.
	 */
	private ArrayList<BatchJob> finishedJobs;
	
	/**
	 * List of currently paused jobs.
	 */
	private ArrayList<BatchJob> pausedJobs;
	
	/**
	 * Mapping between running jobs and the servers on which they are executed. Where the keys are the ids of the servers 
	 * and the values of the map are the ids of the job that is currently executed by the server that corresponds to a key.
	 */
	private HashMap<Integer, String> serverJobMapping;
	
	/**
	 * Contains the SLA costs that occured during the point in simulation time at which the last call of the EventHandler happened.
	 */
	private double currentSLACost;
	
	/**
	 * DC to which this EventHandler belongs.
	 */
	private DC handledDC;
	
	public EventHandler(DC handledDC) {
		this.handledDC = handledDC;
		this.serverJobMapping = new HashMap<Integer, String>();
		
		this.occupiedServer = new ArrayList<Server>(this.getOccupiedServers(handledDC.getServer()));
		this.idleServer = new ArrayList<Server>(this.getIdleServers(handledDC.getServer()));
		if(!handledDC.isChanged()) {
			this.initJobLists(handledDC);
		}
	}
	
	/**
	 * Handle all events and elapse simulation time in all running and paused jobs.
	 * @param scheduledJobs List of all jobs that are currently scheduled.
	 * @param currentTime Point in simulation time for which the events should be handled.
	 * @param updateRemainingTimes Indicates whether a simulation timestep should be elapsed for the running and paused jobs.
	 */
	public void update(List<BatchJob> scheduledJobs, int currentTime, boolean updateRemainingTimes) {
		if(handledDC.getJobPauseEvents(currentTime) != null) {
			this.handleJobPauseEvents(currentTime, scheduledJobs);
		}
		if(handledDC.getJobRestartEvents(currentTime) != null) {
			this.handleJobRestartEvents(scheduledJobs, currentTime);
		}
		if(handledDC.getJobFinishEvents(currentTime) != null) {
			this.handleJobFinishEvents(currentTime);
		}
		if(handledDC.getJobStartEvents(currentTime) != null) {
			this.handleJobStartEvents(scheduledJobs, currentTime);
		}
		if(handledDC.getJobFinishEvents(currentTime) != null) {
			this.handleJobFinishEvents(currentTime);
		}
		if(this.handledDC.getJobSubmissionEvents(currentTime) != null) {
			this.handleJobSubmissionEvents(currentTime);
		}
		if(updateRemainingTimes) {
			this.elapseSimulationTimestepForJobs(scheduledJobs);
		}
	}
	
	public ArrayList<Server> getOccupiedServer() {
		return this.occupiedServer;
	}
	
	public ArrayList<Server> getIdleServer() {
		return this.idleServer;
	}
	
	public ArrayList<BatchJob> getFinishedJobs() {
		return this.finishedJobs;
	}
	
	public ArrayList<BatchJob> getRunningJobs() {
		return this.runningJobs;
	}
	
	public void setRunningJobs(ArrayList<BatchJob> runningJobs) {
		this.runningJobs = runningJobs;
	}
	
	public ArrayList<BatchJob> getPausedJobs() {
		return this.pausedJobs;
	}
	
	public void setPausedJobs(ArrayList<BatchJob> pausedJobs) {
		this.pausedJobs = pausedJobs;
	}
	
	public HashMap<Integer, String> getServerJobMapping() {
		return this.serverJobMapping;
	}
	
	public void setServerJobMapping(HashMap<Integer, String> serverJobMapping) {
		this.serverJobMapping = serverJobMapping;
	}
	 
	public double getCurrentSLACost() {
		return this.currentSLACost;
	}
	
	/**
	 * Initializes the occuped and idle server lists of the EventHandler component.
	 */
	public void initLists() {
		this.occupiedServer = new ArrayList<Server>(this.getOccupiedServers(this.handledDC.getServer()));
		this.idleServer = new ArrayList<Server>(this.getIdleServers(this.handledDC.getServer()));
	}
	
	/**
	 * Handles all job submission events that are scheduled at the specified point in simulation time.
	 * @param currentTime Point in simulation time for which the job submission events should be handled
	 */
	public void handleJobSubmissionEvents(int currentTime) {
		List<Event> submissionEvents = this.handledDC.getJobSubmissionEvents(currentTime);
		
		BatchJob job;
		boolean startChanged;
		for(Event event : submissionEvents) {
			job = ((JobEvent)event).getAffectedJob();
			if(!this.handledDC.isCopy() && Setup.testLogOutput)
			System.out.println(job.getId() + " submitted at " + currentTime + " !");
			
			startChanged = false;
			job.setStatus(BatchJobStatus.SUBMITTED);
			if(Setup.superMUCMode && job.getStartTime() < (this.handledDC.getNextSchedulerCall())) {
//				System.out.println(this.handledDC + " Start time changed" + " " + job.getStartTime() + " " + currentTime + " " + this.handledDC.getNextSchedulerCall());
				startChanged = true;
			}
			if(Setup.superMUCMode && startChanged) {
				this.handledDC.submitAffectedSubmittedJob(job);
			}
			else {
				this.handledDC.submitParsedJob(job);
			}
		}
		submissionEvents.clear();
	}
	
	/**
	 * Handles all job start events that are scheduled at the specified point in simulation time.
	 * @param scheduledJobs List of all jobs that are currently scheduled. 
	 * @param currentTime Point in simulation time for which the job start events should be handled.
	 */
	private void handleJobStartEvents(List<BatchJob> scheduledJobs, int currentTime) {		
		List<Event> eventList = handledDC.getJobStartEvents(currentTime);
		List<Event> eventListQ = new ArrayList<Event>(eventList);
		
		
		ArrayList<Server> assignedServers;
		BatchJob j;
		for(Event event : eventListQ) {			
			j = ((JobEvent)event).getAffectedJob();
			if(!this.handledDC.isCopy() && Setup.testLogOutput) {
				System.out.println(j.getId() + " started at " + currentTime + " !");
			}
			
//			if(currentTime == 1689900) {
//				System.out.println(j.getId() + " " + j.getStartTime());
//			}
			if(j.getScheduledStartTime() == currentTime && j.getStatus() == BatchJobStatus.SCHEDULED) {
				if(this.idleServer.size() < j.getAmountOfServers()) {
//					for(BatchJob job : this.runningJobs) {
//						System.out.println(job.getId() + " " + job.getStatus() + " " + job.getFinishEvent().getTimestamp() + " " + job.getCalculatedFinishTime() + " " + job.getRemainingRuntimeInSimulationTime() + " " + job.getElapsedRuntimeInSeconds() + " " + job.getFrequency());
//					}
					
//					System.out.println(this.handledDC + " " + currentTime + " Job " + j.getId() + " cannot be started, because there are not enough servers available! " + j.getAmountOfServers() + " " + j.getStatus() + " " + this.idleServer.size() + " " + this.occupiedServer.size() + " " + (this.idleServer.size() + this.occupiedServer.size()));
					j.setStartTime(this.handledDC.getNextSchedulerCall());
					this.handledDC.unscheduleEvent(j.getStartEvent());
					this.handledDC.unscheduleEvent(j.getFinishEvent());
					
					this.handledDC.submitAffectedSubmittedJob(j);
					scheduledJobs.remove(j);
					j.setStatus(BatchJobStatus.SUBMITTED);
					
//					this.handledDC.rescheduleEvent(event, startTime);
//					this.handledDC.rescheduleEvent(j.getFinishEvent(), j.getCalculatedFinishTime());
					continue;
				}
				assignedServers = new ArrayList<Server>();
				Server cIdleServer;
				for(int i=0; i < j.getAmountOfServers(); i++) {
					cIdleServer = this.idleServer.get(0);
					
					cIdleServer.setStatus(ServerStatus.OCCUPIED);
					cIdleServer.setCurrentJob(j);
					this.serverJobMapping.put(cIdleServer.getId(), j.getId());
					this.occupiedServer.add(cIdleServer);
					this.idleServer.remove(cIdleServer);
					
					assignedServers.add(cIdleServer);
					this.handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, cIdleServer));
				}
				
				this.handledDC.rescheduleEvent(j.getFinishEvent(), j.getCalculatedFinishTime());
				j.assignServers(assignedServers);
				
				j.setStatus(BatchJobStatus.RUNNING);
				this.runningJobs.add(j);
				if(!scheduledJobs.remove(j)) {
					System.out.println("Started job was not in scheduled list! " + j.getStatus());
				}
			}
			this.handledDC.handledEvent(event);
		}
	}
	
	/**
	 * Elapses a simulation timestep for all running and paused jobs.
	 * @param scheduledJobs List of all jobs that are currently scheduled. 
	 */
	private void elapseSimulationTimestepForJobs(List<BatchJob> scheduledJobs) {

		ArrayList<BatchJob> tmpR = new ArrayList<BatchJob>(this.runningJobs);
		tmpR.addAll(this.pausedJobs);
		tmpR.addAll(scheduledJobs);

		for (BatchJob j : tmpR) {
			j.elapseOneSimulationTimestep();;
		}
	}
	
	/**
	 * Handles all job finish events that are scheduled at the specified point in simulation time.
	 * @param currentTime Point in simulation time for which the job finish events should be handled.
	 */
	private void handleJobFinishEvents(int currentTime) {
		this.currentSLACost = 0;
		List<Event> jobFinishEventList = handledDC.getJobFinishEvents(currentTime);
		List<Event> jobFinishEventListQ = new ArrayList<Event>(jobFinishEventList);
		
		BatchJob j;
		for(Event event : jobFinishEventListQ) {
			j = ((JobEvent)event).getAffectedJob();
			
			if(!this.handledDC.isCopy() && Setup.testLogOutput)
				System.out.println(j.getId() + " finished at " + currentTime + " !");
			if(j.getStatus() != BatchJobStatus.RUNNING) {
				System.out.println(j.getId() + " " + j.getStatus());
			}
			if(j.getRemainingRuntimeInSimulationTime() > 0) {
				System.out.println(j.getRemainingRuntimeInSimulationTime());
			}
			
			j.setStatus(BatchJobStatus.FINISHED);
			j.setActualFinsihingTime(currentTime);
			this.runningJobs.remove(j);
			this.finishedJobs.add(j);
			
			for(Server s : j.getAssignedServers()) {
				s.setStatus(ServerStatus.IDLE);
				s.setCurrentJob(null);
				this.serverJobMapping.remove(s.getId());
				this.occupiedServer.remove(s);
				this.idleServer.add(s);
				this.handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, s));
			}
			j.assignServers(null);
			this.handledDC.handledEvent(event);
			
			this.currentSLACost += j.calculateSLACosts(Setup.usagePrice);
		}
	}
	
	/**
	 * Handles all job pause events that are scheduled at the specified point in simulation time.
	 * @param scheduledJobs List of all jobs that are currently scheduled. 
	 * @param currentTime Point in simulation time for which the job pause events should be handled.
	 */
	private void handleJobPauseEvents(int currentTime, List<BatchJob> scheduledJobs) {		
		List<Event> eventList = handledDC.getJobPauseEvents(currentTime);
		List<Event> eventListQ = new ArrayList<Event>(eventList);
		
		BatchJob j;
		for(Event event : eventListQ) {
			j = ((JobEvent)event).getAffectedJob();
			
			if(!this.handledDC.isCopy() && Setup.testLogOutput)
			System.out.println(j.getId() + " paused at " + currentTime + " !");
//			if(j.getStatus() == BatchJobStatus.FINISHED || j.getStatus() == BatchJobStatus.PAUSED) {
//				continue;
//			}
//			System.out.println(this.handledDC + " " + j.getId() + " pause event!");
			if(j.getStatus() != BatchJobStatus.RUNNING) {
				System.out.println(j.getId() + " " + j.getStatus() + " " + currentTime);
			}
			
			if(j.isAlreadyRescheduled()) {
				j.setStatus(BatchJobStatus.RESCHEDULED);
				scheduledJobs.add(j);
			}
			else {
				j.setStatus(BatchJobStatus.PAUSED);
				j.setScheduledRestartTime(this.handledDC.getNextSchedulerCall());
				this.pausedJobs.add(j);
			}
			
			
			j.setIsRequestedToPause(false);
			j.setIsAlreadyRescheduled(false);
			this.handledDC.unscheduleEvent(j.getFinishEvent());
			this.runningJobs.remove(j);
			
			for(Server s : j.getAssignedServers()) {
				s.setStatus(ServerStatus.IDLE);
				s.setCurrentJob(null);
				this.serverJobMapping.remove(s.getId());
				this.occupiedServer.remove(s);
				this.idleServer.add(s);
				this.handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, s));
			}
			j.assignServers(null);
			this.handledDC.handledEvent(event);
		}
	}
	
	/**
	 * Handles all job restart events that are scheduled at the specified point in simulation time.
	 * @param scheduledJobs List of all jobs that are currently scheduled. 
	 * @param currentTime Point in simulation time for which the job restart events should be handled.
	 */
	private void handleJobRestartEvents(List<BatchJob> scheduledJobs, int currentTime) {
		List<Event> eventList = handledDC.getJobRestartEvents(currentTime);
		List<Event> eventListQ = new ArrayList<Event>(eventList);
		
		ArrayList<Server> assignedServers;
		BatchJob job;
		for(Event event : eventListQ) {
			job = ((JobEvent)event).getAffectedJob();
			
			if(!this.handledDC.isCopy() && Setup.testLogOutput)
			System.out.println(job.getId() + " restarted at " + currentTime + " !");
			
			if(job.getStatus() == BatchJobStatus.RESCHEDULED && job.getScheduledRestartTime() == currentTime) {
				if(this.idleServer.size() < job.getAmountOfServers()) {
//					System.out.println(this.handledDC + " " + currentTime + " Job " + job.getId() + " cannot be restarted, because there are not enough servers available! " + job.getAmountOfServers() + " " + job.getStatus() + " " + this.idleServer.size() + " " + this.occupiedServer.size() + " " + (this.idleServer.size() + this.occupiedServer.size()));
					
					job.setScheduledRestartTime(this.handledDC.getNextSchedulerCall());
					scheduledJobs.remove(job);
					this.pausedJobs.add(job);
					job.setStatus(BatchJobStatus.PAUSED);
					this.handledDC.unscheduleEvent(job.getRestartEvent());
					this.handledDC.unscheduleEvent(job.getFinishEvent());
					continue;
				}
				assignedServers = new ArrayList<Server>();
				Server cIdleServer;
				for(int i=0; i < job.getAmountOfServers(); i++) {
					cIdleServer = this.idleServer.get(0);
					
					cIdleServer.setStatus(ServerStatus.OCCUPIED);
					cIdleServer.setCurrentJob(job);
					this.serverJobMapping.put(cIdleServer.getId(), job.getId());
					this.occupiedServer.add(cIdleServer);
					this.idleServer.remove(cIdleServer);
					
					assignedServers.add(cIdleServer);
					this.handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, cIdleServer));
				}
				this.handledDC.rescheduleEvent(job.getFinishEvent(), job.getCalculatedFinishTime());
				job.setStatus(BatchJobStatus.RUNNING);
				job.assignServers(assignedServers);
				job.setScheduledRestartTime(0);
				
				if(!scheduledJobs.remove(job)) {
					System.out.println("Restarted job was not in scheduled list! " + job.getStatus());
				}
				this.runningJobs.add(job);
			}
			this.handledDC.handledEvent(event);
		}
	}
	
	/**
	 * Determines all currently idle servers.
	 * @param servers List of all servers in the DC to which this EventHandler belongs.
	 * @return List of all servers that are currently in the IDLE status.
	 */
	private ArrayList<Server> getIdleServers(List<Server> servers) {
		ArrayList<Server> result = new ArrayList<Server>();
		
		for(Server s : servers) {
			if(s.getStatus() == ServerStatus.IDLE) {
				result.add(s);
			}
		}
		
		return result;
		
	}
	
	/**
	 * Determines all currently occupied servers.
	 * @param servers List of all servers in the DC to which this EventHandler belongs.
	 * @return List of all servers that are currently in the OCCUPIED status.
	 */
	private ArrayList<Server> getOccupiedServers(List<Server> servers) {
		ArrayList<Server> result = new ArrayList<Server>();
		
		for(Server s : servers) {
			if(s.getStatus() == ServerStatus.OCCUPIED) {
				result.add(s);
				if(!this.handledDC.isCopy()) {
					serverJobMapping.put(s.getId(), s.getCurrentJob().getId());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Initializes the job lists of the EventHandler component.
	 * @param handledDC DC to which this EventHandler component belongs.
	 */
	private void initJobLists(DC handledDC) {
		this.runningJobs = new ArrayList<BatchJob>();
		this.finishedJobs = new ArrayList<BatchJob>();
		this.pausedJobs = new ArrayList<BatchJob>();
		for(BatchJob j : handledDC.getUnsubmittedParsedJobs()) {
			if(j.getStatus() == BatchJobStatus.RUNNING) {
				this.runningJobs.add(j);
			}
			else if(j.getStatus() == BatchJobStatus.FINISHED) {
				this.finishedJobs.add(j);
			}
			else if(j.getStatus() == BatchJobStatus.PAUSED) {
				this.pausedJobs.add(j);
			}
		}
	}

}
