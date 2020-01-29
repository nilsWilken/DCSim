package de.uma.dcsim.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//This class is only for testing purposes

public class NodeAmountTester {
	
	public static void main(String[] args) {
		BatchJobParser parser = new BatchJobParser();
		ArrayList<BatchJob> jobs;
		
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		try {
			jobs = (ArrayList<BatchJob>)parser.parseJobFile("src/main/resources/superMUC_jobs_january.csv", format.parse("01.01.2014 00:00:00"));
//			printNodeAmounts(jobs, format.parse("01.01.2014 00:00:00"), format.parse("30.01.2014 23:59:59"));
			printNodeAmounts(jobs, format.parse("01.01.2014 00:00:00"), format.parse("05.01.2014 23:59:59"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void printNodeAmounts(List<BatchJob> jobs, Date startDate, Date endDate) {
		
		Date tmp = new Date(startDate.getTime());
		int currentNodeSumCalculated = 0;
		int currentNodeSumParsed = 0;
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
//		for(BatchJob job : jobs) {
//			System.out.println(format.format(job.getEndTime()) + " " + format.format(job.getCalculatedFinishTime()));
//		}
		
		while(tmp.before(endDate)) {
			for(BatchJob job : jobs) {
//				System.out.println(format.format(job.getEndTime()) + " " + format.format(job.getCalculatedFinishTime()));
				if(tmp.equals(job.getStartTime())) {
					currentNodeSumCalculated += job.getAmountOfServers();
					currentNodeSumParsed += job.getAmountOfServers();
				}
				if(tmp.equals(job.getCalculatedFinishTime())) {
//					System.out.println(format.format(job.getEndTime()) + " " + format.format(job.getCalculatedFinishTime()));
					currentNodeSumCalculated -= job.getAmountOfServers();
				}
//				if(tmp.equals(job.getEndTime())) {
//					currentNodeSumParsed -= job.getAmountOfServers();
//				}
				
			}
//			System.out.println(format.format(tmp) + ": " + currentNodeSumCalculated);
			if(currentNodeSumParsed != currentNodeSumCalculated) {
				System.out.println("Not same! " + format.format(tmp) + " " + (currentNodeSumParsed - currentNodeSumCalculated));
				if(Math.abs(currentNodeSumParsed - currentNodeSumCalculated) > 10) {
					break;
				}
			}
			tmp.setTime(tmp.getTime()+1000);
			
		}
	}
	
	public static void printNodeAverages(List<BatchJob> jobs, Date startDate, Date endDate, long millisecondsPerTimestep) {
		Date tmp = new Date(startDate.getTime());
		Date tmp2 = new Date(startDate.getTime() + (millisecondsPerTimestep - 1000));
		double nodeSum = 0;
		double overallNodeSum = 0;
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		while(tmp.before(endDate)) {
			for(BatchJob job : jobs) {
				if(tmp.equals(job.getStartTime())) {
					nodeSum += job.getAmountOfServers();
				}
//				if(tmp.equals(job.getEndTime())) {
//					nodeSum -= job.getAmountOfServers();
//				}
			}
			tmp.setTime(tmp.getTime() + 1000);
			
			if(tmp.after(tmp2)) {
				System.out.println(format.format(tmp));
				overallNodeSum /= (millisecondsPerTimestep/1000);
				System.out.println(overallNodeSum);
				tmp2.setTime(tmp.getTime() + (millisecondsPerTimestep - 1000));
				overallNodeSum = 0;
				
			}
			overallNodeSum += nodeSum;
		}
	}
	
	public static void printJobAverages(List<BatchJob> jobs, Date startDate, Date endDate, long millisecondsPerTimestep) {
		Date tmp = new Date(startDate.getTime());
		Date tmp2 = new Date(startDate.getTime() + (millisecondsPerTimestep - 1000));
		double jobSum = 0;
		double overallJobSum = 0;
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		while(tmp.before(endDate)) {
			for(BatchJob job : jobs) {
				if(tmp.equals(job.getStartTime())) {
					jobSum ++;
				}
//				if(tmp.equals(job.getEndTime())) {
//					jobSum--;
//				}
			}
			tmp.setTime(tmp.getTime() + 1000);
			
			if(tmp.after(tmp2)) {
				System.out.println(format.format(tmp));
				overallJobSum /= (millisecondsPerTimestep/1000);
				System.out.println(overallJobSum);
				tmp2.setTime(tmp.getTime() + (millisecondsPerTimestep - 1000));
				overallJobSum = 0;
				
			}
			overallJobSum += jobSum;
		}
	}
	
	public static void printAverageJobPower(List<BatchJob> jobs, Date startDate, Date endDate, long millisecondsPerTimestep) {
		Date tmp = new Date(startDate.getTime());
		Date tmp2 = new Date(startDate.getTime() + (millisecondsPerTimestep - 1000));
		double jobPowerSum = 0;
		double overallJobPowerSum = 0;
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		while(tmp.before(endDate)) {
			for(BatchJob job : jobs) {
				if(tmp.equals(job.getStartTime())) {
					jobPowerSum += (job.getAmountOfServers()*job.getParsedAveragePowerConsumption());
				}
//				if(tmp.equals(job.getEndTime())) {
//					jobPowerSum -= (job.getAmountOfServers()*job.getAveragePowerConsumption());
//				}
			}
			tmp.setTime(tmp.getTime() + 1000);
			
			if(tmp.after(tmp2)) {
//				System.out.println(format.format(tmp));
				overallJobPowerSum /= (millisecondsPerTimestep/1000);
				System.out.println(overallJobPowerSum/1000.0);
				tmp2.setTime(tmp.getTime() + (millisecondsPerTimestep - 1000));
				overallJobPowerSum = 0;
				
			}
			overallJobPowerSum += jobPowerSum;
		}
	}

}
