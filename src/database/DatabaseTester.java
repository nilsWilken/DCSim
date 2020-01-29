//package database;
//
//import java.util.ArrayList;
//
///**
// * This class is designed to test the functionalities of the SqLiteDBHandler class.
// * @author nilsw
// *
// */
//public class DatabaseTester {
//	
//	public static void main(String[] args) {
//		SqLiteDBHandler dbHandler = new SqLiteDBHandler("test.db");
//		dbHandler.createTestSetUp();
//		
////		dbHandler.createResultTable("testResult");
////		dbHandler.createResultTable("testResult2");
////		dbHandler.createResultTable("simRun_16042018_1116");
////		
////		dbHandler.insertIntoResultTable("testResult2", 1000, 20, 40, 50, 60, 890);
//		
//		ArrayList<String> tableC = dbHandler.getTableCatalog();
//		
//		for(String s : tableC) {
//			System.out.println(s);
//		}
//		
//		System.out.println(dbHandler.getStatistic("testResult2", ColumnType.TOTAL_EC, StatisticType.AVERAGE));
//		System.out.println(dbHandler.getStatistic("testResult2", ColumnType.TOTAL_EC, StatisticType.MAXIMUM));
//		System.out.println(dbHandler.getStatistic("testResult2", ColumnType.TOTAL_EC, StatisticType.MINIMUM));
//		System.out.println(dbHandler.getStatistic("testResult2", ColumnType.TOTAL_EC, StatisticType.SUM));
//		
//		ArrayList<ColumnType> filterList = new ArrayList<ColumnType>();
//		ArrayList<Object[]> frameList = new ArrayList<Object[]>();
//		
//		filterList.add(ColumnType.TOTAL_EC);
//		frameList.add(new Object[] {900.0,2000.0});
//		
//		System.out.println(dbHandler.getStatisticWithFilter("testResult2", StatisticType.AVERAGE, ColumnType.TOTAL_EC, filterList, frameList));
//	}
//
//}
