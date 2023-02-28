package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;

public class ViewManagerCollectionData {
	private final String accNo; 
	private final String name; 
	private final String officerName;
	private final BigDecimal total;
	private final BigDecimal collected;
	private final BigDecimal due;
	public ViewManagerCollectionData(final String accNo,final String name,final String officerName,final BigDecimal total,
			final BigDecimal collected,final BigDecimal due) {
		this.accNo = accNo;
		this.name = name;
		this.officerName= officerName;
		this.total = total;
		this.collected = collected;
		this.due= due;
	}
}
