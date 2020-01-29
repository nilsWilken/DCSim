package de.uma.dcsim.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.uma.dcsim.database.ColumnType;
import de.uma.dcsim.database.EvaluationTable;

public class Evaluation {
	
	public static void main(String[] args) {
//		if(args.length != 1) {
//			System.out.println("Please give only the path of the Simulation Setup file as parameter...");
//			System.exit(0);
//		}
		
//		EvaluationSetup evaluationSetup = EvaluationSetupParser.parseEvaluationSetup("C:/Users/nwilken/Documents/Forschung/MasterThesis_Paper/ACM_e-Energy_Paper/DRDCSim_Setups/EvaluationSetups/DummyEvaluationSetup.xml");
//		evaluationSetup.evaluate();
		
		BufferedReader reader;
		
		try {
			reader = new BufferedReader(new InputStreamReader(System.in));
			
			String line;
			String[] split;
			String command;
			String[] parameters;
			
			System.out.print(">> ");
			while(!(line = reader.readLine()).equals("exit")) {
				split = line.split(" ");
				parameters = new String[1];
				if(split.length >= 1) {
					command = split[0];
					if(split.length > 1) {
						parameters = new String[split.length-1];
						for(int i=1; i < split.length; i++) {
							parameters[i-1] = split[i];
						}
					}
					Evaluation.reactToCommand(command, parameters);
				}
				else {
					System.out.println("No command given!");
				}
				System.out.print(">> ");
			}
			
			reader.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void reactToCommand(String command, String[] parameters) {
		switch(command) {
		case "evaluate":
			if(parameters.length != 1) {
				System.out.println("Wrong amount of parameters given. Please indicate only the path of the evaluation setup as parameter.");
				return;
			}
			Evaluation.evaluate(parameters[0]);
			return;
		case "printEvaluationTables":
			Evaluation.printEvaluationTables();
			return;
		case "printColumnTypes":
			Evaluation.printColumnTypes();
			return;
		case "printEvaluationTypes":
			Evaluation.printEvaluationTypes();
			return;
		default:
			return;
		}
	}
	
	public static void evaluate(String path) {
		EvaluationSetup evaluationSetup = EvaluationSetupParser.parseEvaluationSetup(path);
		evaluationSetup.evaluate();
	}
	
	public static void printEvaluationTypes() {
		for(EvaluationType eType : EvaluationType.values()) {
			System.out.println(EvaluationType.getStringNameFromType(eType));
		}
		System.out.println();
	}
	
	public static void printColumnTypes() {
		for(ColumnType cType : ColumnType.values()) {
			System.out.println(ColumnType.convertToStringName(cType));
		}
		System.out.println();
	}
	
	public static void printEvaluationTables() {
		for(EvaluationTable table : EvaluationTable.values()) {
			System.out.println(EvaluationTable.getTableName(table));
		}
		System.out.println();
	}

}
