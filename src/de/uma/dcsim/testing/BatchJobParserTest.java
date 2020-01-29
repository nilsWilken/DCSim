package de.uma.dcsim.testing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.junit.Test;

import de.uma.dcsim.utilities.BatchJob;
import de.uma.dcsim.utilities.BatchJobParser;

public class BatchJobParserTest {
	
	@Test
	public void testparser() throws IOException{
		BatchJobParser parser = new BatchJobParser();
		
		Date simStartTime = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
		
		try {
			simStartTime = format.parse("01.01.2014 00:00:00");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		ArrayList<BatchJob> testResult = (ArrayList<BatchJob>)parser.parseJobFile("src/main/resources/superMUC_jobs_january.csv", simStartTime);
		
		assertEquals(testResult.get(0).getAmountOfServers(), 2);
		assertEquals(testResult.get(0).getId(), "srv04-ib.297906");
		
		assertEquals(testResult.get(testResult.size()-1).getAmountOfServers(), 1);
		assertEquals(testResult.get(testResult.size()-1).getId(), "srv03-ib.369152");
	}

}
