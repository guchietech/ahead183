package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;
@SuppressWarnings("unused")
public class LoanOfficerPortfolioData {
    private final BigDecimal amount;
   
    public LoanOfficerPortfolioData(final BigDecimal amount) {
		this.amount = amount;
    }
	public BigDecimal getAmount() {
		return amount;
	}
    
}
