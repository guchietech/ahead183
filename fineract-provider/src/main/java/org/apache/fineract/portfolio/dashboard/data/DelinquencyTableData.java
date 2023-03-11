package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class DelinquencyTableData {
	private final String displayname;
    private final BigDecimal amount;

   
    public DelinquencyTableData(final String displayname, final BigDecimal amount) {
		this.displayname = displayname;
		this.amount = amount;		
	}

}
