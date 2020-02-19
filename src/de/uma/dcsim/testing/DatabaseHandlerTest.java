package de.uma.dcsim.testing;
//package testing;
//
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import org.junit.Test;
//
//import database.ColumnType;
//import database.DatabaseRecord;
//import database.SqLiteDBHandler;
//import database.StatisticType;
//import database.TableType;
//
public class DatabaseHandlerTest {
//
//	
//	@Test
//	public void testSelect() throws IOException {
//		SqLiteDBHandler dbHandler = new SqLiteDBHandler("test.db");
//		dbHandler.createTestSetUp();
//		
//		ArrayList<DatabaseRecord> result = dbHandler.getResultTable("splitTest");
//		
//		assertTrue(result.size()==7);
//		
//		DatabaseRecord testRecord = result.get(3);
//		
////		assertTrue(testRecord.getTimestamp() == 3000);
//		assertTrue(testRecord.getDouble(ColumnType.TOTAL_EC) == 900);
//		assertTrue(testRecord.getDouble(ColumnType.HVAC_EC) == 40);
//		assertTrue(testRecord.getDouble(ColumnType.JOB_POWER) == 50);
//		assertTrue(testRecord.getDouble(ColumnType.ENERGY_COST) == 60);
//		assertTrue(testRecord.getDouble(ColumnType.SLA_COST) == 890);
//	}
//	
//	@Test
//	public void testSelectBetweenDates() throws IOException {
//		SqLiteDBHandler dbHandler = new SqLiteDBHandler("test.db");
//		
//		dbHandler.createTestSetUp();
//		
//		Date first = new Date();
//		Date second = new Date();
//
//		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//		
//		try {
//			first = format.parse("02.01.2014");
//			second = format.parse("04.01.2014");
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
////		first.setTime(1500);
////		second.setTime(3500);
//		
//		ArrayList<DatabaseRecord> result = dbHandler.getRecordsBetweenDates("splitTest", first, second);
//		
//		assertTrue(result.size() == 3);
//		
////		for(SimulationRecord record : result) {
////			assertTrue(record.getTimestamp() >= 1500);
////			assertTrue(record.getTimestamp() <= 3500);
////		}
//	}
//	
//	@Test
//	public void testSelectBetween() throws IOException {
//		SqLiteDBHandler dbHandler = new SqLiteDBHandler("test.db");
//		
//		dbHandler.createTestSetUp();
//		
//		Object[] bounds = {300.0,2500.0};
//		
//		ArrayList<DatabaseRecord> result = dbHandler.getRecordsBetween("splitTest", ColumnType.TOTAL_EC, bounds);
//		assertTrue(result.size() == 3);
//		
//		for(DatabaseRecord record : result) {
//			assertTrue(record.getDouble(ColumnType.TOTAL_EC) >= 300);
//			assertTrue(record.getDouble(ColumnType.TOTAL_EC) <= 2500);
//		}
//		
//	}
//	
//	@Test
//	public void testStatisticWithFilter() throws IOException {
//		ArrayList<Double> result = new ArrayList<Double>();
//		SqLiteDBHandler dbHandler = new SqLiteDBHandler("test.db");
//		
//		dbHandler.createTestSetUp();
//		
//		List<ColumnType> filterColumns = new ArrayList<ColumnType>();
//		filterColumns.add(ColumnType.TIMESTAMP);
//		
//		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
//		Date endDate = new Date();
//		Date tmp = new Date();
//		Date startDate = new Date();
//		try {
//			endDate = format.parse("04.01.2014");
//			startDate = format.parse("02.01.2014");
//			
//			tmp = format.parse("02.01.2014");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Date tmp2 = new Date(tmp.getTime() + 90000000);
//		List<Object[]> filterFrame;
//		while(tmp.before(endDate)) {
//			filterFrame = new ArrayList<Object[]>();
//			filterFrame.add(new Object[] {tmp.getTime(), tmp2.getTime()});
//			
//			result.add(dbHandler.getStatisticWithFilter("splitTest", StatisticType.AVERAGE, ColumnType.TOTAL_EC, filterColumns, filterFrame));
//			
//			tmp.setTime(tmp.getTime() + 90000000);
//			tmp2.setTime(tmp.getTime() + (89999000));
//		}
//		
//		
//	}
}
