package de.uma.dcsim.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import de.uma.dcsim.database.ColumnType;
import de.uma.dcsim.database.EvaluationTable;
import de.uma.dcsim.utilities.Constants;

public class EvaluationSetupParser {
	
	private static final String[] evaluationSetupElements = {"EvaluationName", "DatabasePath", "OutputPath", "DateFormatString", "EvaluationStartDate", "EvaluationEndDate", "MillisecondsPerAggregationInterval", "DecimalSeparator"};
	
	public static EvaluationSetup parseEvaluationSetup(String path) {
		
		try {
			File setupFile = new File(path);
			
			if(setupFile.exists()) {
				Document doc = new SAXBuilder().build(path);
				Element root = doc.getRootElement();
			
				HashMap<String, Element> parsedElements = new HashMap<String, Element>();
				Element tmp;
				for(String evaElement : EvaluationSetupParser.evaluationSetupElements) {
					tmp = root.getChild(evaElement);
					parsedElements.put(evaElement, tmp);
				}
				
				List<Element> evaluationSpecifications = root.getChild("EvaluationSpecifications").getChildren("EvaluationSpecification");
				List<Element> dbNames = root.getChild("DBNames").getChildren("DBName");
				
				List<EvaluationSpecification> parsedEvaluationSpecifications = EvaluationSetupParser.parseEvaluationSpecifications(evaluationSpecifications);
				List<String> parsedDBNames = EvaluationSetupParser.parseDBNames(dbNames);
				
				
				String evaluationName = parsedElements.get("EvaluationName").getText();
				String dbPath = parsedElements.get("DatabasePath").getText();
				String outputPath = parsedElements.get("OutputPath").getText();
				String dateFormatString = parsedElements.get("DateFormatString").getText();
				String startDate = parsedElements.get("EvaluationStartDate").getText();
				String endDate = parsedElements.get("EvaluationEndDate").getText();
				String millisecondsPerAggregationInterval = parsedElements.get("MillisecondsPerAggregationInterval").getText();
				
				String decimalSeparator = Constants.DECIMAL_SEPARATOR;
				if(parsedElements.get("DecimalSeparator") != null) {
					decimalSeparator = parsedElements.get("DecimalSeparator").getText();
				}
				
				
				return new EvaluationSetup(evaluationName, dbPath, outputPath, parsedDBNames, dateFormatString, startDate, endDate, millisecondsPerAggregationInterval, parsedEvaluationSpecifications, decimalSeparator);
				
				
			}
			else {
				System.out.println("File with path " + path + " not found!");
				return null;
			}
			
		}catch(IOException | JDOMException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static List<EvaluationSpecification> parseEvaluationSpecifications(List<Element> evaluationSpecifications) {
		List<EvaluationSpecification> result = new ArrayList<EvaluationSpecification>();
		
		ColumnType columnType = null;
		List<ColumnType> columnTypes = null;
		EvaluationType evaluationType;
		String columnName = null;
		List<String> columnNames = null;
		EvaluationTable evaluationTable;
		
		for(Element evaluationSpec : evaluationSpecifications) {
			columnType = ColumnType.parseFromString(evaluationSpec.getChild("ColumnType").getText());
			evaluationType = EvaluationType.parseFromString(evaluationSpec.getChild("EvaluationType").getText());
			
			if(evaluationType == EvaluationType.ALL_RECORDS) {
				columnNames = new ArrayList<String>();
				columnTypes = new ArrayList<ColumnType>();
				for(Element cName : evaluationSpec.getChildren("EvaluationColumnName")) {
					columnNames.add(cName.getText());
				}
				for(Element cType : evaluationSpec.getChildren("ColumnType")) {
					columnTypes.add(ColumnType.parseFromString(cType.getText()));
				}
			}
			else {
				columnName = evaluationSpec.getChild("EvaluationColumnName").getText();
			}
			evaluationTable = EvaluationTable.parseFromString(evaluationSpec.getChild("EvaluationTable").getText());
			
			result.add(new EvaluationSpecification(columnType, columnTypes, evaluationType, columnName, columnNames, evaluationTable));
		}
		
		return result;
	}
	
	private static List<String> parseDBNames(List<Element> dbNames) {
		List<String> result = new ArrayList<String>();
		
		for(Element dbName : dbNames) {
			result.add(dbName.getText());
		}
		
		return result;
	}

}
