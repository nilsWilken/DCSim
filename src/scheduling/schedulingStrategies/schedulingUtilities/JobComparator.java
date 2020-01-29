package scheduling.schedulingStrategies.schedulingUtilities;

import java.util.Comparator;

/**
 * This class can be used to compare to instances of JobSortContainer.
 * @author nilsw
 *
 */
public class JobComparator implements Comparator<JobSortContainer> {

	public int compare(JobSortContainer container1, JobSortContainer container2) {
		if(container1.getTheta() < container2.getTheta()) {
			return -1;
		}
		else if(container1.getTheta() == container2.getTheta()) {
			return 0;
		}
		else {
			return 1;
		}
	}

}
