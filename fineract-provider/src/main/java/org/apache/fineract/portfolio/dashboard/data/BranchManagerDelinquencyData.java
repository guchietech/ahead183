package org.apache.fineract.portfolio.dashboard.data;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;
@SuppressWarnings("unused")
public class BranchManagerDelinquencyData {
	 	private final String displayname;
	    private final BigDecimal delinquentamount;
	    private LocalDate duedate;
	   
	    public BranchManagerDelinquencyData(final String displayname, final BigDecimal delinquentamount,final LocalDate duedate) {
			this.displayname = displayname;
			this.delinquentamount = delinquentamount;
			this.duedate = duedate;
			
		}

		public LocalDate getDuedate() {
			return duedate;
		}

		public void setDuedate(LocalDate duedate) {
			this.duedate = duedate;
		}

		public String getDisplayname() {
			return displayname;
		}

		public BigDecimal getDelinquentamount() {
			return delinquentamount;
		}
	    
}
