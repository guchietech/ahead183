package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;
@SuppressWarnings("unused")
public class ManagerActiveLoanSummaryData {
	private final String accNo;
	private final String name; 
	private final Long mobNo;
	private final String gender; 
	private final String office; 
	private final String officerName;
	private final BigDecimal principal;
	public ManagerActiveLoanSummaryData(final String accNo,final String name,final Long mobNo,final String gender,
			final String office,final String officerName,final BigDecimal principal) {
		this.accNo = accNo;
		this.name = name;
		this.mobNo = mobNo;
		this.gender = gender;
		this.office = office;
		this.officerName = officerName;
		this.principal= principal;
	}


}
