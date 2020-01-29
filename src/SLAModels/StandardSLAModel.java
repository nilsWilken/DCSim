package SLAModels;

import java.util.Random;

public class StandardSLAModel implements SLAModel {
	
	public int createDeadline(int scheduledStartTime, double duration) {
		int deadline = scheduledStartTime;
		
		//Add job execution time
		deadline = deadline + (int)duration;
		
		//Add puffer time
		Random rand = new Random();
		double delta = rand.nextDouble()*2.0;
		deadline = deadline + (int)(duration*delta);
		
		return deadline;
	}
	
	public double calculateSLAFee(double delay, double usagePrice) {
		double fee = 0;
		
		fee = usagePrice*delay;
		
		return fee;
	}

}
