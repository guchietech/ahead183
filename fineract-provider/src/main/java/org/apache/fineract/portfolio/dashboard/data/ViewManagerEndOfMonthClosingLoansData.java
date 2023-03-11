package org.apache.fineract.portfolio.dashboard.data;

public class ViewManagerEndOfMonthClosingLoansData {
	private final String accNo;
	private final String name; 
	private final Long mobNo;
	private final String gender; 
	private final String office; 
	private final String officerName;
	private final Long installment;
	public ViewManagerEndOfMonthClosingLoansData(final String accNo,final String name,final Long mobNo,final String gender,
			final String office,final String officerName,final Long installment) {
		this.accNo = accNo;
		this.name = name;
		this.mobNo = mobNo;
		this.gender = gender;
		this.office = office;
		this.officerName = officerName;
		this.installment= installment;
	}
}
