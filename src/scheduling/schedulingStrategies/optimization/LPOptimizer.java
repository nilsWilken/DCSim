package scheduling.schedulingStrategies.optimization;

import java.util.ArrayList;
import java.util.List;

import eventHandling.EventType;
import eventHandling.JobEvent;
import hardware.DC;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import simulationControl.Setup;
import utilities.BatchJob;
import utilities.BatchJobStatus;

/**
 * This class creates a linear optimization problem for the optimization of the schedule during a DR event.
 * For the solving process, the lp_solve libraries are used.
 * 
 * @author nilsw
 *
 */
public class LPOptimizer {
	
	/**
	 * Creates optimization problem, solves it, and subsequently adjusts the schedule according to the found solution.
	 * @param relevantJobs List of all jobs that are relevant for the optimization.
	 * @param intervalStart Point in simulation time at which the interval for which the schedule should be optimized starts.
	 * @param intervalEnd Point in simulation time at which the interval for which the schedule should be optimized ends.
	 * @param handledDC DC to which the scheduler that uses this solver belongs.
	 */
	public void scheduleRelevantJobs(List<BatchJob> relevantJobs, int intervalStart, int intervalEnd, DC handledDC) {
		//Calculate interval length, if the simulation time is currently set to seconds, the interval length is calculated in minutes. Otherwise it is always calculated in simulation time.
		int intervalLength = (intervalEnd - intervalStart);
		
		if(Setup.secondsPerSimulationTimestep == 1) {
			intervalLength = Math.round((float)intervalLength/(float)60);
		}
		double[] powerConsumptionSums = new double[intervalLength];
		int[] nodeOccupationSums = new int[intervalLength];
		
		//If jobs are not pausable, all jobs that are currently running and all jobs that finish after the optimized interval cannot be shifted within the interval
		if(!Setup.jobsPausable) {
			ArrayList<BatchJob> tmp = new ArrayList<BatchJob>(relevantJobs);
			for(BatchJob job : tmp) {
				if(job.getStatus() == BatchJobStatus.RUNNING) {
					relevantJobs.remove(job);
					this.updateSums(powerConsumptionSums, nodeOccupationSums, intervalStart, intervalEnd, job);
				}
				else if(job.getCalculatedFinishTime() >= intervalEnd) {
					relevantJobs.remove(job);
					this.updateSums(powerConsumptionSums, nodeOccupationSums, intervalStart, intervalEnd, job);
				}
			}
		}
		
		//If there are less than two jobs, there is nothing to optimize.
		if(relevantJobs.size() < 2) {
			return;
		}
		
		//Set the amount of variables of the model
		int amountOfVariables = (intervalLength*relevantJobs.size())+1;
		
		//Calculate the runtime of each job within the interval that is optimized.
		int[] jobLengths = new int[relevantJobs.size()];
		for(int i=0; i < relevantJobs.size(); i++) {
			jobLengths[i] = this.calculateJobRuntimeInInterval(relevantJobs.get(i), intervalStart, intervalEnd);
			if(jobLengths[i] > intervalLength) {
				jobLengths[i] = intervalLength;
			}
		}
		
		//Utilities just for convienience and testing reasons
		String[] jobIDs = new String[amountOfVariables];
		jobIDs[0] = "z";
		int index = 0;
		for(int i=1; i < jobIDs.length; i++) {
			jobIDs[i] = relevantJobs.get(index).getId();
			if(i%intervalLength == 0) {
				index++;
			}
		}
		
		//START OF THE ACTUAL OPTIMIZING PART
		try {
			//Create new solver
			LpSolve solver = LpSolve.makeLp(0, amountOfVariables);
			
			//This mode makes the building process of the model faster when it is built row by row.
			solver.setAddRowmode(true);
			
			//The row array takes the multipliers for each variable of the model for each condition.
			//The colno array saves the column numbers of each position in the row array.
			double[] row = new double[amountOfVariables];
			int[] colno = new int[amountOfVariables];
			
			//Init the colno array
			for(int i=1; i <= colno.length; i++) {
				colno[i-1] = i;
			}
			
			//Set all but the objective variable to binary
			for(int i=2; i <= row.length; i++) {
				solver.setBinary(i, true);
			}
			
			//Create and add the conditions which ensure that each job runs for the specified runtime.
			for(int i=0; i < relevantJobs.size(); i++) {
				row = new double[amountOfVariables];
				for(int j=(i*intervalLength)+1; j <= ((i+1)*intervalLength); j++) {
					row[j] = 1;
				}
				solver.addConstraintex(amountOfVariables, row, colno, LpSolve.EQ, jobLengths[i]);
				
			}
			
			//Create and add the conditions which constrain the objective variable and ensure that at each point in simulation time the maximum number of nodes is not violated.
			double[] row2;
			for(int i=1; i <= intervalLength; i++) {
				row = new double[amountOfVariables];
				row2 = new double[amountOfVariables];
				row[0] = -1.0;
				index = 0;
				for(int j=i; j <= ((relevantJobs.size()-1)*intervalLength)+i; j+=intervalLength) {
					//Constraint for the objective variable.
					row[j] = relevantJobs.get(index).getTotalPowerConsumption()*relevantJobs.get(index).getAmountOfServers();
					row[j] = row[j]/10000.0;
					
					//Constraint for the active node number.
					row2[j] = (double)relevantJobs.get(index).getAmountOfServers()/1000.0;
					index++;
				}
				double powerConstant = 0.0;
				int nodeConstant = 0;
				if(powerConsumptionSums[i-1] > 0) {
					powerConstant = -powerConsumptionSums[i-1];
				}
				if(nodeOccupationSums[i-1] > 0) {
					nodeConstant = nodeOccupationSums[i-1];
				}
				solver.addConstraintex(amountOfVariables, row, colno, LpSolve.LE, powerConstant);
				solver.addConstraintex(amountOfVariables, row2, colno, LpSolve.LE, 9.216-nodeConstant);
			}
			
			//Create and add the conditions that ensure that each job is executed as a block and is not splitted into several parts.
			if (!Setup.jobsPausable) {
				int bound = intervalLength;
				index = 0;
				for (int i = 1; i < amountOfVariables; i++) {
					for (int j = i + jobLengths[index]; j <= bound; j++) {
						row = new double[amountOfVariables];
						row[i] = 1;
						row[j] = 1;
						solver.addConstraintex(amountOfVariables, row, colno, LpSolve.LE, 1);
					}
					if (i == bound) {
						bound += intervalLength;
					}
					if (i % intervalLength == 0) {
						index++;
					}
				}
			}

			//Turn the add-rowmode off again (necessary to make the solver work properly).
			solver.setAddRowmode(false);
			
			//Add the objective function to the model.
			row = new double[amountOfVariables];
			row[0] = 1;
			solver.setObjFnex(amountOfVariables, row, colno);
			solver.setMinim();
			//Writes the model into a file (in .lp format).
			solver.writeLp("model.lp");
			
			//Set a timeout to ensure that the solver does not block the simulator forever.
			solver.setTimeout(Setup.solverTimeout);	
			//Writes the output of the solver to a file instead of the system output.
			solver.setOutputfile("solverLog.txt");
			
			solver.setScaling(LpSolve.SCALE_EQUILIBRATE + LpSolve.SCALE_GEOMETRIC);
			solver.setBbDepthlimit(-10);
			solver.setObjBound(10000);
			
			//Solve the model.
			int solution = solver.solve();
			
			//If the solver only found a suboptimal solution (due to abort by timeout).
			if(solution == 1) {
				System.out.println("Solution is not optimal!");
			}
			//If the solver did not found any solution before the timeout. In this case the jobs are not rescheduled.
			else if(solution > 2) {
				System.out.println("No valid solution found in " +  Setup.solverTimeout + " seconds!");
				solver.deleteLp();
				return;
			}
			
//			System.out.println("Value of objective function: " + solver.getObjective());
			//Get the variable values of the solution.
			double[] sol = solver.getPtrVariables();
			
//			int timestamp = -1;
//			for(int i=0; i < sol.length; i++) {
//				System.out.println("Value of variable " + i + " " + jobIDs[i] + " " + timestamp + ": " + sol[i]);
//				timestamp++;
//				if(timestamp%intervalLength == 0 && i > 0) {
//					timestamp = 0;
//				}
//			}

			//Free the memory space which is blocked by the model.
			solver.deleteLp();
			
			//RESCHEDULE THE JOBS ACCORDING TO THE FOUND SOLUTION.
			if (handledDC != null) {
//				System.out.println("reschedule");
//				intervalLength = intervalEnd - intervalStart;
				int remainingRuntime;
				BatchJob cJob;
				for (int i = 0; i < relevantJobs.size(); i++) {
					cJob = relevantJobs.get(i);
//					System.out.println(cJob.getId() + " " + jobLengths[i]);
					for (int j = (i * intervalLength) + 1; j <= (i + 1) * intervalLength; j++) {
						//sol[j] will be 1 if the optimizer determined this job to run at the corresponding timestep.
						if (sol[j] > 0.9) {
							/*
							 * If a job is running at the beginning of the interval, there are two possibilities.
							 * The first possibility is that the job runs through the entire time of the interval and the second possibility is that the job finishes before the end of the optimized
							 * interval.
							 * In the first case, the job cannot be shifted by the optimizer and there are no reschedule actions necessary.
							 * In the second case, the optimizer might shift the job within the interval. When this is the case, the job has to be paused at the beginning of the interval and has to be
							 * restarted at the time which is specified by the optimizer.
							 */
							if (cJob.getStatus() == BatchJobStatus.RUNNING) {
								//Second case.
								if (((j-1) % intervalLength) > 0) {
									handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, intervalStart, cJob));
									cJob.setIsRequestedToPause(true);
									cJob.setIsAlreadyRescheduled(true);
									if(Setup.secondsPerSimulationTimestep == 1) {
										cJob.setScheduledRestartTime(intervalStart + (((j-1)%intervalLength)*60));
//										System.out.println(j + " " + ((j-2)%intervalLength) + " " + intervalLength);
									}
									else {
										cJob.setScheduledRestartTime(intervalStart + ((j-1)%intervalLength));
									}
//									System.out.println("True " + cJob.getId() + " " + intervalStart + " " + cJob.getScheduledRestartTime());
									handledDC.rescheduleEvent(cJob.getRestartEvent(), cJob.getScheduledRestartTime());
//									handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
								}
								break;
							}
							/*
							 * For jobs that are paused at the beginning of the interval, there are also two possibilities.
							 * The first possibility is that the job is currently scheduled as such that it finishes before the end of the interval.
							 * The second possibility is that the job is currently scheduled as such that it will not finish before the end of the interval.
							 * 
							 * In the first case, the optimizer might shift the job within the interval. However, it will be ensured that it still finishes within the interval. Thus,
							 * the only rescheduling action that has to be taken might be a change of the restart and finish events.
							 * 
							 * In the second case, the optimizer also might shift the job to an earlier restart time. In this case, in order to ensure that no additional workload is added to the
							 * interval, the job has to be paused again after it has reached it originally scheduled runtime within the interval.
							 */
							else if(cJob.getStatus() == BatchJobStatus.RESCHEDULED) {
								//Done in both cases.
								if(Setup.secondsPerSimulationTimestep == 1) {
									cJob.setScheduledRestartTime(intervalStart + (((j-1)%intervalLength)*60));
								}
								else {
									cJob.setScheduledRestartTime(intervalStart + ((j-1)%intervalLength));
								}
								handledDC.rescheduleEvent(cJob.getRestartEvent(), cJob.getScheduledRestartTime());
//								handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
								
								//Only done in the second case.
								if (cJob.getCalculatedFinishTime() > intervalEnd) {
									remainingRuntime = intervalEnd - cJob.getScheduledRestartTime();
									if (Setup.secondsPerSimulationTimestep == 1) {
										remainingRuntime = (int)((float) remainingRuntime / (float) 60);
									}

									if (remainingRuntime > jobLengths[i]) {
										if (Setup.secondsPerSimulationTimestep == 1) {
//											System.out.println(cJob.getScheduledRestartTime() +  " " + cJob.getScheduledRestartTime() + ((jobLengths[i] * 60)));
											int pauseEventTime = cJob.getScheduledRestartTime() + ((jobLengths[i])*60);
											if(pauseEventTime > intervalEnd) {
												pauseEventTime = intervalEnd;
											}
											handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, pauseEventTime, cJob));
//											System.out.println(cJob.getId() + " " + cJob.getStatus() + " " + cJob.getStartTime() + " " + (cJob.getStartTime() + ((jobLengths[i] * 60))));
										}
										else {
											handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, cJob.getScheduledRestartTime() + jobLengths[i], cJob));
										}
//										handledDC.unscheduleEvent(cJob.getFinishEvent());
									}
								}
								break;
							}
							/*
							 * When a job is scheduled at the beginning of the interval, there are basically the same cases as for paused jobs. The only difference is that the optimizer
							 * might change the start time instead of the restart time.
							 */
							else if(cJob.getStatus() == BatchJobStatus.SCHEDULED) {
								//Done for both cases.
								if(Setup.secondsPerSimulationTimestep == 1) {
									cJob.setStartTime(intervalStart + (((j-1)%intervalLength)*60));
								}
								else {
									cJob.setStartTime(intervalStart + ((j-1)%intervalLength));
								}
								handledDC.rescheduleEvent(cJob.getStartEvent(), cJob.getStartTime());
//								handledDC.rescheduleEvent(cJob.getFinishEvent(), cJob.getCalculatedFinishTime());
								
								//Only done in the second case.
								if (cJob.getCalculatedFinishTime() > intervalEnd) {
									remainingRuntime = intervalEnd - cJob.getStartTime();
									if (Setup.secondsPerSimulationTimestep == 1) {
										remainingRuntime = (int)((float) remainingRuntime / (float) 60);
									}

									if (remainingRuntime > jobLengths[i]) {
//										System.out.println("info: " + cJob.getStartEvent().getTimestamp() + " " + cJob.getStartTime() + " " + jobLengths[i] + " " + remainingRuntime + " " + (cJob.getStartTime() + (jobLengths[i])*60) + " " + cJob.getFinishEvent().getTimestamp());
										if (Setup.secondsPerSimulationTimestep == 1) {
//											System.out.println(cJob.getStartTime() +  " " + (cJob.getStartTime() + ((jobLengths[i] * 60))) + " " + cJob.getId());
											int pauseEventTime = cJob.getScheduledRestartTime() + ((jobLengths[i])*60);
											if(pauseEventTime > intervalEnd) {
												pauseEventTime = intervalEnd;
											}
											handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, pauseEventTime, cJob));
//											System.out.println(cJob.getId() + " " + cJob.getStatus() + " " + cJob.getStartTime() + " " + (cJob.getStartTime() + ((jobLengths[i] * 60))));
										}
										else {
											handledDC.scheduleEvent(new JobEvent(EventType.JOB_PAUSE, cJob.getStartTime() + jobLengths[i], cJob));
										}
										handledDC.unscheduleEvent(cJob.getFinishEvent());
									}
								}
								break;
							}
						}
					}
				}
			}
			
		} catch (LpSolveException e) {
			e.printStackTrace();
			System.out.println("EXCEPTION!");
		}
		
	}
	
	/**
	 * 
	 * @param job Instance of BatchJob for which the runtime should be calculated.
	 * @param intervalStart Simulation time step that represents the start of the interval that is optimized.
	 * @param intervalEnd Simulation time step that represents the end of the interval that is optimized.
	 * @return The originally scheduled runtime of the BatchJob instance within the interval, which is framed by the two simulation time steps.
	 */
	private int calculateJobRuntimeInInterval(BatchJob job, int intervalStart, int intervalEnd) {
		int jobRuntime = 0;
		int intervalLength;
		if(Setup.secondsPerSimulationTimestep == 1) {
			intervalLength = (int)((float)(intervalEnd - intervalStart)/(float)60) + 1;
		}
		else {
			intervalLength = intervalEnd - intervalStart;
		}
		if(job.getStatus() == BatchJobStatus.RUNNING) {
			if(job.getCalculatedFinishTime() >= intervalEnd) {
				jobRuntime = intervalLength;
			}
			else {
				if(Setup.secondsPerSimulationTimestep == 1) {
					jobRuntime = (int)((float)job.getRemainingRuntimeInSimulationTime()/(float)60) + 1;
				}
				else {
					jobRuntime = job.getRemainingRuntimeInSimulationTime();
				}
			}
		}
		else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
			if(job.getCalculatedFinishTime() >= intervalEnd) {
				jobRuntime = intervalEnd - job.getScheduledRestartTime();
				if(Setup.secondsPerSimulationTimestep == 1) {
					jobRuntime = (int)((float)jobRuntime/(float)60) + 1;
				}
			}
			else {
				jobRuntime = job.getCalculatedFinishTime() - job.getScheduledRestartTime();
				if(Setup.secondsPerSimulationTimestep == 1) {
					jobRuntime = (int)((float)jobRuntime/(float)60) + 1;
				}
			}
		}
		else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
			if(job.getCalculatedFinishTime() >= intervalEnd) {
				jobRuntime = intervalEnd - job.getStartTime();
				if(Setup.secondsPerSimulationTimestep == 1) {
					jobRuntime = (int)((float)jobRuntime/(float)60) + 1;
				}
			}
			else {
				jobRuntime = job.getCalculatedFinishTime() - job.getStartTime();
				if(Setup.secondsPerSimulationTimestep == 1) {
					jobRuntime = (int)((float)jobRuntime/(float)60) + 1;
				}
			}
//			System.out.println(job.getStartTime() + " " + job.getCalculatedFinishTime() + " " + jobRuntime);
		}
//		System.out.println("runtime: " + jobRuntime);
		return jobRuntime;
	}
	
	private void updateSums(double[] powerConsumptionSums, int[] nodeOccupationSums, int intervalStart, int intervalEnd, BatchJob job) {
		int tmp = 0;
		if(job.getStatus() == BatchJobStatus.RUNNING) {
			tmp = intervalStart;
		}
		else if(job.getStatus() == BatchJobStatus.SCHEDULED) {
			tmp = job.getStartTime();
		}
		else if(job.getStatus() == BatchJobStatus.RESCHEDULED) {
			tmp = job.getScheduledRestartTime();
		}
		
		
		int counter = tmp - intervalStart;
		if(Setup.secondsPerSimulationTimestep == 1) {
			counter = (tmp-intervalStart)/60;
		}
		while(tmp < intervalEnd && tmp <= job.getCalculatedFinishTime()) {
			powerConsumptionSums[counter] += job.getTotalPowerConsumption();
			nodeOccupationSums[counter] += job.getAmountOfServers();
			counter++;
			if(Setup.secondsPerSimulationTimestep == 1) {
				tmp += 60;
			}
			else {
				tmp++;
			}
		}
	}
	
//	public static void main(String[] args) {
//		BatchJob j1 = new BatchJob("j1", 2.3, 400, 166, 500, null, 0, 10, 10000, 10);
//		BatchJob j2 = new BatchJob("j2", 2.3, 405, 300.8, 500, null, 0, 10, 10000, 10);
//		BatchJob j3 = new BatchJob("j3", 2.3, 1000, 321.4, 10000, null, 0, 10, 10000, 10);
//		BatchJob j4 = new BatchJob("j4", 2.3, 200, 440.8, 500, null, 0, 10, 100000, 10);
//		
//		j1.setStatus(BatchJobStatus.RUNNING);
//		j2.setStatus(BatchJobStatus.SCHEDULED);
//		j3.setStatus(BatchJobStatus.SCHEDULED);
//		j4.setStatus(BatchJobStatus.SCHEDULED);
//		
//		List<BatchJob> relevantJobs = new ArrayList<BatchJob>();
//		relevantJobs.add(j1);
//		relevantJobs.add(j2);
//		relevantJobs.add(j3);
//		relevantJobs.add(j4);
//		
//		LPOptimizer optimizer = new LPOptimizer();
//		optimizer.scheduleRelevantJobs(relevantJobs, 0, 900, null);
//	}

}
