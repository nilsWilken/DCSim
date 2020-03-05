package de.uma.dcsim.SLAModels;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobParser;
import de.uma.dcsim.utilities.Constants;

public class SLADeadlineCreator {
	
	private static String originalWorkloadTrace = ("C:/Users/nwilken/Documents/Forschung/MasterThesis_Paper/ACM_e-Energy_Paper/DRDCSim_Setups/resources/workloadTraces/superMUC_jobs.csv");
	private static String outputPath = ("C:/Users/nwilken/Documents/Forschung/MasterThesis_Paper/ACM_e-Energy_Paper/DRDCSim_Setups/resources/workloadTraces/superMUC_jobs_SoftSLA.csv");
	
	
	public static void main(String[] args) {
		StandardSLAModel slaModel = new StandardSLAModel();
		try {
			List<BatchJob> oJobs = BatchJobParser.parseJobFile(originalWorkloadTrace, Constants.getDateFormat().parse("01.01.2014 00:00:00"));
			System.out.println(Constants.getDateFormat().format(Constants.getDateFormat().parse("01.01.2014 00:00:00")));
			List<BatchJob> tJobs = new ArrayList<BatchJob>();
			
			for(BatchJob job : oJobs) {
				job.setSLADeadline(slaModel.createDeadline(job.getStartTime(), job.getDurationInSimulationTime(), 2.0));
				tJobs.add(job);
			}
			
			BatchJobParser.writeBatchJobFile(outputPath, tJobs, Constants.getDateFormat().parse("01.01.2014 00:00:00"));
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
