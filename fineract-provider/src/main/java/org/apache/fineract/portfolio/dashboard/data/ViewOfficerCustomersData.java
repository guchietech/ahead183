package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

public class ViewOfficerCustomersData {
	private final String accNo;
	private final String name; 
	private final String mobNo;
	private final String gender; 
	private final String office; 
	private final String officerName;
	public ViewOfficerCustomersData(final String accNo,final String name,final String mobNo,final String gender,
			final String office,final String officerName) {
		this.accNo = accNo;
		this.name = name;
		this.mobNo = mobNo;
		this.gender = gender;
		this.office = office;
		this.officerName = officerName;
	}
}
