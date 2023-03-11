package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class LoanPerformanceData {

	private final BigDecimal arrears;
	private final BigDecimal paid;
	private final BigDecimal owed;
	//private final BigDecimal due;
	private final BigDecimal loanAmount;
    public LoanPerformanceData(final BigDecimal arrears, final BigDecimal paid,final BigDecimal owed
    		,final BigDecimal loanAmount) {
		this.arrears = arrears;
		this.paid = paid;
		this.owed= owed;
		//this.due =due;
		this.loanAmount = loanAmount;
		
	}
	public BigDecimal getArrears() {
		return arrears;
	}
	public BigDecimal getPaid() {
		return paid;
	}
	public BigDecimal getOwed() {
		return owed;
	}
	public BigDecimal getLoanAmount() {
		return loanAmount;
	}
    
}
