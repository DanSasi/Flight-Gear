package model.data;

public class CorrelatedFeatures {
	public final String valA, valB;
	public final float correlation;
	public final Line lin_reg;
	public final float threshold;
	
	public CorrelatedFeatures(String aVal, String bVal, float correlation, Line lin_reg, float threshold) {
		this.valA = aVal;
		this.valB = bVal;
		this.correlation = correlation;
		this.lin_reg = lin_reg;
		this.threshold = threshold;
	}
	
}
