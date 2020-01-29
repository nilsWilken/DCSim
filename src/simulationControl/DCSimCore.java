/*
 * %%
 * Copyright (C) 2012 University of Mannheim - Chair of Software Engineering
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * 
 */

package simulationControl;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import database.ColumnType;
import database.DatabaseRecord;
import database.EvaluationTable;
import database.SqLiteDBHandler;
import drEventHandling.SimpleDREventHandler;
import eventHandling.DRRequestEvent;
import eventHandling.Event;
import eventHandling.EventType;
import eventHandling.ServerEvent;
import hardware.DC;
import hardware.Server;
import scheduling.SchedulingResult;
import utilities.BatchJob;
import utilities.ReserveProvisionType;

/**
 * @author University of Mannheim
 * 
 */
public class DCSimCore {
	private static Thread simulationThread;
	private static List<DC> dcs;
	private static List<DCFederation> federations;
	private static InputStream io;
	private static int clock;
	private static int HistoricalTimeFrame;
	private static int HistoricalNumberOfElements;
	private static int length;
	private static boolean simulatorIsRunning;
	private static Calendar simStartTime;
	private static SqLiteDBHandler dbHandler;

	/**
	 * @param in
	 */
	public static void DCSimCore(final InputStream in) {
		io = in;
		setClock(0);
		length = Integer.MAX_VALUE;
		
		simulationThread = new Thread() {
			public void run() {
//				String generalSimulationRecordTableName = "General_Simulation_Records_" + TableType.getTablePostfix(TableType.GENERAL_SIMULATION_MONITORING_TABLE);
//				String drRequestRecordTableName = "DR_Request_Records_" + TableType.getTablePostfix(TableType.DR_REQUEST_MONITORING_TABLE);
				
//				String generalSimulationRecordTableName = EvaluationTable.getTableName(EvaluationTable.GENERAL_EVALUATION_TABLE);
//				String drRequestRecordTableName = EvaluationTable.getTableName(EvaluationTable.DR_REQUEST_RECORD_TABLE);
				
				setSimulatorIsRunning(true);				
				int i = 0;
				Setup setup = new Setup();
				setup.readDC(io);
				
				// Setting up all configurations
				if (length > 0)
					length = setup.getSimLength();
				else {
					length = setup.getSimLength();
				}
				dcs = setup.getDcs();

				
				setFederations(setup.getDcFederations());
			
				// Starting the Simulation
				try {
					io.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				for (DC dc : dcs) {
					dc.setupDC();
				}
				
				SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
				Date simStart = new Date();
				Date simEnd = new Date();
				
				try {
					simStart = format.parse("01.01.2014 00:00:00");
					simEnd = new Date(simStart.getTime() + ((long)length*(long)1000));
				}catch(ParseException e) {
					e.printStackTrace();
				} catch (java.text.ParseException e) {
					e.printStackTrace();
				}
				
				
				// starting the update loop
				System.out.println(length);

				//Setup variables that are used for the monitoring database
				DatabaseRecord cRecord;
				Object[] values;
				ColumnType[] generalSimulationSchema = EvaluationTable.getTableSchema(EvaluationTable.GENERAL_EVALUATION_TABLE);
				ColumnType[] drRequestSchema = EvaluationTable.getTableSchema(EvaluationTable.DR_REQUEST_RECORD_TABLE);
				
				dbHandler = new SqLiteDBHandler(Setup.evaluationDatabasePath);
				for(EvaluationTable eTable : EvaluationTable.values()) {
					dbHandler.createRecordTables(simStart, simEnd, eTable);
				}
//				dbHandler.createRecordTables(generalSimulationRecordTableName, simStart, simEnd, EvaluationTable.GENERAL_EVALUATION_TABLE);
//				dbHandler.createRecordTables(drRequestRecordTableName, simStart, simEnd, EvaluationTable.DR_REQUEST_RECORD_TABLE);

				//Used to measure the runtime of a simulation run
				long start = (new Date()).getTime();
				
		
				
				List<Event> drRequestEvents;
				double averageRemainingRuntime;
				double averageNodesPerJob;
				SchedulingResult result;
				
				//Simulation loop
				while (i < length) {
					setClock(i++);
					for (DC dc : dcs) {
						
						//If DR request event is scheduled for the current point in simulation time
						if(dc.getDRRequestEvents(dc.getClock()) != null) {
							
							//Ensure that all jobs are scaled back to their original frequency (necessary to handle directly subsequent DR events properly)
							for(BatchJob job : dc.getRunningJobs()) {
								if(job.getFrequency() != job.getOriginalFrequency()) {
									job.setFrequency(job.getOriginalFrequency());
									for(Server s : job.getAssignedServers()) {
										dc.scheduleEvent(new ServerEvent(EventType.SERVER_UPDATE, dc.getClock(), s));
									}
									dc.rescheduleEvent(job.getFinishEvent(), job.getCalculatedFinishTime());
								}
							}
							dc.updateJobAllocation(false, false);
							
							//Get list of scheduled DR request events
							drRequestEvents = dc.getDRRequestEvents(dc.getClock());
							
							//If there is more than one event scheduled
							if(drRequestEvents.size() > 1) {
								System.out.println("More than one DR request per timestep!");
							}
							
							//Retrieve DR request parameters from the event
							double adjustmentHeight = ((DRRequestEvent)drRequestEvents.get(0)).getDRRequest().getAdjustmentHeight();
							double reward = ((DRRequestEvent)drRequestEvents.get(0)).getDRRequest().getReward();
							int length = ((DRRequestEvent)drRequestEvents.get(0)).getDRRequest().getLength();
							ReserveProvisionType provisionType = ((DRRequestEvent)drRequestEvents.get(0)).getDRRequest().getProvisionType();
							
							//Calculate current workload characteristics
							averageRemainingRuntime = 0;
							averageNodesPerJob = 0;
							for(BatchJob job : dc.getRunningJobs()) {
								averageRemainingRuntime += job.getRemainingRuntimeInSimulationTime();
								averageNodesPerJob += job.getAmountOfServers();
							}
							averageRemainingRuntime /= dc.getRunningJobs().size();
							averageNodesPerJob /= dc.getRunningJobs().size();
							
							//Get DREventHandler of the DC
							SimpleDREventHandler handler = (SimpleDREventHandler)dc.getDREventHandler();
							
							//Determine optimal configuration of power demand flexibility provision techniques for the reaction to the DR event
							double[] optimalComb = handler.optimizePowerDemandFlexibilityCost(adjustmentHeight, provisionType, length, reward);

							//If such a combination was found
							if(optimalComb[0] != 0 || optimalComb[1] != 0) {
								handler.setShiftingFraction(optimalComb[0]);
								handler.setScalingFrequency(optimalComb[1]);
								
								result = handler.issueDemandResponseRequest(adjustmentHeight, provisionType, length, reward);
								values = new Object[] {dc.getCurrentDate().getTime(), optimalComb[1], optimalComb[0], result.getActuallyShiftedNodeFraction(), 0.0, averageRemainingRuntime, averageNodesPerJob};
								cRecord = new DatabaseRecord(drRequestSchema, values);
								dbHandler.insertRecord(cRecord);
							}
							//Otherwise enter some default values to the database (indicates that a DR event was not handled successfully)
							else {
								values = new Object[] {dc.getCurrentDate().getTime(), optimalComb[1], optimalComb[0], 0.0, 0.0, averageRemainingRuntime, averageNodesPerJob};
								cRecord = new DatabaseRecord(drRequestSchema, values);
								dbHandler.insertRecord(cRecord);
								dc.scheduleJobs();
							}
						}
						//If no DR events are scheduled for the current point in simulation time
						else {
							dc.scheduleJobs();
						}
						
						//Update resource allocation of the DC
						dc.updateJobAllocation(true, true);
						dc.removeKey();
						
						//Enter general monitoring values to the monitoring database
						values = new Object[] {dc.getCurrentDate().getTime(), dc.getOccupiedServer().size(), dc.getRunningJobs().size(), dc.getOverallCurrentPC()/1000.0, dc.getCurrentITPC()/1000.0, dc.getCurrentHVACPC()/1000.0, dc.getCurrentJobPC()/1000.0, dc.getEnergyCostOfCurrentTimestep(), dc.getSLACostOfCurrentTimestep()};
						cRecord = new DatabaseRecord(generalSimulationSchema, values);
						dbHandler.insertRecord(cRecord);
					}
					
					//Print the current date for each day in real time and perform commit on the database
					if(clock%86400 == 0) {
						System.out.println(format.format(dcs.get(0).getCurrentDate()));
						dbHandler.commit();
					}
//					setClock(i++);
				}
				//Commit last uncommited changes on the databse
				dbHandler.commit();
				
				//Print runtime of the current simulation run to the console
				System.out.println("Length: " + (((new Date()).getTime() -start)/1000));
				System.out.println(dcs.get(0).getFinishedJobs().size());
				setSimulatorIsRunning(false);
			}
		};
	}

	/**
	 * Starts a simulation
	 * @return
	 */
	public static boolean startSimulation() {
		try {
			setSimulatorIsRunning(true);
			simulationThread.start();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean stopSimulation() {
		try {
			simulationThread.stop();
			setSimulatorIsRunning(false);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * @param federations2
	 */
	private static void setFederations(List<DCFederation> federations2) {
		federations = federations2;
	}

	/**
	 * @return
	 */
	public static List<DCFederation> getFederations() {
		return federations;
	}

	public static DCFederation getFederationOfDC(int dcID) {
		DCFederation theFederation = null;
		for (DCFederation fed : federations) {
			for (DC dc : fed.getDcs()) {
				if (dc.getId() == dcID) {
					theFederation = fed;
					break;
				}
			}
			if (theFederation != null) {
				break;
			}
		}
		return theFederation;
	}

	@SuppressWarnings("static-access")
	public static void setClock(int clock2) {
		clock = clock2;
	}

	public static int getClock() {
		return clock;
	}

	public static void setHistoricalNumberOfElements(
			int historicalNumberOfElements) {
		HistoricalNumberOfElements = historicalNumberOfElements;
	}

	public static int getHistoricalNumberOfElements() {
		return HistoricalNumberOfElements;
	}

	public static void setHistoricalTimeFrame(int historicalTimeFrame) {
		HistoricalTimeFrame = historicalTimeFrame;
	}

	public static int getHistoricalTimeFrame() {
		return HistoricalTimeFrame;
	}

	public static void setLength(int newLength) {
		length = newLength;
	}

	public static void setSimStartTime(Calendar simStartTime) {
		DCSimCore.simStartTime = simStartTime;
	}

	public static Calendar getSimStartTime() {
		return simStartTime;
	}

	public static void setSimulatorIsRunning(boolean simulatorIsRunning) {
		DCSimCore.simulatorIsRunning = simulatorIsRunning;
	}

	public static boolean isSimulatorIsRunning() {
		return simulatorIsRunning;
	}
	
	public static Date getCurrentDate() {
		Date currentTime = DCSimCore.simStartTime.getTime();
		currentTime.setTime(currentTime.getTime()+(DCSimCore.clock*1000));
		return currentTime;
	}

}
