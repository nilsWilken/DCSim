package de.uma.dcsim.scheduling.schedulingStrategies;

import de.uma.dcsim.scheduling.schedulingStrategies.timeSortScheduling.FirstInFirstOutScheduling;
import de.uma.dcsim.scheduling.schedulingStrategies.timeSortScheduling.ShortestTimeToDeadlineFirst;
import de.uma.dcsim.simulationControl.Setup;

public enum SchedulingStrategyType {
	
	SHORTEST_TIME_TO_DEADLINE_FIRST,
	MINIMUM_COST,
	FIRST_IN_FIRST_OUT;
	
	public static SchedulingStrategyType parseFromString(String name) {
		switch(name.toLowerCase()) {
		case "shortesttimetodeadlinefirst":
			return SHORTEST_TIME_TO_DEADLINE_FIRST;
		case "minimumcost":
			return MINIMUM_COST;
		case "firstinfirstout":
			return FIRST_IN_FIRST_OUT;
		default:
			return SHORTEST_TIME_TO_DEADLINE_FIRST;
		}
	}
	
	public static SchedulingStrategy getSchedulingStrategyByType(SchedulingStrategyType type) {
		switch(type) {
		case SHORTEST_TIME_TO_DEADLINE_FIRST:
			return new ShortestTimeToDeadlineFirst();
		case FIRST_IN_FIRST_OUT:
			return new FirstInFirstOutScheduling();
		case MINIMUM_COST:
			return new ScheduleForMinimumCost(86400, 300);
		default:
			return new ShortestTimeToDeadlineFirst();
		}
	}

}
