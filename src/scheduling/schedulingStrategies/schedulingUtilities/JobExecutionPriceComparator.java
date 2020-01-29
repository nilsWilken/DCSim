package scheduling.schedulingStrategies.schedulingUtilities;

import java.util.Comparator;

import utilities.JobExecutionPrice;

public class JobExecutionPriceComparator implements Comparator<JobExecutionPrice> {

	@Override
	public int compare(JobExecutionPrice arg0, JobExecutionPrice arg1) {
		if(arg0.getCosts() < arg1.getCosts()) {
			return -1;
		}
		else if(arg0.getCosts() == arg1.getCosts()) {
			return 0;
		}
		else {
			return 1;
		}
	}

}
