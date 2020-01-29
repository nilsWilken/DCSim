package de.uma.dcsim.utilities;

import java.util.List;

import de.uma.dcsim.SLAModels.SLAModel;
import de.uma.dcsim.SLAModels.StandardSLAModel;
import de.uma.dcsim.eventHandling.Event;
import de.uma.dcsim.eventHandling.EventType;
import de.uma.dcsim.eventHandling.JobEvent;
import de.uma.dcsim.hardware.Server;
import de.uma.dcsim.powerModels.PowerModelSelector;
import de.uma.dcsim.runtimeModels.RuntimeModelSelector;
import de.uma.dcsim.simulationControl.Setup;

/**
 * This class represents a batch job within the simulation framework.
 * 
 * @author nilsw
 *
 */
public class BatchJob {

	/**
	 * Unique ID of the job.
	 */
	private String id; 
	
	/**
	 * Current execution frequency of the job.
	 */
	private double frequency; 
	
	/**
	 * Originally parsed execution frequency of the job.
	 */
	private double originalFrequency;
	
	/**
	 * Amount of servers that this job occupies during execution.
	 */
	private int amountOfServers; 
	
	/**
	 * Average power consumption  that was parsed from the workload trace.
	 */
	private double averagePowerConsumption;
	
	/**
	 * Duration in simulation time that was parsed from the workload trace.
	 */
	private int duration; 
	
	/**
	 * Job class that was parsed from the workload trace.
	 */
	private int jobClass;

	/**
	 * SLA deadline in simulation time.
	 */
	private int slaDeadline; 
	
	/**
	 * SLA model that is utilized by the job.
	 */
	private SLAModel slaModel;
	
	/**
	 * Amount of simulation timesteps that the job finished after its specified SLA deadline.
	 * This is 0 until the job has actually finished.
	 */
	private int delay; 
	
	/**
	 * Submission time of the job.
	 */
	private int submissionTime;
	
	/**
	 * Currently scheduled start time of the job.
	 */
	private int scheduledStartTime; 
	
	/**
	 * Currently scheduled restart time of the job.
	 */
	private int scheduledRestartTime;
	
	/**
	 * Actual finishing time of the job.
	 */
	private int finishingTime;
	
	/**
	 * Amount of simulation timesteps for which the job was already executed.
	 */
	private int elapsedRuntime; 
	
	/**
	 * Amount of simulation timesteps that still have to be executed until the job finishes.
	 */
	private int remainingRuntime;
	
	/**
	 * Amount of simulation timesteps that job was in the PAUSED status.
	 */
	private int pausedTime;
	
	/**
	 * Finish event of the job.
	 */
	private Event finishEvent;
	
	/**
	 * Start event of the job.
	 */
	private Event startEvent;
	
	/**
	 * Restart event of the job.
	 */
	private Event restartEvent;

	/**
	 * Current status of the job.
	 */
	private BatchJobStatus status;
	
	/**
	 * Indicates whether the job is currently requested to pause and thus a pause event is scheduled for it.
	 */
	private boolean requestedToPause;
	
	/**
	 * Indicates whether a job is already rescheduled (used by the EventHandler, when it handles a pause event for this job).
	 */
	private boolean alreadyRescheduled;
	
	/**
	 * List of all servers on which the job is executed. This is null if the job is currently not running.
	 */
	private List<Server> servers; 

	public BatchJob(String id, double frequency, int amountOfServers, double averagePowerConsumption, int duration, BatchJobStatus status, int submissionTime, int scheduledStartTime, int slaDeadline, int jobClass) {
		
		this.id = id;
		this.frequency = frequency;
		this.originalFrequency = frequency;
		this.amountOfServers = amountOfServers;
		this.averagePowerConsumption = averagePowerConsumption;
		this.duration = duration;
		this.elapsedRuntime = 0;
		this.remainingRuntime = duration;
		this.status = status;
		this.scheduledStartTime = scheduledStartTime;
		this.requestedToPause = false;
		this.alreadyRescheduled = false;
		this.pausedTime = 0;
		this.jobClass = jobClass;

		this.submissionTime = submissionTime;

		this.restartEvent = new JobEvent(EventType.JOB_RESTART, this.scheduledStartTime, this);
		this.finishEvent = new JobEvent(EventType.JOB_FINISH, this.scheduledStartTime, this);
		this.startEvent = new JobEvent(EventType.JOB_START, this.scheduledStartTime, this);
		
		this.slaModel = new StandardSLAModel();
		this.slaDeadline = slaDeadline;
	}
	
	/**
	 * Provides a deep copy of the job.
	 * @return Deep copy instance of the instance on which this method is called.
	 */
	public BatchJob deepCopy() {
		BatchJob copy = new BatchJob(id, frequency, amountOfServers, averagePowerConsumption, duration, BatchJobStatus.PARSED, this.submissionTime, this.scheduledStartTime, this.slaDeadline, jobClass);
		switch(status) {
		case PARSED:
			copy.setStatus(BatchJobStatus.PARSED);
			break;
		case SUBMITTED:
			copy.setStatus(BatchJobStatus.SUBMITTED);
			break;
		case PAUSED:
			copy.setStatus(BatchJobStatus.PAUSED);
			break;
		case RUNNING:
			copy.setStatus(BatchJobStatus.RUNNING);
			break;
		case FINISHED:
			copy.setStatus(BatchJobStatus.FINISHED);
			break;
		case SCHEDULED:
			copy.setStatus(BatchJobStatus.SCHEDULED);
			break;
		case RESCHEDULED:
			copy.setStatus(BatchJobStatus.RESCHEDULED);
			break;
		default:
			break;
		}
		
		copy.setIsRequestedToPause(requestedToPause);
		copy.setElapsedRuntime(this.elapsedRuntime);
		copy.setRemainingRuntime(this.remainingRuntime);
		copy.setPausedTime(this.pausedTime);
		copy.setActualFinsihingTime(this.finishingTime);
		copy.setSubmissionTime(this.submissionTime);
		copy.setScheduledRestartTime(this.scheduledRestartTime);
		
		Event finishEvent = new JobEvent(EventType.JOB_FINISH, copy.getCalculatedFinishTime(), copy);
		copy.setFinishEvent(finishEvent);
		
		return copy;
	}
	
	/**
	 * Calculates the SLA costs of a job. This method uses the delay variable of the job and thus the SLA costs
	 * that are calculated by this method are 0 until the job has actually finished. Before this, the getCurrentlyCausedSLACosts
	 * method can be used.
	 * 
	 * @param usagePrice Usage price that the DC, in which this job is executed, charges per compute node per hour in euro.
	 * @return SLA costs of the job.
	 */
	public double calculateSLACosts(double usagePrice) {
		usagePrice = (usagePrice*this.amountOfServers)*((this.duration*Setup.secondsPerSimulationTimestep)/3600);
		return this.slaModel.calculateSLAFee((this.delay/this.duration), usagePrice);
	}
	
	/**
	 * Calculates the currently estimated SLA costs of this job. The estimation is based on the assumption the the job
	 * runs until the end in the current configuration and without being shifted.
	 * 
	 * @param usagePrice Usage price that the DC, in which this job is executed, charges per compute node per hour in euro.
	 * @return Estimated SLA costs of the job.
	 */
	public double getCurrentlyCausedSLACosts(double usagePrice) {
		usagePrice = (usagePrice*this.amountOfServers)*((this.duration*Setup.secondsPerSimulationTimestep)/3600);
		int delay = this.calculateFinishTime() - this.slaDeadline;
		if(delay <= 0) {
			return 0;
		}
		else {
			return this.slaModel.calculateSLAFee((delay/this.duration), usagePrice);
		}
	}
	
	/**
	 * Elapse one simulation timestep for the job. If the job is running, the elapsedRuntime and the remainingRuntime are adjusted.
	 * Otherwise, if the job is paused or rescheduled, the paused time is updated.
	 */
	public void elapseOneSimulationTimestep() {
		if(this.status == BatchJobStatus.RUNNING) {
			if(this.remainingRuntime > 0) {
				this.elapsedRuntime ++;
				this.remainingRuntime --;
			}
			else {
				System.out.println(this.getId() + " remaining runtime over! " + this.finishEvent.getTimestamp() );
			}
		}
		else if(this.status == BatchJobStatus.PAUSED || this.status == BatchJobStatus.RESCHEDULED) {
			this.pausedTime ++;
		}
	}
	
	/**
	 * Returns the estimated finish time of this job. Thereby, it is assumed that the run configuration of the job
	 * is not changed and that the job is not shifted.
	 * 
	 * @return Estimated finish time in simulation time.
	 */
	public int getCalculatedFinishTime() {
		return this.calculateFinishTime();
	}
	
	/**
	 * Returns the APC value  of the job that was parsed from the workload trace.
	 * @return Parsed APC value  of the job.
	 */
	public double getParsedAveragePowerConsumption() {
		return this.averagePowerConsumption;
	}
	
	/**
	 * Returns the APC value  of the job that is obtained by the currently used server power consumption model.
	 * @return Modeled APC value  of the job.
	 */
	public double getModeledAveragePowerConsumption() {
		return PowerModelSelector.getServerPower(this.frequency, this.jobClass);
	}

	/**
	 * Returns the total APC value  of the job. This means that the modeled APC value is multiplied with the
	 * amount of nodes that this job utilizes before it is returned.
	 * 
	 * @return Total APC value  of this job.
	 */
	public double getTotalPowerConsumption() {
		return (PowerModelSelector.getServerPower(this.frequency, this.jobClass)*(double)this.amountOfServers);
	}
	
	public int getJobClass() {
		return this.jobClass;
	}
	
	public Event getStartEvent() {
		return this.startEvent;
	}
	
	public Event getRestartEvent() {
		return this.restartEvent;
	}
	
	public Event getFinishEvent() {
		return this.finishEvent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public double getFrequency() {
		return frequency;
	}

	/**
	 * Sets the execution frequency of the job and automatically updates the APC value and
	 * the remaining runtime of the job.
	 * @param frequency New execution frequency of the job.
	 */
	public void setFrequency(double frequency) {
		this.updateRemainingRuntime(frequency);
		this.updateAveragePowerConsumption(frequency);
		this.frequency = frequency;
	}
	
	public boolean isRunningAt(int timestep) {
		if(this.status == BatchJobStatus.SCHEDULED) {
			if(this.startEvent.getTimestamp() <= timestep && timestep <= (this.getCalculatedFinishTime()+1)) {
				return true;
			}
			return false;
		}
		else if(this.status == BatchJobStatus.RUNNING) {
			if(timestep <= this.getCalculatedFinishTime()) {
				return true;
			}
			return false;
		}
		else if(this.status == BatchJobStatus.RESCHEDULED) {
			if(this.restartEvent.getTimestamp() <= timestep && timestep <= (this.getCalculatedFinishTime()+1)) {
				return true;
			}
			return false;
		}
		else {
			return false;
		}
	}
	
	public double getOriginalFrequency() {
		return this.originalFrequency;
	}

	public int getAmountOfServers() {
		return amountOfServers;
	}

	public void setAmountOfServers(int amountOfServers) {
		this.amountOfServers = amountOfServers;
	}
	
	public int getDurationInSimulationTime() {
		return this.duration;
	}
	
	public void setDurationInSimulationTime(int duration) {
		this.duration = duration;
	}

	public BatchJobStatus getStatus() {
		return status;
	}

	public void setStatus(BatchJobStatus status) {
		this.status = status;
	}
	
	public int getStartTime() {
		return this.scheduledStartTime;
	}
	
	public int getScheduledStartTime() {
		return this.startEvent.getTimestamp();
	}
	
	public void setStartTime(int startTime) {
		this.scheduledStartTime = startTime;
	}
	
	public List<Server> getAssignedServers() {
		return this.servers;
	}
	
	public void assignServers(List<Server> servers) {
		this.servers = servers;
	}
	
	public int getSLADeadline() {
		return this.slaDeadline;
	}
	
	public void setSLADeadline(int slaDeadline) {
		this.slaDeadline = slaDeadline;
	}
	
	public SLAModel getSLAModel() {
		return this.slaModel;
	}
	
	public void setSLAModel(SLAModel slaModel) {
		this.slaModel = slaModel;
	}
	
	public int getRemainingRuntimeInSimulationTime() {
		return this.remainingRuntime;
	}
	
	public boolean isRequestedToPause() {
		return this.requestedToPause;
	}
	
	public void setIsRequestedToPause(boolean requestedToPause) {
		this.requestedToPause = requestedToPause;
	}
	
	public boolean isAlreadyRescheduled() {
		return this.alreadyRescheduled;
	}
	
	public void setIsAlreadyRescheduled(boolean alreadyRescheduled) {
		this.alreadyRescheduled = alreadyRescheduled;
	}
	
	public int getSubmissionTime() {
		return this.submissionTime;
	}
	
	public void setSubmissionTime(int submissionTime) {
		this.submissionTime = submissionTime;
	}
	
	public int getScheduledRestartTime() {
		return this.scheduledRestartTime;
	}
	
	public void setScheduledRestartTime(int scheduledRestartTime) {
		this.scheduledRestartTime = scheduledRestartTime;
	}
	
	public int getFinishingDelayInSimulationTime() {
		return this.delay;
	}
	
	public void setElapsedRuntime(int elapsedRuntime) {
		this.elapsedRuntime = elapsedRuntime;
	}
	
	public void setRemainingRuntime(int remainingRuntime) {
		this.remainingRuntime = remainingRuntime;
	}
	
	public void setPausedTime(int pausedTime) {
		this.pausedTime = pausedTime;
	}
	
	public int getActualFinishingTime() {
		return this.finishingTime;
	}
	
	/**
	 * Sets the actual finshing time of a job and automatically calculates the delay of the job.
	 * @param finishingTime Actual finishing time of the job.
	 */
	public void setActualFinsihingTime(int finishingTime) {
		this.finishingTime = finishingTime;
		if(this.finishingTime > this.slaDeadline) {
			this.delay = this.finishingTime - this.slaDeadline;
		}
	}
	
	protected void setFinishEvent(Event finishEvent) {
		this.finishEvent = finishEvent;
	}
	
	private void updateRemainingRuntime(double adjustedFrequency) {
		this.remainingRuntime = RuntimeModelSelector.getAdjustedRuntime(adjustedFrequency, this.frequency, this.remainingRuntime);
	}
	
	private void updateAveragePowerConsumption(double adjustedFrequency) {
		this.averagePowerConsumption = PowerModelSelector.getServerPower(adjustedFrequency, this.jobClass);
	}
	
	private int calculateFinishTime() {
		if(this.status == BatchJobStatus.RESCHEDULED || this.status == BatchJobStatus.PAUSED) {
			return this.scheduledRestartTime + this.remainingRuntime;
		}
		else {
			return this.scheduledStartTime + this.elapsedRuntime + this.remainingRuntime + this.pausedTime;
		}
	}

}
