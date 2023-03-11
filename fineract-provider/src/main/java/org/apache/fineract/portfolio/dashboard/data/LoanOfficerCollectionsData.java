package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;
@SuppressWarnings("unused")
public class LoanOfficerCollectionsData {
	
    private final BigDecimal amount;
   
    public LoanOfficerCollectionsData(final BigDecimal amount) {
		this.amount = amount;		
	}


	public BigDecimal getAmount() {
		return amount;
	}

}
