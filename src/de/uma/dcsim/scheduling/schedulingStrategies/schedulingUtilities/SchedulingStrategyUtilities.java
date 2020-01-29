package de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities;

import java.util.ArrayList;
import java.util.List;

import de.uma.dcsim.eventHandling.Event;
import de.uma.dcsim.eventHandling.JobEvent;
import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobStatus;

/**
 * This class provides several utilities that are considered to be useful during the scheduling procedure.
 * 
 * @author nilsw
 *
 */
public class SchedulingStrategyUtilities {

	/**
	 * This methods calculates the Theta_STDF values for all jobs and sorts them in ascending order.
	 * @param jobsToSort List of all jobs that should be sorted.
	 * @return Sorted list of JobSortContainer instances that encapsule the corresponding BatchJob instances.
	 */
	public static List<JobSortContainer> sortJobListByTheta(List<BatchJob> jobsToSort) {
		List<JobSortContainer> sortedJobList = new ArrayList<JobSortContainer>();
		
		double jobTheta;
		for(BatchJob job : jobsToSort) {
			jobTheta = (((double)job.getSLADeadline() - (double)job.getCalculatedFinishTime())*9216)/(double)job.getAmountOfServers();
			sortedJobList.add(new JobSortContainer(job, jobTheta));
		}
		
		sortedJobList.sort(new JobComparator());
		
		return sortedJobList;
	}
	
//	public static List<JobSortContainer> sortJobListBySubmissionTime(List<BatchJob> jobsToSort) {
//		List<JobSortContainer> sortedJobList = new ArrayList<JobSortContainer>();
//		
//		
//		return sortedJobList;
//	}
	
	/**
	 * This methods sorts all provided jobs according to their submission time in ascending order.
	 * @param jobsToSort List of all jobs that should be sorted.
	 * @return Sorted list of BatchJob instances.
	 */
	public static List<BatchJob> sortJobListBySubmissionTime(List<BatchJob> jobsToSort) {
		List<JobSortContainer> sortedJobList = new ArrayList<JobSortContainer>();
		
		for(BatchJob job : jobsToSort) {
			sortedJobList.add(new JobSortContainer(job, job.getSubmissionTime()));
		}
		
		sortedJobList.sort(new JobComparator());
		
		List<BatchJob> result = new ArrayList<BatchJob>();
		for(JobSortContainer job : sortedJobList) {
			result.add(job.getJob());
		}
		
		return result;
	}
	
	/**
	 * Creates an array of the length that equals the specified interval length. Each position in the array corresponds to a 
	 * step in simulation time in the interval and will be filled with the amount of nodes that are active at this poin time, 
	 * according to the current schedule.
	 * 
	 * @param allJobs List of all jobs that are relevant for the schedule.
	 * @param schedulingIntervalLength Length in simulation time of the interval for which the node occupation is requested.
	 * @param currentTime Point in simulation time at which the interval for which the node occupation is requested starts.
	 * @return Array that indicates the amount of active nodes for each step in simulation time in the specified interval.
	 */
	public static int[] getNodeOccupationPlan(List<BatchJob> allJobs, int schedulingIntervalLength, int currentTime) {
		int[] nodeOccupationPlan = new int[schedulingIntervalLength];
		
		int intervalEnd = currentTime + schedulingIntervalLength;
//		int intervalEnd = currentTime + 190000;
		
		int counter = 0;
		int tmp;
		for(BatchJob job : allJobs) {
			if(job.getStatus() == BatchJobStatus.RUNNING) {
				counter = 0;
				tmp = currentTime;
				
				while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
					nodeOccupationPlan[counter] += job.getAmountOfServers();
					counter++;
					tmp++;
				}
			}
			else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
				tmp = job.getScheduledStartTime();
				counter = tmp - currentTime;
				
				while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
					nodeOccupationPlan[counter] += job.getAmountOfServers();
					counter++;
					tmp++;
				}
			}
			else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
				tmp = job.getScheduledRestartTime();
				counter = tmp - currentTime;
				while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
					nodeOccupationPlan[counter] += job.getAmountOfServers();
					counter++;
					tmp++;
				}
			}
			else {
				System.out.println(job.getStatus());
			}
		}
		return nodeOccupationPlan;
	}
	
	
	public static int[] getNodeOccupationPlanEventBased(DC handledDC, int schedulingIntervalLength, int currentTime) {
		int[] nodeOccupationPlan = new int[schedulingIntervalLength];
		
		int intervalEnd = currentTime + schedulingIntervalLength;
//		int intervalEnd = currentTime + 190000;
		
		int counter = 0;
		int tmp = currentTime;
		int occupiedServer = handledDC.getOccupiedServer().size();
		int startingNodes = 0;
		int finishingNodes = 0;
		BatchJob j;
		
		while(tmp < intervalEnd) {
			startingNodes = 0;
			finishingNodes = 0;
			if(handledDC.getJobStartEvents(tmp) != null) {
				for(Event e : handledDC.getJobStartEvents(tmp)) {
					j = ((JobEvent)e).getAffectedJob();
					startingNodes += j.getAmountOfServers();
				}
			}
			if(handledDC.getJobRestartEvents(tmp) != null) {
				for(Event e : handledDC.getJobRestartEvents(tmp)) {
					j = ((JobEvent)e).getAffectedJob();
					startingNodes += j.getAmountOfServers();
				}
			}
			if(handledDC.getJobFinishEvents(tmp) != null) {
				for(Event e : handledDC.getJobFinishEvents(tmp)) {
					j = ((JobEvent)e).getAffectedJob();
					finishingNodes += j.getAmountOfServers();
				}
			}
			if(handledDC.getJobPauseEvents(tmp) != null) {
				for(Event e : handledDC.getJobPauseEvents(tmp)) {
					j = ((JobEvent)e).getAffectedJob();
					finishingNodes += j.getAmountOfServers();
				}
			}
			
			occupiedServer += startingNodes;
			occupiedServer -= finishingNodes;
			
			nodeOccupationPlan[counter] = occupiedServer;
			
			tmp++;
			counter++;
		}
		
		
		return nodeOccupationPlan;
	}
	
	/**
	 * Updates an array (node occupation plan) of the length that equals the specified interval length. Each position in the array corresponds to a 
	 * step in simulation time in the interval and is filled with the amount of nodes that are active at this poin time, 
	 * according to the current schedule.
	 * @param nodeOccupationPlan Node occupation plan to update.
	 * @param job Job that should be included in the provided node occupation plan.
	 * @param currentTime Point in simulation time at which the interval, to which the node occupation plan corresponds, starts.
	 * @return Update node occupation plan.
	 */
	public static int[] updateNodeOccupationPlan(int[] nodeOccupationPlan, BatchJob job, int currentTime) {
		int intervalEnd = currentTime + nodeOccupationPlan.length;
		
		
		int tmp;
		if(job.getStatus() == BatchJobStatus.PAUSED) {
			tmp = job.getScheduledRestartTime();
		}
		else {
			tmp = job.getStartTime();
		}
		int counter = tmp - currentTime;


		while (tmp < intervalEnd && tmp <= (job.getCalculatedFinishTime())) {
			nodeOccupationPlan[counter] += job.getAmountOfServers();
			tmp++;
			counter++;
		}

		return nodeOccupationPlan;
	}
	
	/**
	 * Checks whether it is possible to schedule a job at the start/restart time that is currently specified by the
	 * BatchJob instance.
	 * @param nodeOccupationPlan Array that indicates the amounts of active nodes for each timestep in the considered interval.
	 * @param job BatchJob instance for which it should be checked whether it can be scheduled.
	 * @param currentTime Point in simulation time at which the considered interval starts.
	 * @return True when the job can be scheduled without exceeding the maximum amount of nodes, false otherwise.
	 */
	public static boolean checkSchedulingFeasibilityForJob(int[] nodeOccupationPlan, BatchJob job, int currentTime) {
		int intervalEnd = currentTime + nodeOccupationPlan.length;
		
		int tmp;
		if(job.getStatus() == BatchJobStatus.PAUSED) {
			tmp = job.getScheduledRestartTime();
		}
		else {
			tmp = job.getStartTime();
		}
		int counter = tmp - currentTime;
		
//		if(job.getId().equals("srv04-ib.297826")) {
//			System.out.println(job.getDurationInSimulationTime() + "\t " + job.getScheduledStartTime() + "\t " + job.getStartTime());
//			//			System.out.println("tmp: " + tmp + "\t nodeOccPlan+jobServer: " + (nodeOccupationPlan[counter]+job.getAmountOfServers()));
//		}
		if(tmp >= intervalEnd) {
			return false;
		}
		while (tmp < intervalEnd && tmp <= (job.getCalculatedFinishTime())) {
//			if(job.getId().equals("srv04-ib.263388")) {
//				System.out.println("tmp: " + tmp + "\t nodeOccPlan+jobServer: " + (nodeOccupationPlan[counter]+job.getAmountOfServers()));
//			}
			if((nodeOccupationPlan[counter]+job.getAmountOfServers()) > 9216) {
				return false;
			}
			tmp++;
			counter++;
		}
		
		return true;
	}
	
	/**
	 * Determines the earliest point in simulation time at which the amount of requeted nodes are idle.
	 * @param relevantJobs List of all jobs that currently may occupy some nodes of the DC.
	 * @param amountOfRequestedNodes Amount of nodes that are requested.
	 * @param currentTime Point in simulation time that indicates the point in time at which the check should start.
	 * @return Earliest point in simulation time at which the requested amount of nodes are idle, according to the schedule that is 
	 * defined by all provided jobs.
	 */
	public static int determineEarliestTimeForNodeAvailability(List<BatchJob> relevantJobs, int amountOfRequestedNodes, int currentTime) {
		int maxFinishTime = Integer.MIN_VALUE;
		if(relevantJobs.size() == 0) {
			return Integer.MAX_VALUE;
		}
		for(BatchJob j : relevantJobs) {
			if(j.getCalculatedFinishTime() > maxFinishTime) {
				maxFinishTime = j.getCalculatedFinishTime();
			}
		}
		int[] nodeOccupation = new int[maxFinishTime - currentTime];
		
		int counter = 0;
		int tmp = 0;
		for(BatchJob j : relevantJobs) {
			if(j.getStatus() == BatchJobStatus.SCHEDULED) {
				tmp = j.getStartTime();
				counter = tmp - currentTime;
			}
			else if(j.getStatus() == BatchJobStatus.RUNNING) {
				tmp = currentTime;
				counter = 0;
			}
			else if(j.getStatus() == BatchJobStatus.RESCHEDULED) {
				tmp = j.getScheduledRestartTime();
				counter = tmp - currentTime;
			}
			
			while(tmp < j.getCalculatedFinishTime()) {
				nodeOccupation[counter] += j.getAmountOfServers();
				tmp++;
				counter++;
			}
		}
		
		for(int i=0; i < nodeOccupation.length; i++) {
			if((9216 - nodeOccupation[i]) >= amountOfRequestedNodes) {
				return currentTime + i;
			}
		}
		
		return currentTime + maxFinishTime + 1;
	}

}
