package org.apache.fineract.portfolio.dashboard.service;

import java.util.Date;

import javax.ws.rs.core.UriInfo;

import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.portfolio.dashboard.data.BranchManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.GeneralManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.InputData;
import org.apache.fineract.portfolio.dashboard.data.ViewDetailsData;

import net.minidev.json.JSONObject;

public interface DashboardReadPlatformService {
	
	JSONObject generalManagerLandingRetrieveDashboardTemplate(final String chartType,final DateParam from,final DateParam to,final Long productId,final Long officeId);
	String generalManagerretrieveChartByFilter(final InputData input,  final UriInfo uriInfo);
	
	JSONObject branchManagerLandingRetrieveDashboardTemplate(final String chartType,final DateParam from,final DateParam to);
	String branchManagerretrieveChartByFilter(final InputData input,  final UriInfo uriInfo);
	JSONObject loanOfficerLandingLandingRetrieveDashboardTemplate(final String chartType,final DateParam from,final DateParam to);
	String loanOfficerretrieveChartByFilter(final InputData input,  final UriInfo uriInfo);
	ViewDetailsData retrieveviewdetails(final String ViewDetailsName,final  DateParam from,final  DateParam to);
	JSONObject retrieveClientDashboardTemplate(Long clientId);
}
