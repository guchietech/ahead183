package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class BranchManagercollectionsData {
    private final BigDecimal amount;
   
    public BranchManagercollectionsData(final BigDecimal amount) {
		this.amount = amount;
		
    }


	public BigDecimal getAmount() {
		return amount;
	}
}
