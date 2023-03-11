package org.apache.fineract.portfolio.dashboard.data;

@SuppressWarnings("unused")
public class InputData {

    private final String chartID;
    private final String filterID;
    private final Long filterValue;
    private final String fromDate;
    private final String toDate;
    private final Long productId;
    

    private InputData(final String chartID,
    	     final String filterID,
    	     final Long filterValue,
    	     final String fromDate,
    	     final String toDate,
    	     final Long productId) {
		this.chartID = chartID;
		this.filterID = filterID;
		this.filterValue = filterValue;
		this.fromDate = fromDate;
		this.toDate = toDate;
		
		this.productId = productId;
	}


	public String getChartID() {
		return chartID;
	}


	public String getFilterID() {
		return filterID;
	}


	public Long getFilterValue() {
		return filterValue;
	}


	public String getFromDate() {
		return fromDate;
	}


	public String getToDate() {
		return toDate;
	}
	
	public Long getProductd() {
		return productId;
	}

    
}