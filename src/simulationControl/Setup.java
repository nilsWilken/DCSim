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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import energyPriceModels.EnergyPriceModelSelector;
import eventHandling.DRRequestEvent;
import eventHandling.EventType;
import hardware.DC;
import hardware.ESF;
import hardware.HVAC;
import hardware.Server;
import pueModels.PUEModelSelector;
import scheduling.schedulingStrategies.schedulingUtilities.SchedulingStrategyUtilities;
import serviceRelatedClasses.VM;
import utilities.BatchJob;
import utilities.BatchJobParser;
import utilities.DRRequest;
import utilities.DRRequestParser;
import utilities.EnergyPrice;
import utilities.EnergyPriceParser;
import utilities.PUE;
import utilities.PUEParser;

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
	
	public static double usagePrice = 0.36;

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
		 c = DatatypeConverter.parseDateTime(startTime);
				
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

						int efficiency = 0;
						int input = 0;
						ArrayList<ESF> esfs = new ArrayList<ESF>();

						// ------- ESF -------
//						NodeList esfList = dcElement
//								.getElementsByTagName("ESF");
//						for (int j = 0; j < esfList.getLength(); j++) {
//							Node esf = esfList.item(j);
//							if (esf.getNodeType() == Node.ELEMENT_NODE) {
//
//								Element esfElement = (Element) esf;
//
//								NodeList effList = esfElement
//										.getElementsByTagName("Efficiency");
//								efficiency = (int) (100 * Double
//										.parseDouble(((Node) (((Element) effList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim()));
//
//								NodeList iList = esfElement
//										.getElementsByTagName("ChargingInput");
//								input = Integer
//										.parseInt(((Node) (((Element) iList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//								NodeList capList = esfElement
//										.getElementsByTagName("Capacity");
//								int capacity = Integer
//										.parseInt(((Node) (((Element) capList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//								esfs.add(new ESF(j, new ArrayList<Integer>(),
//										new ArrayList<Integer>(), efficiency,
//										100, input, capacity));
//
//							}
//						}

//						int vmID = 1000000 * s + 1;
//						int serviceID = 1000000 * s + 1;
//						List<Service> services = Collections.synchronizedList(new ArrayList<Service>());
//
//						// ------- Service -------
//						NodeList serviceList = dcElement
//								.getElementsByTagName("Service");
//						for (int j = 0; j < serviceList.getLength(); j++) {
//							Node service = serviceList.item(j);
//							if (service.getNodeType() == Node.ELEMENT_NODE) {
//
//								String sName, availability;
//								int nbOfVMs, mips, pes, ram, bw, storage, startup;
//								List<Integer> utilization = Collections.synchronizedList(new ArrayList<Integer>());
//
//								Element serviceElement = (Element) service;
//
//								NodeList sNameList = serviceElement
//										.getElementsByTagName("Name");
//								sName = ((Node) (((Element) sNameList.item(0))
//										.getChildNodes()).item(0))
//										.getNodeValue().trim();
//
//								NodeList availList = serviceElement
//										.getElementsByTagName("Availability");
//								availability = ((Node) (((Element) availList
//										.item(0)).getChildNodes()).item(0))
//										.getNodeValue().trim();
//
//								int avail = 0; // low
//
//								if (availability.equals("high")) {
//									avail = 2;
//								} else if (availability.equals("medium")) {
//									avail = 1;
//								}
//
//								NodeList startupList = serviceElement
//										.getElementsByTagName("StartupTime");
//								startup = Integer
//										.parseInt(((Node) (((Element) startupList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList nbVMsList = serviceElement
//										.getElementsByTagName("NumberOfVMs");
//								nbOfVMs = Integer
//										.parseInt(((Node) (((Element) nbVMsList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList mipsList = serviceElement
//										.getElementsByTagName("MIPS");
//								mips = Integer
//										.parseInt(((Node) (((Element) mipsList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList pesList = serviceElement
//										.getElementsByTagName("PES");
//								pes = Integer
//										.parseInt(((Node) (((Element) pesList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList ramList = serviceElement
//										.getElementsByTagName("RAM");
//								ram = Integer
//										.parseInt(((Node) (((Element) ramList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList bwList = serviceElement
//										.getElementsByTagName("BW");
//								bw = Integer
//										.parseInt(((Node) (((Element) bwList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList storageList = serviceElement
//										.getElementsByTagName("Storage");
//								storage = Integer
//										.parseInt(((Node) (((Element) storageList
//												.item(0)).getChildNodes())
//												.item(0)).getNodeValue().trim());
//
//								NodeList utilList = serviceElement
//										.getElementsByTagName("Data");
//								String utilizationString = ((Node) (((Element) utilList
//										.item(0)).getChildNodes()).item(0))
//										.getNodeValue().trim();
//
//								for (String u : utilizationString.split(" | ")) {
//									if (!u.trim().equals("")
//											&& !u.trim().equals("|"))
//										utilization.add((int) (1 * Double
//												.valueOf(u.trim())));
//								}
//
//								SLA sla = new SLA(mips, pes, ram, bw, storage,
//										avail, startup);
//
//								nbOfVMs *= 2;
//
//								ArrayList<VM> vms = new ArrayList<VM>();
//								ArrayList<Integer> vmIDs = new ArrayList<Integer>();
//								for (int nbVMs = 0; nbVMs < nbOfVMs; nbVMs++) {
//									vms.add(new VM(vmID, utilization, mips,
//											pes, ram, bw, storage, "off", 0));
//									vmIDs.add(vmID);
//									vmID++;
//								}
//
//								Service newService = new Service(serviceID+j,
//										sName, "running", sla, vms, (int)schedulingInterval, c, s);
//
//								services.add(newService);
//
//							}
//						}

						// ------- Pricing -------
//						NodeList pricingList = dcElement
//								.getElementsByTagName("Pricing");
//						Node pricing = pricingList.item(0);
//						if (pricing.getNodeType() == Node.ELEMENT_NODE) {
//							Element pricingElement = (Element) pricing;
//
//							NodeList mipsPriceList = pricingElement
//									.getElementsByTagName("MIPS");
//							double mipsPrice = Double
//									.parseDouble(((Node) (((Element) mipsPriceList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList pesPriceList = pricingElement
//									.getElementsByTagName("PES");
//							double pesPrice = Double
//									.parseDouble(((Node) (((Element) pesPriceList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList ramPriceList = pricingElement
//									.getElementsByTagName("RAM");
//							double ramPrice = Double
//									.parseDouble(((Node) (((Element) ramPriceList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList bwPriceList = pricingElement
//									.getElementsByTagName("BW");
//							double bwPrice = Double
//									.parseDouble(((Node) (((Element) bwPriceList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList storagePriceList = pricingElement
//									.getElementsByTagName("Storage");
//							double storagePrice = Double
//									.parseDouble(((Node) (((Element) storagePriceList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList availabilityLowList = pricingElement
//									.getElementsByTagName("AvailabilityLow");
//							double availabilityLowPrice = Double
//									.parseDouble(((Node) (((Element) availabilityLowList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList availabilityMediumList = pricingElement
//									.getElementsByTagName("AvailabilityMedium");
//							double availabilityMediumPrice = Double
//									.parseDouble(((Node) (((Element) availabilityMediumList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList availabilityHighList = pricingElement
//									.getElementsByTagName("AvailabilityHigh");
//							double availabilityHighPrice = Double
//									.parseDouble(((Node) (((Element) availabilityHighList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList startupList = pricingElement
//									.getElementsByTagName("Startup");
//							double startup = Double
//									.parseDouble(((Node) (((Element) startupList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							NodeList dcAdaptionCostList = pricingElement
//									.getElementsByTagName("DCAdaptionCost");
//							double dcAdaptionCost = Double
//									.parseDouble(((Node) (((Element) dcAdaptionCostList
//											.item(0)).getChildNodes()).item(0))
//											.getNodeValue().trim());
//
//							this.pricing = new Pricing(mipsPrice, pesPrice,
//									ramPrice, bwPrice, storagePrice,
//									availabilityLowPrice,
//									availabilityMediumPrice,
//									availabilityHighPrice, startup,
//									dcAdaptionCost);
//
//						}
						
						
						//--JOBS--
						BatchJobParser batchJobParser = new BatchJobParser();
						ArrayList<BatchJob> jobs = (ArrayList<BatchJob>)batchJobParser.parseJobFile(this.workloadTraceFile, this.c.getTime());
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
