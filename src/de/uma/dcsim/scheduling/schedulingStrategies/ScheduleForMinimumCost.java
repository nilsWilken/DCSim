package de.uma.dcsim.scheduling.schedulingStrategies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.uma.dcsim.energyPriceModels.EnergyPriceModelSelector;
import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities.JobExecutionPriceComparator;
import de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import de.uma.dcsim.simulationControl.Setup;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobStatus;
import de.uma.dcsim.utilities.JobExecutionPrice;

public class ScheduleForMinimumCost implements SchedulingStrategy {

	private int lookAheadInterval;
	private int startTimeStepSize;
	private Date startDate;
	
	public ScheduleForMinimumCost(int lookAheadInterval, int startTimeStepSize) {
		this.lookAheadInterval = lookAheadInterval;
		this.startTimeStepSize = startTimeStepSize;
	}

	@Override
	public void scheduleNextInterval(List<BatchJob> runningJobs, List<BatchJob> submittedJobs,
			List<BatchJob> scheduledJobs, List<BatchJob> pausedJobs, int intervalLength, int currentTime,
			int[] nodeOccupationPlan, DC handledDC) {
//		System.out.println("Scheduling started!");
//		if(currentTime%3600 == 0) {
//			System.out.println("Current time: " + (currentTime/3600));
//		}
//		System.out.println(currentTime);

//		List<BatchJob> occupationPlanRelevantJobs = new ArrayList<BatchJob>();
//		occupationPlanRelevantJobs.addAll(scheduledJobs);
//		occupationPlanRelevantJobs.addAll(runningJobs);
//		occupationPlanRelevantJobs.addAll(pausedJobs);
		
		this.startDate = new Date(handledDC.getSimStartTime().getTime() + ((long)currentTime*1000L));

		int[] nodeOccupationPlan2 = SchedulingStrategyUtilities.getNodeOccupationPlanEventBased(handledDC, this.lookAheadInterval,
				currentTime);

		int intervalEnd = currentTime + this.lookAheadInterval;
		int tmp;
		boolean scheduled = true;
		BatchJob pJob;

		ArrayList<BatchJob> allJobs = new ArrayList<BatchJob>(pausedJobs);
		allJobs.addAll(submittedJobs);

		for (int i = 0; i < allJobs.size(); i++) {
			pJob = allJobs.get(i);
			tmp = currentTime;
			scheduled = false;

			ArrayList<JobExecutionPrice> prices = new ArrayList<JobExecutionPrice>();
			int simStepsPerStartTimeStep = this.startTimeStepSize/Setup.secondsPerSimulationTimestep;
			for (int j = tmp; j <= intervalEnd; j += simStepsPerStartTimeStep) {
				for (int freq = 12; freq <= 27; freq += 1) {
					prices.add(new JobExecutionPrice(j,
							this.calculateCostForJobConfiguration(pJob, ((double) freq / 10.0), j), ((double)freq/10.0)));
				}
			}
			prices.sort(new JobExecutionPriceComparator());
			
//			pJob.setFrequency(prices.get(0).getFrequency());
//			System.out.println(pJob.getTotalPowerConsumption());
//			System.out.println(pJob.getDurationInSimulationTime());
//			System.out.println(prices.get(0).getCosts());
//			System.out.println(prices.get(0).getStartTime());
//			System.exit(0);

			int index = 0;
			while (!scheduled && index < prices.size()) {
//				System.out.println(prices.get(index));
				pJob.setStartTime(prices.get(index).getStartTime());
				pJob.setFrequency(prices.get(index).getFrequency());
				if (SchedulingStrategyUtilities.checkSchedulingFeasibilityForJob(nodeOccupationPlan2, pJob,
						currentTime)) {
//					if(pJob.getId().equals("srv04-ib.297826")) {
//						System.out.println("true\t" + pJob.getStartTime() + "\t " + pJob.getAmountOfServers() + "\t " + nodeOccupationPlan2[pJob.getStartTime()-currentTime] + "\t" + currentTime);
//					}
					SchedulingStrategyUtilities.updateNodeOccupationPlan(nodeOccupationPlan2, pJob, currentTime);
//					if(pJob.getId().equals("srv04-ib.297826")) {
//						System.out.println("true\t" + pJob.getStartTime() + "\t " + pJob.getAmountOfServers() + "\t " + nodeOccupationPlan2[pJob.getStartTime()-currentTime]);
//					}
					handledDC.rescheduleEvent(pJob.getStartEvent(), pJob.getStartTime());
					submittedJobs.remove(pJob);
					scheduledJobs.add(pJob);
					pJob.setStatus(BatchJobStatus.SCHEDULED);
					scheduled = true;
				}
				index++;
			}
			if(!scheduled) {
//				System.out.println(pJob.getId() + " not scheduled!");
				break;
			}
		}
//		System.out.println("\t Scheduling finished!\n");
		
	}

	public double calculateCostForJobConfiguration(BatchJob job, double frequency, int startTime) {
		BatchJob copy = job.deepCopy();
		copy.setStartTime(startTime);
		copy.setFrequency(frequency);
		copy.setActualFinsihingTime(copy.getCalculatedFinishTime());
		
		Date calculatedFinishTmp = new Date(this.startDate.getTime() + ((long)(copy.getCalculatedFinishTime()*Setup.secondsPerSimulationTimestep)*1000L));
		Date startTmp = new Date(this.startDate.getTime() + ((long)(startTime*Setup.secondsPerSimulationTimestep)*1000L));
		Date tmp = new Date(startTmp.getTime());
		
		Calendar current = new GregorianCalendar();
		current.setTime(tmp);
		
		Calendar start = new GregorianCalendar();
		start.setTime(startTmp);
		
		Calendar calculatedFinish = new GregorianCalendar();
		calculatedFinish.setTime(calculatedFinishTmp);
		
		int simulationStepsPerHour = 3600/Setup.secondsPerSimulationTimestep;
		int currentSimulationStep = startTime;
		
		
		double slaCost = copy.calculateSLACosts(Setup.usagePrice);

		double powerUsage = copy.getTotalPowerConsumption()/1000.0;
		
//		double energyPrice;
		double usedEnergy;
		double energyCosts = 0;
		
		current.roll(Calendar.HOUR, 1);
		current.set(Calendar.MINUTE, 0);
		current.set(Calendar.SECOND, 0);
		
//		int counter = 0;
		
		if(current.after(calculatedFinish)) {
//			usedEnergy = powerUsage / (((60/simulationStepsPerHour)*copy.getDurationInSimulationTime())/60);
			usedEnergy = powerUsage * (copy.getDurationInSimulationTime()/simulationStepsPerHour);
			energyCosts += EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(startTime)*usedEnergy;
//			counter++;
		}
		else {
			int secondsInFirstHour = 3600 - ((start.get(Calendar.MINUTE)*60) + start.get(Calendar.SECOND));
			usedEnergy = (secondsInFirstHour/3600)*powerUsage;
			energyCosts += EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(startTime)*usedEnergy;
//			counter++;
			
			currentSimulationStep += (secondsInFirstHour/Setup.secondsPerSimulationTimestep);
			
			while((currentSimulationStep += simulationStepsPerHour) <= copy.getCalculatedFinishTime()) {
				energyCosts += EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(currentSimulationStep-1)*powerUsage;
//				counter++;
			}
			currentSimulationStep -= simulationStepsPerHour;
			int remainingSimulationSteps = copy.getCalculatedFinishTime()-currentSimulationStep;
			usedEnergy = powerUsage*(remainingSimulationSteps/simulationStepsPerHour);
			energyCosts += usedEnergy*EnergyPriceModelSelector.getEnergyPriceInCentPerKWh(copy.getCalculatedFinishTime());
//			counter++;
			
//			System.out.println(counter);
		}

		return slaCost + energyCosts;
	}

}
