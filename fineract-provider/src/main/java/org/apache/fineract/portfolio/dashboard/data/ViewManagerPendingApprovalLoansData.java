package org.apache.fineract.portfolio.dashboard.data;

public class ViewManagerPendingApprovalLoansData {
	private final String id;
	private final String accNo;
	private final String name; 
	private final Long mobNo;
	private final String gender; 
	private final String office; 
	private final String officerName;
	
	public ViewManagerPendingApprovalLoansData(final String id ,final String accNo,final String name,final Long mobNo,final String gender,
			final String office,final String officerName) {
		this.id = id;
		this.accNo = accNo;
		this.name = name;
		this.mobNo = mobNo;
		this.gender = gender;
		this.office = office;
		this.officerName = officerName;
		
	}
}
