package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

public class LoanPerformanceDueData {
	private final BigDecimal due;
	public LoanPerformanceDueData(final BigDecimal due) {
		this.due = due;
	}
	public BigDecimal getDue() {
		return due;
	}
	
}
