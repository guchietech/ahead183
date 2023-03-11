/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.portfolio.dashboard.api;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.dashboard.data.BranchManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.GeneralManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.GeneralManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.service.DashboardReadPlatformService;
import org.apache.fineract.portfolio.dashboard.data.InputData;
import org.apache.fineract.portfolio.dashboard.data.ViewDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import net.minidev.json.JSONObject;

@Path("/dashboard")
@Component
@Scope("singleton")
public class DashboardApiResource {
	private final PlatformSecurityContext context;
	private final DashboardReadPlatformService dashboardReadPlatformService;
	private final ToApiJsonSerializer<JSONObject> toApiJsonSerializer;
	private final ToApiJsonSerializer<ViewDetailsData> toApiJsonSerializerManagerViewDetailsData;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	@Autowired
    public DashboardApiResource(final PlatformSecurityContext context,
    		final DashboardReadPlatformService dashboardReadPlatformService,
            final ToApiJsonSerializer<JSONObject> toApiJsonSerializer,
            final ToApiJsonSerializer<ViewDetailsData> toApiJsonSerializerManagerViewDetailsData,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.dashboardReadPlatformService = dashboardReadPlatformService;
		this.toApiJsonSerializerManagerViewDetailsData= toApiJsonSerializerManagerViewDetailsData;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
	}
	@GET
	@Path("general")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String generalManagerLanding(@Context final UriInfo uriInfo, @QueryParam("chartType") final String chartType,@QueryParam("from") final DateParam from,
			@QueryParam("to") final DateParam to,@QueryParam("productId") final Long productId,@QueryParam("officeId") final Long officeId) {
		this.context.authenticatedUser();
		JSONObject dashboardData = this.dashboardReadPlatformService.generalManagerLandingRetrieveDashboardTemplate(chartType,from,to,productId,officeId);
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, dashboardData);
	}
	
	@GET
	@Path("manager")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String branchManagerLanding(@Context final UriInfo uriInfo, @QueryParam("chartType") final String chartType,@QueryParam("from") final DateParam from,
			@QueryParam("to") final DateParam to) {
		this.context.authenticatedUser();
		JSONObject dashboardData = this.dashboardReadPlatformService.branchManagerLandingRetrieveDashboardTemplate(chartType,from,to);
		
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, dashboardData);
	}
	
	@GET
	@Path("viewdetails")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String managerViewDetails(@Context final UriInfo uriInfo, @QueryParam("ViewDetailsName") final String ViewDetailsName,@QueryParam("from") final DateParam from,
			@QueryParam("to") final DateParam to) {
		this.context.authenticatedUser();
		ViewDetailsData viewDetailsData = this.dashboardReadPlatformService.retrieveviewdetails(ViewDetailsName,from,to);
		
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializerManagerViewDetailsData.serialize(settings, viewDetailsData);
	}
	
	@GET
	@Path("officer")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String loanOfficerLanding(@Context final UriInfo uriInfo, @QueryParam("chartType") final String chartType,@QueryParam("from") final DateParam from,
			@QueryParam("to") final DateParam to) {
		this.context.authenticatedUser();
		JSONObject dashboardData = this.dashboardReadPlatformService.loanOfficerLandingLandingRetrieveDashboardTemplate(chartType,from,to);
		
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, dashboardData);
	}
	
	@GET
	@Path("client")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String clientLanding(@Context final UriInfo uriInfo, @QueryParam("clientId") final Long clientId) {
		this.context.authenticatedUser();
		JSONObject dashboardData = this.dashboardReadPlatformService.retrieveClientDashboardTemplate(clientId);
		
		final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, dashboardData);
	}
	
	@GET
	@Path("generalmanagerfilter")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String generalManagerFilter(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {
		this.context.authenticatedUser();
		
		Gson g = new Gson();
    	InputData inputData = g.fromJson(apiRequestBodyAsJson, InputData.class);
    	
    	final String output = this.dashboardReadPlatformService.generalManagerretrieveChartByFilter(inputData, uriInfo);
    	
        return output;
	}
	@GET
	@Path("branchmanagerfilter")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
	public String branchManagerFilter(@Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {
		this.context.authenticatedUser();
		
		Gson g = new Gson();
    	InputData inputData = g.fromJson(apiRequestBodyAsJson, InputData.class);
    	
    	final String output = this.dashboardReadPlatformService.branchManagerretrieveChartByFilter(inputData, uriInfo);
    	
        return output;
	}

}
