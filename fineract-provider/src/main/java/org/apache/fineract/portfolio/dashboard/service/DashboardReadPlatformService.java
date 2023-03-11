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

package org.apache.fineract.portfolio.dashboard.service;

import javax.ws.rs.core.UriInfo;
import net.minidev.json.JSONObject;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.portfolio.dashboard.data.InputData;
import org.apache.fineract.portfolio.dashboard.data.ViewDetailsData;

public interface DashboardReadPlatformService {

    JSONObject generalManagerLandingRetrieveDashboardTemplate(final String chartType, final DateParam from, final DateParam to,
            final Long productId, final Long officeId);

    String generalManagerretrieveChartByFilter(final InputData input, final UriInfo uriInfo);

    JSONObject branchManagerLandingRetrieveDashboardTemplate(final String chartType, final DateParam from, final DateParam to);

    String branchManagerretrieveChartByFilter(final InputData input, final UriInfo uriInfo);

    JSONObject loanOfficerLandingLandingRetrieveDashboardTemplate(final String chartType, final DateParam from, final DateParam to);

    String loanOfficerretrieveChartByFilter(final InputData input, final UriInfo uriInfo);

    ViewDetailsData retrieveviewdetails(final String ViewDetailsName, final DateParam from, final DateParam to);

    JSONObject retrieveClientDashboardTemplate(Long clientId);
}
