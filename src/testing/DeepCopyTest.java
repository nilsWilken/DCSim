package testing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Test;

import drEventHandling.SimpleDREventHandler;
import hardware.DC;
import hardware.Server;
import simulationControl.Setup;
import utilities.BatchJob;
import utilities.BatchJobStatus;
import utilities.ReserveProvisionType;
import utilities.ServerStatus;

public class DeepCopyTest {
	
	@Test
	public void testDeepCopy() throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream("src/main/resources/SuperMUC_testSetup.xml"));
		Setup setup = new Setup();
		setup.readDC(in);

		List<DC> dcs = setup.getDcs();
		in.close();

		dcs.get(0).setupDC();
		
//		for(int i=0; i < 1200; i++) {
//			dcs.get(0).updateJobAllocation();
//		}

		DC copy = dcs.get(0).deepCopy(dcs.get(0).getEndOfNextSchedulingInterval());
		DC copy2 = copy.deepCopy(copy.getEndOfNextSchedulingInterval());
		DC original = dcs.get(0);
		
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

		for(int i=0; i < 500000; i++) {
			copy.scheduleJobs();
			copy.updateJobAllocation(true, true);
			
			copy2.scheduleJobs();
			copy2.updateJobAllocation(true, true);
			
			original.scheduleJobs();
			original.updateJobAllocation(true, true);
			
			assertTrue(copy.getOverallCurrentPC() == original.getOverallCurrentPC());
			assertTrue(copy2.getOverallCurrentPC() == original.getOverallCurrentPC());
			
			assertTrue(copy.getOccupiedServer().size() == original.getOccupiedServer().size());
			assertTrue(copy2.getOccupiedServer().size() == original.getOccupiedServer().size());
			
			assertTrue(copy.getRunningJobs().size() == original.getRunningJobs().size());
			assertTrue(copy2.getRunningJobs().size() == original.getRunningJobs().size());
		}
		
		DC copy3 = copy2.deepCopy(copy2.getEndOfNextSchedulingInterval());
		
		for(int i=0; i < 500000; i++) {
			copy.scheduleJobs();
			copy.updateJobAllocation(true, true);
			
			copy2.scheduleJobs();
			copy2.updateJobAllocation(true, true);
			
			copy3.scheduleJobs();
			copy3.updateJobAllocation(true, true);
			
			original.scheduleJobs();
			original.updateJobAllocation(true, true);
			
			assertTrue(copy.getOverallCurrentPC() == original.getOverallCurrentPC());
			assertTrue(copy2.getOverallCurrentPC() == original.getOverallCurrentPC());
//			System.out.println(copy3.getOverallCurrentEC() + " " + original.getOverallCurrentEC());
//			System.out.println(copy3.getPausedJobs().size() + " " + original.getPausedJobs().size());
//			System.out.println(copy3.getRunningJobs().size() + " " + original.getRunningJobs().size());
//			System.out.println(copy3.getOccupiedServer().size() + " " + original.getOccupiedServer().size());
			assertTrue(copy3.getOverallCurrentPC() == original.getOverallCurrentPC());
			
			assertTrue(copy.getOccupiedServer().size() == original.getOccupiedServer().size());
			assertTrue(copy2.getOccupiedServer().size() == original.getOccupiedServer().size());
			assertTrue(copy3.getOccupiedServer().size() == original.getOccupiedServer().size());
			
			assertTrue(copy.getRunningJobs().size() == original.getRunningJobs().size());
			assertTrue(copy2.getRunningJobs().size() == original.getRunningJobs().size());
			assertTrue(copy3.getRunningJobs().size() == original.getRunningJobs().size());
			
			
		}
		
		DC copy4 = copy3.deepCopy(copy3.getEndOfNextSchedulingInterval());
		SimpleDREventHandler copy3Handler = (SimpleDREventHandler)copy3.getDREventHandler();
		SimpleDREventHandler copy4Handler = (SimpleDREventHandler)copy4.getDREventHandler();
		
		copy3Handler.setShiftingFraction(0.8);
		copy3Handler.setScalingFrequency(2.0);
		copy3Handler.issueDemandResponseRequest(100, ReserveProvisionType.POSITIVE, 900, 0);
		
		copy4Handler.setShiftingFraction(0.8);
		copy4Handler.setScalingFrequency(2.0);
		copy4Handler.issueDemandResponseRequest(100, ReserveProvisionType.POSITIVE, 900, 0);
		System.out.println(copy4.getClock() + " " + copy4.getNextSchedulerCall());
		for(int i=0; i < 500000; i++) {
			copy3.scheduleJobs();
			copy3.updateJobAllocation(true, true);
			
			copy4.scheduleJobs();
			copy4.updateJobAllocation(true, true);
			
			assertTrue(copy3.getOverallCurrentPC() == copy4.getOverallCurrentPC());
			assertTrue(copy3.getOccupiedServer().size() == copy4.getOccupiedServer().size());
			assertTrue(copy3.getRunningJobs().size() == copy4.getRunningJobs().size());
		}
		
		DC copy5 = copy4.deepCopy(copy4.getEndOfNextSchedulingInterval());
		DC copy6 = copy4.deepCopy(copy4.getEndOfNextSchedulingInterval());
		SimpleDREventHandler copy5Handler = (SimpleDREventHandler)copy5.getDREventHandler();
		SimpleDREventHandler copy6Handler = (SimpleDREventHandler)copy6.getDREventHandler();
		
		copy5Handler.setShiftingFraction(0.8);
		copy5Handler.setScalingFrequency(2.0);
		copy5Handler.issueDemandResponseRequest(100, ReserveProvisionType.POSITIVE, 900, 0);
		
		copy6Handler.setShiftingFraction(0.8);
		copy6Handler.setScalingFrequency(2.0);
		copy6Handler.issueDemandResponseRequest(100, ReserveProvisionType.POSITIVE, 900, 0);
		
		for(int i=0; i < 500000; i++) {
			copy5.scheduleJobs();
			copy5.updateJobAllocation(true, true);
			
			copy6.scheduleJobs();
			copy6.updateJobAllocation(true, true);
			
			assertTrue(copy5.getOverallCurrentPC() == copy6.getOverallCurrentPC());
			assertTrue(copy5.getOccupiedServer().size() == copy6.getOccupiedServer().size());
			assertTrue(copy5.getRunningJobs().size() == copy6.getRunningJobs().size());
		}
		
		System.out.println(format.format(original.getCurrentDate()));
		System.out.println(original.getOverallCurrentPC());
		System.out.println(original.getOccupiedServer().size());
		
		
		
		assertFalse(copy.equals(original));
		
		boolean foundServer;
		for(Server os : original.getServer()) {
			os.update();
			foundServer = false;
			for(Server cs : copy.getServer()) {
				cs.update();
				if(cs.getId() == os.getId()) {
					assertFalse(os.equals(cs));
					assertTrue(os.getStatus() == cs.getStatus());
					if(os.getStatus() == ServerStatus.OCCUPIED) {
						assertTrue(os.getCurrentJob().getId().equals(cs.getCurrentJob().getId()));
					}
					
					assertTrue(cs.getBw() == os.getBw());
					assertTrue(cs.getCurrentPC() == os.getCurrentPC());
					assertTrue(cs.getCurrentUtil() == os.getCurrentUtil());
					assertTrue(cs.getHdd() == os.getHdd());
					assertTrue(cs.getMips() == os.getMips());
					assertTrue(cs.getPes() == os.getPes());
					assertTrue(cs.getRam() == os.getRam());
					
					foundServer = true;
					break;
				}
			}
			assertTrue(foundServer);
		}
		
		boolean foundJob;
		for(BatchJob oj : original.getJobs()) {
			foundJob = false;
			for(BatchJob cj : copy.getJobs()) {
				if(oj.getId().equals(cj.getId())) {
					assertFalse(oj.equals(cj));
					assertTrue(oj.getStatus() == cj.getStatus());
					if(oj.getStatus() == BatchJobStatus.RUNNING) {
						List<Server> oAssigned = oj.getAssignedServers();
						List<Server> cAssigned = cj.getAssignedServers();
						
						for(Server oas : oAssigned) {
							foundServer = false;
							for(Server cas : cAssigned) {
								if(oas.getId() == cas.getId()) {
									foundServer = true;
									break;
								}
							}
							assertTrue(foundServer);
						}
					}
					
					assertTrue(cj.getAmountOfServers() == oj.getAmountOfServers());
//					assertTrue(cj.getDurationInHours() == oj.getDurationInHours());
//					assertTrue(cj.getDurationInMinutes() == oj.getDurationInMinutes());
					assertTrue(cj.getFrequency() == oj.getFrequency());
//					assertTrue(cj.getPowerScalingConstant() == oj.getPowerScalingConstant());
					assertTrue(cj.getStartTime() == oj.getStartTime());
//					assertTrue(cj.getUtilization() == oj.getUtilization());
					
					foundJob = true;
					break;
				}
			}
//			assertTrue(foundJob);
		}
		
		assertTrue(copy.getId() == original.getId());
		assertTrue(copy.getName().equals(original.getName()));
		assertTrue(copy.getOverallCurrentPC() == original.getOverallCurrentPC());
		assertTrue(copy.getPue() == original.getPue());
		
		
	}

}
