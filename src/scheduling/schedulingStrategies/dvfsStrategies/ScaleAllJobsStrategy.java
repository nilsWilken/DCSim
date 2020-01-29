package scheduling.schedulingStrategies.dvfsStrategies;

import java.util.ArrayList;
import java.util.List;

import eventHandling.EventType;
import eventHandling.ServerEvent;
import hardware.DC;
import hardware.Server;
import utilities.BatchJob;
import utilities.BatchJobStatus;
import utilities.ReserveProvisionType;

/**
 * This class implements a simple strategy to determine the jobs for which the execution frequency should be adjusted.
 * It simply selects all jobs.
 * 
 * @author nilsw
 *
 */
public class ScaleAllJobsStrategy implements DVFSStrategy {
	
	public void scaleFrequencies(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, ReserveProvisionType provisionType, int amountOfNodeStepsToScale, double requestedFrequency, double maximumFrequency, double minimumFrequency, int currentTime, int schedulingInterval, DC handledDC) {
		int intervalEnd = currentTime + schedulingInterval;
		
		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>();
		allJobs.addAll(submittedJobs);
		allJobs.addAll(scheduledJobs);
		allJobs.addAll(runningJobs);
		allJobs.addAll(pausedJobs);
		
		//Get list of jobs for which the change of the execution frequency actually has an impact on the power consumption of the DC
		List<BatchJob> scalableJobs = this.getScalableJobs(allJobs, intervalEnd, currentTime);
		
		//Scale all jobs
		for(BatchJob cJob : scalableJobs) {
			cJob.setFrequency(requestedFrequency);
			//If the job is currently running, the power consumption of the server and the finish event of the job have to be updated
			if(cJob.getStatus() == BatchJobStatus.RUNNING) {
				for(Server s : cJob.getAssignedServers()) {
					handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, s));
				}
				handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
			}
		}
	}
	
	@Override
	public void scheduleForMaximumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double maximumFrequency, int currentTime, int schedulingInterval, DC handledDC) {
		int intervalEnd = currentTime + schedulingInterval;
		
		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>();
		allJobs.addAll(submittedJobs);
		allJobs.addAll(scheduledJobs);
		allJobs.addAll(runningJobs);
		allJobs.addAll(pausedJobs);
		
		List<BatchJob> scalableJobs = this.getScalableJobs(allJobs, intervalEnd, currentTime);
		
		BatchJob cJob;
		for(int i=0; i < scalableJobs.size(); i++) {
			cJob = scalableJobs.get(i);
			cJob.setFrequency(maximumFrequency);
			
			if(cJob.getStatus() == BatchJobStatus.RUNNING) {
				for(Server s : cJob.getAssignedServers()) {
					handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, s));
				}
				handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
			}
		}
	}
	
	public void scheduleForMinimumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, double minimumFrequency, int currentTime, int schedulingInterval, DC handledDC) {
		int intervalEnd = currentTime + schedulingInterval;
		
		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>();
		allJobs.addAll(submittedJobs);
		allJobs.addAll(scheduledJobs);
		allJobs.addAll(runningJobs);
		allJobs.addAll(pausedJobs);
		
		List<BatchJob> scalableJobs = this.getScalableJobs(allJobs, intervalEnd, currentTime);
		
		BatchJob cJob;
		for(int i=0; i < scalableJobs.size(); i++) {
			cJob = scalableJobs.get(i);
			cJob.setFrequency(minimumFrequency);
			
			if(cJob.getStatus() == BatchJobStatus.RUNNING) {
				for(Server s : cJob.getAssignedServers()) {
					handledDC.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, currentTime, s));
				}
				handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
			}
		}
	}
	
	public List<BatchJob> getScalableJobs(List<BatchJob> allJobs, int intervalEnd, int currentTime) {
		List<BatchJob> scalableJobs = new ArrayList<BatchJob>();
		
		for(BatchJob job : allJobs) {
			if((job.getStatus() == BatchJobStatus.SCHEDULED) && job.getStartTime() < intervalEnd) {
				scalableJobs.add(job);
			}
			else if(job.getStatus() == BatchJobStatus.RUNNING && !job.isRequestedToPause()) {
				scalableJobs.add(job);
			}
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED && job.getScheduledRestartTime() < intervalEnd && job.getScheduledRestartTime() >= currentTime) {
				scalableJobs.add(job);
			}
		}
		return scalableJobs;
	}



}
