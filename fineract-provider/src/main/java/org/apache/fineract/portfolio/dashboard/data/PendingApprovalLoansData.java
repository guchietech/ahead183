package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

public class PendingApprovalLoansData {
	
	private final Long id;
	private final String Name;
	private final String Account;
	private final String Gender;
	private final String MobileNumber;
	private final String Officer;
	private final String Branch;
	

	public PendingApprovalLoansData(final Long id,final String Name, final String Account, final String Gender
			, final String MobileNumber, final String Officer, final String Branch) {
		this.id = id;
		this.Name = Name;
		this.Account = Account;
		this.Gender = Gender;
		this.MobileNumber = MobileNumber;
		this.Officer = Officer;
		this.Branch = Branch;
	}


	public String getName() {
		return Name;
	}


	public String getAccount() {
		return Account;
	}


	public String getGender() {
		return Gender;
	}


	public String getMobileNumber() {
		return MobileNumber;
	}


	public String getOfficer() {
		return Officer;
	}


	public String getBranch() {
		return Branch;
	}


	public Long getId() {
		return id;
	}

}
