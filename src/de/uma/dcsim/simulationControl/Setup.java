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

package de.uma.dcsim.simulationControl;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.uma.dcsim.energyPriceModels.EnergyPriceModelSelector;
import de.uma.dcsim.eventHandling.DRRequestEvent;
import de.uma.dcsim.eventHandling.EventType;
import de.uma.dcsim.hardware.DC;
import de.uma.dcsim.hardware.ESF;
import de.uma.dcsim.hardware.HVAC;
import de.uma.dcsim.hardware.Server;
import de.uma.dcsim.pueModels.PUEModelSelector;
import de.uma.dcsim.scheduling.schedulingStrategies.SchedulingStrategyType;
import de.uma.dcsim.scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import de.uma.dcsim.serviceRelatedClasses.VM;
import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobParser;
import de.uma.dcsim.utilities.DRRequest;
import de.uma.dcsim.utilities.DRRequestParser;
import de.uma.dcsim.utilities.EnergyPrice;
import de.uma.dcsim.utilities.EnergyPriceParser;
import de.uma.dcsim.utilities.PUE;
import de.uma.dcsim.utilities.PUEParser;

/**
 * 
 * @author University of Mannheim
 * 
 */
public class Setup {

	/**
	 * Configured length of the simulation in simulation time.
	 */
	private int simLength;

	/**
	 * List of all servers in the DC.
	 */
	private List<Server> hosts;

	/**
	 * List of all DCs that are specified in the configuration.
	 */
	private List<DC> dcs;

	/**
	 * List of all DC federations that are specified in the configuration.
	 */
	private List<DCFederation> dcFederations;

	/**
	 * Simulation start time.
	 */
	private Calendar c;

	/**
	 * Path to the file that contains the workload trace for the simulation.
	 */
	private String workloadTraceFile;

	/**
	 * Path to the file that contains the DR event trace for the simulation.
	 */
	private String drEventTraceFile;

	/**
	 * Configured length of the scheduling interval.
	 */
	public static int schedulingInterval;
	public static int length;

	/**
	 * Amount of real time seconds that each step in simulation time contains.
	 */
	public static int secondsPerSimulationTimestep = 1;

	/**
	 * Exponent for the SLA cost function.
	 */
	public static int slaCostExponent;

	/**
	 * Indicates whether the simulation framework is started in superMUCMode.
	 */
	public static boolean superMUCMode = true;

	/**
	 * Maximum job runtime that occurs in the workload trace.
	 */
	public static int maximumRuntime;

	/**
	 * Timeout of the linear solver that is used in the scheduling process.
	 */
	public static int solverTimeout = 10;

	/**
	 * Path at which the databse that contains the monitoring values should be
	 * placed.
	 */
	public static String evaluationDatabasePath;

	/**
	 * Path to the file that contains the energy price trace for the simulation.
	 */
	public static String energyPriceTraceFile;

	/**
	 * Path to the file that contains the WEKA linear regression models that are
	 * used for the server power consumption models.
	 */
	public static String wekaPowerModelFile;

	/**
	 * Path to the file that contains the PUE trace for the simulation.
	 */
	public static String pueTraceFile;

	/**
	 * Indicates whether jobs are pausable.
	 */
	public static boolean jobsPausable = false;

	/**
	 * Indicates whether the test log outputs should be printed to the console.
	 */
	public static boolean testLogOutput = false;

	/**
	 * Indicates whether future knowledge on energy price and workload should be
	 * used.
	 */
	public static boolean useFutureKnowledge = false;
	
	/**
	 * Format of the dates that is used in the file that contains the workload trace.
	 */
	private static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
	
	public static final double SERVER_IT_POWER_FRACTION = 0.714286;
	
	public static double usagePrice = 0;
	
	public static SchedulingStrategyType usedSchedulingStrategyType;

	public Setup() {
		this.hosts = new ArrayList<Server>();
		this.dcs = new ArrayList<DC>();
		this.setDcFederations(new ArrayList<DCFederation>());
	}

	public double getSchedulingInterval() {
		return schedulingInterval;
	}

	/**
	 * Reads a configuration file from the passed input stream.
	 * 
	 * @param file
	 *            Configuration file that should be parsed.
	 */
	public void readDC(final InputStream file) {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			readDC(doc);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<String> readDCNamesOnly(final InputStream file) {
		ArrayList<String> dcNames = new ArrayList<String>();
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);
			doc.getDocumentElement().normalize();

			NodeList listOfDatacentres = doc.getElementsByTagName("DataCentre");

			for (int s = 0; s < listOfDatacentres.getLength(); s++) {
				Node dc = listOfDatacentres.item(s);
				if (dc.getNodeType() == Node.ELEMENT_NODE) {

					Element dcElement = (Element) dc;
					String name;

					NodeList nameList = dcElement.getElementsByTagName("Name");
					name = ((Node) (((Element) nameList.item(0)).getChildNodes()).item(0)).getNodeValue().trim();

					dcNames.add(name);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return dcNames;
	}

	public void setDcs(List<DC> dcs) {
		this.dcs = dcs;
	}

	public List<DC> getDcs() {
		return dcs;
	}

	public void setDcFederations(List<DCFederation> dcFederations) {
		this.dcFederations = dcFederations;
	}

	public List<DCFederation> getDcFederations() {
		return dcFederations;
	}

	private void setSimLength(int simLength) {
		this.simLength = simLength;
		Setup.length = simLength;
	}

	public int getSimLength() {
		return simLength;
	}

	public Calendar getStartTime() {
		return c;
	}

	private void readDC(final Document doc) throws Exception {
		doc.getDocumentElement().normalize();
		NodeList schedulingIntervalList = doc
				.getElementsByTagName("SchedulingInterval");
		schedulingInterval = Integer
				.parseInt(((Node) (((Element) schedulingIntervalList.item(0))
						.getChildNodes()).item(0)).getNodeValue().trim());

//		NodeList simSpeedList = doc.getElementsByTagName("SimSpeed");
//		simSpeed = Double.parseDouble(((Node) (((Element) simSpeedList.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		
		NodeList secondsPerSimulationTimestep = doc.getElementsByTagName("SecondsPerSimulationTimestep");
		Setup.secondsPerSimulationTimestep = Integer.parseInt(((Node) (((Element) secondsPerSimulationTimestep.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		
//		NodeList slaCostExponent = doc.getElementsByTagName("SLACostExponent");
//		Setup.slaCostExponent = Integer.parseInt(((Node) (((Element) slaCostExponent.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		
		NodeList superMUCMode = doc.getElementsByTagName("SuperMUCMode");
		String mode = ((Node) (((Element) superMUCMode.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		if(mode.toLowerCase().equals("on")) {
			Setup.superMUCMode = true;
		}
		else {
			Setup.superMUCMode = false;
		}
		
		NodeList usagePrice = doc.getElementsByTagName("UsagePrice");
		if(usagePrice != null && usagePrice.getLength() > 0) {
			Setup.usagePrice = Double.parseDouble(((Node) (((Element) usagePrice.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		}
		System.out.println("Usage price: " + Setup.usagePrice);
		
		NodeList schedulingStrategy = doc.getElementsByTagName("SchedulingStrategy");
		if(schedulingStrategy != null && schedulingStrategy.getLength() > 0) {
			Setup.usedSchedulingStrategyType = SchedulingStrategyType.parseFromString(((Node) (((Element) schedulingStrategy.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		}
		else {
			Setup.usedSchedulingStrategyType = SchedulingStrategyType.parseFromString("");
		}
		System.out.println("Scheduling strategy: " + Setup.usedSchedulingStrategyType);
		
		NodeList solverTimeout = doc.getElementsByTagName("SolverTimeout");
		Setup.solverTimeout = Integer.parseInt(((Node) (((Element) solverTimeout.item(0)).getChildNodes()).item(0)).getNodeValue().trim());
		
		NodeList workloadTrace = doc.getElementsByTagName("WorkloadTraceFile");
		this.workloadTraceFile = ((Node) (((Element) workloadTrace.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList drEventTrace = doc.getElementsByTagName("DREventTraceFile");
		this.drEventTraceFile = ((Node) (((Element) drEventTrace.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList energyPriceTrace = doc.getElementsByTagName("EnergyPriceTraceFile");
		Setup.energyPriceTraceFile = ((Node) (((Element) energyPriceTrace.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList evaluationDatabase = doc.getElementsByTagName("EvaluationDatabase");
		Setup.evaluationDatabasePath = ((Node) (((Element) evaluationDatabase.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList wekaPowerModels = doc.getElementsByTagName("WekaPowerModelFile");
		Setup.wekaPowerModelFile = ((Node) (((Element) wekaPowerModels.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList pueTrace = doc.getElementsByTagName("PUETraceFile");
		Setup.pueTraceFile = ((Node) (((Element) pueTrace.item(0)).getChildNodes()).item(0)).getNodeValue().trim();
		
		NodeList simStartTimeList = doc.getElementsByTagName("SimStartTime");
		String startTime = ((Node) (((Element) simStartTimeList.item(0))
				.getChildNodes()).item(0)).getNodeValue().trim();
		 
		startTime = startTime.replace("T", " ");
		System.out.println("Parsed start string: " + startTime);
		c = Calendar.getInstance();
		INPUT_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		c.setTime(Setup.INPUT_DATE_FORMAT.parse(startTime));
		//c = DatatypeConverter.parseDateTime(startTime);
				
		NodeList simlengthList = doc.getElementsByTagName("SimLength");
		int length = Integer.parseInt(((Node) (((Element) simlengthList.item(0))
				.getChildNodes()).item(0)).getNodeValue().trim());
		if (length < 0)
			length = length * -1;
		setSimLength(length);

//		NodeList histTimeList = doc.getElementsByTagName("HistoricalTimeFrame");
//		histTime = Integer.parseInt(((Node) (((Element) histTimeList.item(0))
//				.getChildNodes()).item(0)).getNodeValue().trim());
//
//		NodeList histElemList = doc
//				.getElementsByTagName("HistoricalNumberOfElements");
//		histElem = Integer.parseInt(((Node) (((Element) histElemList.item(0))
//				.getChildNodes()).item(0)).getNodeValue().trim());

//		NodeList listOfDatacentreFederations = doc
//				.getElementsByTagName("DataCentreFederation");

//		for (int f = 0; f < listOfDatacentreFederations.getLength(); f++) {
//			Node federation = listOfDatacentreFederations.item(f);
//			if (federation.getNodeType() == Node.ELEMENT_NODE) {
				DCFederation fed = new DCFederation();
//				Element fElement = (Element) federation;
				NodeList listOfDatacentres = doc
						.getElementsByTagName("DataCentre");
				List<DC> dcsOfFed= new ArrayList<DC>();
				for (int s = 0; s < listOfDatacentres.getLength(); s++) {
					Node dc = listOfDatacentres.item(s);
					if (dc.getNodeType() == Node.ELEMENT_NODE) {
						
						hosts = new ArrayList<Server>();
						// Get elements from input-XML-file
						Element dcElement = (Element) dc;
						String name;
						double pue = 1.0;

						NodeList pueList = dcElement
								.getElementsByTagName("PUE");
						pue = Double.parseDouble(((Node) (((Element) pueList.item(0))
								.getChildNodes()).item(0)).getNodeValue()
								.trim());
						
						NodeList nameList = dcElement
						.getElementsByTagName("Name");
						name = ((Node) (((Element) nameList.item(0))
						.getChildNodes()).item(0)).getNodeValue()
						.trim();

						// ------- Hosts -------
						NodeList hostList = dcElement
								.getElementsByTagName("ServerConfiguration");
						int serverID = 1;
						for (int i = 0; i < hostList.getLength(); i++) {
							Node host = hostList.item(i);
							if (host.getNodeType() == Node.ELEMENT_NODE) {

								Element hostElement = (Element) host;
								int numberOfHosts;
								int mips, pes, ram, bw, storage;

								NodeList nbHostsList = hostElement
										.getElementsByTagName("NbOfServer");
								numberOfHosts = Integer
										.parseInt(((Node) (((Element) nbHostsList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								NodeList mipsList = hostElement
										.getElementsByTagName("MIPS");
								mips = Integer
										.parseInt(((Node) (((Element) mipsList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								NodeList pesList = hostElement
										.getElementsByTagName("PES");
								pes = Integer
										.parseInt(((Node) (((Element) pesList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								NodeList ramList = hostElement
										.getElementsByTagName("RAM");
								ram = Integer
										.parseInt(((Node) (((Element) ramList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								NodeList bwList = hostElement
										.getElementsByTagName("BW");
								bw = Integer
										.parseInt(((Node) (((Element) bwList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								NodeList storageList = hostElement
										.getElementsByTagName("Storage");
								storage = Integer
										.parseInt(((Node) (((Element) storageList
												.item(0)).getChildNodes())
												.item(0)).getNodeValue().trim());

								for (int ii = 0; ii < numberOfHosts; ii++) {
									Server server = new Server(mips, pes, ram,
											bw, storage,
											s * 1000000 + serverID,
											new ArrayList<VM>());
									hosts.add(server);
									serverID++;
								}

							}
						}

						double standardTemp = 25.0d;

						// ------- HVAC -------
						NodeList hvacList = dcElement
								.getElementsByTagName("HVAC");
						Node hvac = hvacList.item(0);
						if (hvac.getNodeType() == Node.ELEMENT_NODE) {
							Element hvacElement = (Element) hvac;
							NodeList sTList = hvacElement
									.getElementsByTagName("StandardTemp");
							standardTemp = Double
									.parseDouble(((Node) (((Element) sTList
											.item(0)).getChildNodes()).item(0))
											.getNodeValue().trim());
						}

						HVAC hvacInDC = new HVAC(standardTemp);

//						int efficiency = 0;
//						int input = 0;
						ArrayList<ESF> esfs = new ArrayList<ESF>();
						
						//--JOBS--
						ArrayList<BatchJob> jobs = (ArrayList<BatchJob>)BatchJobParser.parseJobFile(this.workloadTraceFile, this.c.getTime());
						jobs = (ArrayList<BatchJob>) SchedulingStrategyUtilities.sortJobListBySubmissionTime(jobs);

						
						DRRequestParser drRequestParser = new DRRequestParser();
						ArrayList<DRRequest> drRequests = (ArrayList<DRRequest>)drRequestParser.parseDRRequestFile(this.drEventTraceFile, this.c.getTime());
						
						EnergyPriceParser energyPriceParser = new EnergyPriceParser();
						ArrayList<EnergyPrice> energyPrices = (ArrayList<EnergyPrice>)energyPriceParser.parseEnergyPriceFile(Setup.energyPriceTraceFile, this.c.getTime());
						EnergyPriceModelSelector.initializeEnergyPriceModel(energyPrices);
						
						PUEParser pueParser = new PUEParser();
						ArrayList<PUE> pues = (ArrayList<PUE>)pueParser.parsePUEFile(Setup.pueTraceFile, this.c.getTime());
						PUEModelSelector.initializePUEModel(pues);
						
//						DC dc2 = new DC(pue, name, s, hosts, services, hvacInDC,
//								esfs, this.pricing, this.histElem, this.c, (int) this.schedulingInterval);
						DC dc2 = new DC(pue, name, s, hosts, jobs, hvacInDC,
								esfs, this.c, (int) Setup.schedulingInterval);
						
						for(DRRequest drRequest : drRequests) {
							dc2.scheduleEvent(new DRRequestEvent(EventType.DR_REQUEST, drRequest.getTimestamp(), drRequest));
						}
//						System.out.println("DC SETUPT");
//						System.out.println(dc2.getName());
						dcs.add(dc2);
						dcsOfFed.add(dc2);

					}
				}
				for (DC dc : dcsOfFed){
					fed.addDC(dc);
				}
				dcFederations.add(fed); 
	}

}
