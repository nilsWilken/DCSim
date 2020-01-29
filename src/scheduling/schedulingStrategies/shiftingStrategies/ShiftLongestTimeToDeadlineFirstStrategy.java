package scheduling.schedulingStrategies.shiftingStrategies;

import java.util.ArrayList;
import java.util.List;

import eventHandling.EventType;
import eventHandling.JobEvent;
import hardware.DC;
import scheduling.schedulingStrategies.schedulingUtilities.JobSortContainer;
import scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import simulationControl.Setup;
import utilities.BatchJob;
import utilities.BatchJobStatus;

public class ShiftLongestTimeToDeadlineFirstStrategy implements ShiftingStrategy {
	@Override
	public int preponeJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int amountOfNodeStepsToShift, int currentTime, int schedulingInterval, DC handledDC) {
		//Calculate end of considered interval
		int intervalEnd = currentTime + schedulingInterval;
		
		//Determine all jobs that are preponable for the considered interval
		List<BatchJob> preponableJobs = this.getPreponableJobs(submittedJobs, affectedSubmittedJobs, scheduledJobs, pausedJobs, currentTime);
		
		//Sort jobs according to their Theta_STDF values
		List<JobSortContainer> sortedList = SchedulingStrategyUtilities.sortJobListByTheta(preponableJobs);
		
		//Create list of jobs that are relevant for the creation of the node occupation plan
		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>(scheduledJobs);
		allJobs.addAll(runningJobs);
		
		//Create node occupation plan
		int[] nodeOccupationPlan = SchedulingStrategyUtilities.getNodeOccupationPlan(allJobs, schedulingInterval, currentTime);
		
		
		//Actually prepone workload
		BatchJob cJob;
		int originalStartTime = 0;
		int nodeStepSum = 0;
		int originalNodeStepSum = 0;
		int tmp;
		boolean scheduled;
		
		//Prepone jobs with smallest Theta_STDF values first
		for(int i=0; i < sortedList.size(); i++) {			
			cJob = sortedList.get(i).getJob();
			tmp = currentTime;
			scheduled = false;
			originalNodeStepSum = this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd);
			
			//Try to prepone job
			while(tmp < intervalEnd && !scheduled) {
				//If currently considerd job is paused
				if (cJob.getStatus() == BatchJobStatus.PAUSED) {
					if(tmp == currentTime) {
						originalStartTime = cJob.getScheduledRestartTime();
					}
					cJob.setScheduledRestartTime(tmp);
					if(SchedulingStrategyUtilities.checkSchedulingFeasibilityForJob(nodeOccupationPlan, cJob, currentTime)) {
						nodeStepSum += (this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd) - originalNodeStepSum);
						scheduled = true;

						pausedJobs.remove(cJob);
						scheduledJobs.add(cJob);
						cJob.setStatus(BatchJobStatus.RESCHEDULED);
						handledDC.rescheduleEvent(cJob.getRestartEvent(), cJob.getScheduledRestartTime());
					}
				//If currently considered job is submitted or scheduled
				} else {
					if(tmp == currentTime) {
						originalStartTime = cJob.getStartTime();
					}
					cJob.setStartTime(tmp);
					if(SchedulingStrategyUtilities.checkSchedulingFeasibilityForJob(nodeOccupationPlan, cJob, currentTime)) {
						nodeStepSum += (this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd) - originalNodeStepSum);
						scheduled = true;

						handledDC.rescheduleEvent(cJob.getStartEvent(),cJob.getStartTime());
						if(cJob.getStatus() == BatchJobStatus.SUBMITTED) {
							if(!submittedJobs.remove(cJob)) {
								affectedSubmittedJobs.remove(cJob);
							}
							scheduledJobs.add(cJob);
						}
						cJob.setStatus(BatchJobStatus.SCHEDULED);
					}
				}
				tmp++;
			}
			//If currently considered job was scheduled successfully --> update node occupation plan
			if(scheduled) {
				SchedulingStrategyUtilities.updateNodeOccupationPlan(nodeOccupationPlan, cJob, currentTime);
			}
			//If currently considered job was not scheduled succesffully --> set specified start/restart time back to the original value
			else {
				if(cJob.getStatus() == BatchJobStatus.PAUSED) {
					cJob.setScheduledRestartTime(originalStartTime);
				}
				else {
					cJob.setStartTime(originalStartTime);
				}
			}
			//If the requested amount of shifted node steps is reached --> stop shifting
			if(nodeStepSum >= amountOfNodeStepsToShift && amountOfNodeStepsToShift >= 0) {
				break;
			}
		}
		return nodeStepSum;
	}

	@Override
	public int postponeJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int amountOfNodeStepsToShift, int currentTime, int schedulingInterval, DC handledDC) {
		//Sort all jobs that are postponable according to their Theta_STDF values
		List<JobSortContainer> sortedList = SchedulingStrategyUtilities.sortJobListByTheta(this.getPostponableJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval));
		int nodeStepSum = 0;
		
		//If no node steps are requested to be shifted --> return
		if(amountOfNodeStepsToShift == 0) {
			return 0;
		}
		
		//Calculate end of considered interval in simulation time
		int intervalEnd = currentTime + schedulingInterval;
		
		//Actually shift workload
		BatchJob cJob;
		//Postpone jobs with largest Theta_STDF values first
		for (int i = sortedList.size() - 1; i > -1; i--) {
			cJob = sortedList.get(i).getJob();

			//If currently considered job is scheduled
			if (cJob.getStatus() == BatchJobStatus.SCHEDULED) {
				nodeStepSum += this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd);
				cJob.setStartTime(intervalEnd);

				//Job will be set back to the submitted status
				cJob.setStatus(BatchJobStatus.SUBMITTED);
				scheduledJobs.remove(cJob);
				
				//If superMUCMode is used, this job hast to be added to the affectedSubmittedJobList
				if (Setup.superMUCMode) {
					affectedSubmittedJobs.add(cJob);
				} else {
					submittedJobs.add(cJob);
				}
				
				handledDC.unscheduleEvent(cJob.getStartEvent());
				
				//Log outputs
				if(!handledDC.isCopy() && Setup.testLogOutput)
				System.out.println(handledDC + " " + cJob.getId() + " " + cJob.getStatus() + " shifted!");
			
			//If the currently considered job is running
			} else if (cJob.getStatus() == BatchJobStatus.RUNNING) {
				nodeStepSum += this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd);
				cJob.setScheduledRestartTime(intervalEnd);

				//Job will be paused
				cJob.setIsRequestedToPause(true);
				handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, currentTime, cJob));

				//Log output
				if(!handledDC.isCopy() && Setup.testLogOutput)
				System.out.println(handledDC + " " + cJob.getId() + " " + cJob.getStatus() + " shifted!");
			
			//If the currently considered job is rescheduled
			} else if (cJob.getStatus() == BatchJobStatus.RESCHEDULED) {
				nodeStepSum += this.calculateNodeStepsForJob(cJob, currentTime, intervalEnd);
				
				//Job will be set back to the paused status
				cJob.setStatus(BatchJobStatus.PAUSED);
				scheduledJobs.remove(cJob);
				pausedJobs.add(cJob);

				cJob.setScheduledRestartTime(intervalEnd);
				handledDC.unscheduleEvent(cJob.getRestartEvent());
				
				//Log output
				if(!handledDC.isCopy() && Setup.testLogOutput)
				System.out.println(handledDC + " " + cJob.getId() + " " + cJob.getStatus() + " shifted!");
			
			//If the currently considered job is submitted, the shifted node steps are not counted
			} else {
				cJob.setStartTime(intervalEnd);
			}
			//If the currently shifted amount of nodesteps is greater or equal to the requested amount --> stop shifting
			if(nodeStepSum >= amountOfNodeStepsToShift) {
				break;
			}
		}
		return nodeStepSum;
	}
	
	@Override
	public void scheduleForMaximumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingInterval, DC handledDC) {
		this.preponeJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, -1, currentTime, schedulingInterval, handledDC);
	}
	
	public void scheduleForMinimumPowerDemand(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingInterval, DC handledDC) {
		//Basically shifts all postponable jobs out of the interval
		
		List<BatchJob> jobs = this.getPostponableJobs(submittedJobs, affectedSubmittedJobs, runningJobs, pausedJobs, scheduledJobs, currentTime, schedulingInterval);
		int intervalEnd = currentTime+schedulingInterval;
		for(BatchJob job : jobs) {
			if(job.getStatus() == BatchJobStatus.RUNNING) {
				job.setScheduledRestartTime(intervalEnd);
				handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, currentTime, job));
				job.setIsRequestedToPause(true);
				
			}
			else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
				job.setStartTime(intervalEnd);
				
				job.setStatus(BatchJobStatus.SUBMITTED);
				scheduledJobs.remove(job);
				if(Setup.superMUCMode) {
					affectedSubmittedJobs.add(job);
				}
				else {
					submittedJobs.add(job);
				}
				
				handledDC.unscheduleEvent(job.getStartEvent());				
			}
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
				job.setScheduledRestartTime(intervalEnd);
				job.setStatus(BatchJobStatus.PAUSED);
				
				scheduledJobs.remove(job);
				pausedJobs.add(job);
				
				handledDC.unscheduleEvent(job.getRestartEvent());
			}
			else {
				job.setStartTime(intervalEnd);
			}
		}
	}
	
	public List<BatchJob> getPreponableJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> scheduledJobs, List<BatchJob> pausedJobs, int currentTime) {
		List<BatchJob> preponableJobs = new ArrayList<BatchJob>();
		
		List<BatchJob> relevantJobs = new ArrayList<BatchJob>();
		relevantJobs.addAll(submittedJobs);
		relevantJobs.addAll(scheduledJobs);
		relevantJobs.addAll(pausedJobs);
		relevantJobs.addAll(affectedSubmittedJobs);
				
		for(BatchJob job : relevantJobs) {
			//If jobs are submitted, scheduled, rescheduled, or paused they can be preponed
			if(job.getStatus() == BatchJobStatus.SCHEDULED || job.getStatus() == BatchJobStatus.RESCHEDULED || job.getStatus() == BatchJobStatus.PAUSED || job.getStatus() == BatchJobStatus.SUBMITTED) {
				preponableJobs.add(job);
			}
		}
		System.out.println(preponableJobs.size());
		return preponableJobs;
	}
	
	public List<BatchJob> getPostponableJobs(List<BatchJob> submittedJobs, List<BatchJob> affectedSubmittedJobs, List<BatchJob> runningJobs, List<BatchJob> pausedJobs, List<BatchJob> scheduledJobs, int currentTime, int schedulingIntervalLength) {
		List<BatchJob> postponableJobs = new ArrayList<BatchJob>();
		
		int intervalEnd = currentTime + schedulingIntervalLength;
		
		List<BatchJob> relevantJobs = new ArrayList<BatchJob>();
		relevantJobs.addAll(scheduledJobs);
		relevantJobs.addAll(runningJobs);

		
		for(BatchJob job : relevantJobs) {
			//If jobs are scheduled and they are supposed to start before the interval end, they can be postponed
			if((job.getStatus() == BatchJobStatus.SCHEDULED) && job.getStartTime() < intervalEnd) {
				postponableJobs.add(job);
			}
			//Similarly, if jobs are rescheduled and they are supposed to restart before the interval end, they can be postponed
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED && job.getScheduledRestartTime() < intervalEnd) {
				postponableJobs.add(job);
			}
			//If jobs are currently running, they can only be postponed, when it is possible to pause jobs
			else if(Setup.jobsPausable && job.getStatus() == BatchJobStatus.RUNNING && job.getStartTime() < intervalEnd) {
				postponableJobs.add(job);
			}
		}
		
		return postponableJobs;
	}
	
	private int calculateNodeStepsForJob(BatchJob job, int intervalStart, int intervalEnd) {
		int nodeSteps = 0;
		
		int tmp = -1;
		
		if(job.getStatus() == BatchJobStatus.SCHEDULED) {
			tmp = job.getStartTime();
		}
		else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
			tmp = job.getScheduledRestartTime();
		}
		else if(job.getStatus() == BatchJobStatus.RUNNING) {
			tmp = intervalStart;
		}
		
		if (tmp != -1) {
			while (tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
				nodeSteps += job.getAmountOfServers();
				tmp++;
			}
		}
		
		return nodeSteps;
	}
	

}
