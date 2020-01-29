package evaluation;

import database.ColumnType;
import database.EvaluationTable;

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
	
	/**
	 * Indicates the type of this evaluation element.
	 */
	private EvaluationType evaluationType;
	
	/**
	 * Indicates the name that the result of the evaluation element should have in the output file.
	 */
	private String evaluationColumnName;
	
	/**
	 * Indicates the unique table prefix of the table that is affected by this evaluation element.
	 */
	private EvaluationTable evaluationTable;

	public EvaluationSpecification(ColumnType column, EvaluationType evaluationType, String evaluationColumnName, EvaluationTable evaluationTable) {
		super();
		this.column = column;
		this.evaluationType = evaluationType;
		this.evaluationColumnName = evaluationColumnName;
		this.evaluationTable = evaluationTable;
	}
	
	public ColumnType getColumn() {
		return column;
	}

	public void setColumn(ColumnType column) {
		this.column = column;
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
