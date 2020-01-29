package de.uma.dcsim.scheduling.schedulingStrategies.timeSortScheduling;

import java.util.ArrayList;
import java.util.List;

import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.scheduling.schedulingStrategies.SchedulingStrategy;
import de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import de.uma.dcsim.simulationControl.Setup;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobStatus;

public class FirstInFirstOutScheduling implements SchedulingStrategy {

	private int backfillingFinishingDeadline;

	@Override
	public void scheduleNextInterval(List<BatchJob> runningJobs, List<BatchJob> submittedJobs, List<BatchJob> scheduledJobs, List<BatchJob> pausedJobs, int intervalLength, int currentTime, int[] nodeOccupationPlan, DC handledDC) {
		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>(pausedJobs);
		allJobs.addAll(submittedJobs);

		//Sort Jobs according to their theta values (see thesis)
		List<BatchJob> sortedJobs = SchedulingStrategyUtilities.sortJobListBySubmissionTime(allJobs);
		this.backfillingFinishingDeadline = 0;
	
		//Calculate interval end
		int intervalEnd = currentTime + intervalLength;
		int tmp;
		boolean scheduled = true;
		BatchJob pJob;
		
		//Schedule jobs
		for(int i=0; i < sortedJobs.size(); i++) {
			pJob = sortedJobs.get(i);
			tmp = currentTime;
			scheduled = false;
			while (tmp < intervalEnd) {
				if(pJob.getStatus() == BatchJobStatus.PAUSED) {
					pJob.setScheduledRestartTime(tmp);
				}
				else if(pJob.getStatus() == BatchJobStatus.SUBMITTED) {
					pJob.setStartTime(tmp);
				}
				
				//If earlier during this call of the method, a job was not able to be scheduled and the current job would finish after the 
				//earliest possible start time of the job that first was not scheduled
				if(this.backfillingFinishingDeadline != 0 && pJob.getCalculatedFinishTime() >= this.backfillingFinishingDeadline) {
					break;
				}
				
				//If it is feasible to schedule a job (maximum number of nodes is not exceeded)
				if (SchedulingStrategyUtilities.checkSchedulingFeasibilityForJob(nodeOccupationPlan, pJob, currentTime)) {
					SchedulingStrategyUtilities.updateNodeOccupationPlan(nodeOccupationPlan, pJob, currentTime);
					if(pJob.getStatus() == BatchJobStatus.PAUSED) {
						handledDC.rescheduleEvent(pJob.getRestartEvent(), pJob.getScheduledRestartTime());
						pausedJobs.remove(pJob);
						scheduledJobs.add(pJob);
						pJob.setStatus(BatchJobStatus.RESCHEDULED);
						scheduled = true;
						if(!handledDC.isCopy() && Setup.testLogOutput) {
							System.out.println(handledDC + "Restart of " + pJob.getId() + " scheduled!");
							if(this.backfillingFinishingDeadline != 0) {
								System.out.println("backfilled");
							}
						}
					}
					else if(pJob.getStatus() == BatchJobStatus.SUBMITTED) {
						handledDC.rescheduleEvent(pJob.getStartEvent(), pJob.getStartTime());
						submittedJobs.remove(pJob);
						scheduledJobs.add(pJob);
						pJob.setStatus(BatchJobStatus.SCHEDULED);
						scheduled = true;
						if(!handledDC.isCopy() && Setup.testLogOutput) {
							System.out.println(handledDC + " Start of " + pJob.getId() + " scheduled! " + currentTime + " " + intervalEnd);
							if(this.backfillingFinishingDeadline != 0) {
								System.out.println("backfilled!");
							}
						}
					}
					break;
				}
				tmp++;
			}
			//If a job was not scheduled successfully
			if(!scheduled) {
				if(pJob.getStatus() == BatchJobStatus.PAUSED) {
					pJob.setScheduledRestartTime(intervalEnd);
				}
				else if(pJob.getStatus() == BatchJobStatus.SUBMITTED) {
					pJob.setStartTime(intervalEnd);
				}
				
				//If this is the first job that was not scheduled
				if(this.backfillingFinishingDeadline == 0) {
					List<BatchJob> relevantJobs = new ArrayList<BatchJob>(runningJobs);
					relevantJobs.addAll(scheduledJobs);
					this.backfillingFinishingDeadline = SchedulingStrategyUtilities.determineEarliestTimeForNodeAvailability(relevantJobs, pJob.getAmountOfServers(), currentTime);
				}
			}
		}
		
	}
	
	public int getBackfillingFinishingDeadline() {
		return this.backfillingFinishingDeadline;
	}

	
	

}
