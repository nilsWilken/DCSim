package de.uma.dcsim.evaluation;

import java.util.List;

import de.uma.dcsim.database.ColumnType;
import de.uma.dcsim.database.EvaluationTable;

/**
 * This class represents a specification of an element of the evaluation.
 * @author nilsw
 *
 */
public class EvaluationSpecification {

	/**
	 * Indicates the column that is affected by this evaluation element.
	 */
	private ColumnType column;
	
	private List<ColumnType> columnTypes;
	
	/**
	 * Indicates the type of this evaluation element.
	 */
	private EvaluationType evaluationType;
	
	/**
	 * Indicates the name that the result of the evaluation element should have in the output file.
	 */
	private String evaluationColumnName;
	
	private List<String> evaluationColumnNames;
	
	/**
	 * Indicates the unique table prefix of the table that is affected by this evaluation element.
	 */
	private EvaluationTable evaluationTable;

	public EvaluationSpecification(ColumnType column, List<ColumnType> columnTypes, EvaluationType evaluationType, String evaluationColumnName, List<String> evaluationColumnNames, EvaluationTable evaluationTable) {
		super();
		this.column = column;
		this.columnTypes = columnTypes;
		this.evaluationType = evaluationType;
		this.evaluationColumnName = evaluationColumnName;
		this.evaluationColumnNames = evaluationColumnNames;
		this.evaluationTable = evaluationTable;
	}
	
	public ColumnType getColumn() {
		return column;
	}

	public void setColumn(ColumnType column) {
		this.column = column;
	}
	
	public List<ColumnType> getColumnTypes() {
		return this.columnTypes;
	}

	public EvaluationType getEvaluationType() {
		return evaluationType;
	}

	public void setEvaluationType(EvaluationType evaluationType) {
		this.evaluationType = evaluationType;
	}

	public String getEvaluationColumnName() {
		return evaluationColumnName;
	}
	
	public List<String> getEvaluationColumnNames() {
		return this.evaluationColumnNames;
	}

	public void setEvaluationColumnName(String evaluationColumnName) {
		this.evaluationColumnName = evaluationColumnName;
	}
	
	public EvaluationTable getEvaluationTable() {
		return this.evaluationTable;
	}
	
	public void setEvaluationTable(EvaluationTable evaluationTable) {
		this.evaluationTable = evaluationTable;
	}

}
