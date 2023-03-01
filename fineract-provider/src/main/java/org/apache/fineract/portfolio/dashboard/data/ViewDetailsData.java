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

package org.apache.fineract.portfolio.dashboard.data;

import java.util.List;

public class ViewDetailsData {

    private final List<PendingApprovalLoansData> ManagerCustomers;
    private final List<ManagerActiveLoanSummaryData> ManagerActiveLoanSummary;
    private final List<ViewManagerPendingApprovalLoansData> ViewManagerPendingApprovalLoans;
    private final List<ViewManagerEndOfMonthClosingLoansData> ViewManagerEndOfMonthClosing;
    private final List<ViewManagerCollectionData> ViewManagerCollection;
    private final List<ViewManagerPortfolioData> ViewManagerPortfolio;
    private final List<ViewOfficerPortfolioData> ViewOfficerPortfolio;
    private final List<ViewOfficerCollectionData> ViewOfficerCollection;
    private final List<ViewOfficerCustomersData> ViewOfficerCustomers;
    private final List<ViewOfficerPendingApprovalLoansData> ViewOfficerPendingApprovalLoans;
    private final List<PendingApprovalLoansData> pendingApprovalLoansDataGeneral;

    public ViewDetailsData(final List<PendingApprovalLoansData> ManagerCustomers,
            final List<ManagerActiveLoanSummaryData> ManagerActiveLoanSummary,
            final List<ViewManagerPendingApprovalLoansData> ViewManagerPendingApprovalLoans,
            final List<ViewManagerEndOfMonthClosingLoansData> ViewManagerEndOfMonthClosing,
            final List<ViewManagerCollectionData> ViewManagerCollection, final List<ViewManagerPortfolioData> ViewManagerPortfolio,
            final List<ViewOfficerPortfolioData> ViewOfficerPortfolio, final List<ViewOfficerCollectionData> ViewOfficerCollection,
            final List<ViewOfficerCustomersData> ViewOfficerCustomers,
            final List<ViewOfficerPendingApprovalLoansData> ViewOfficerPendingApprovalLoans,
            final List<PendingApprovalLoansData> pendingApprovalLoansDataGeneral) {

        this.ManagerCustomers = ManagerCustomers;
        this.ManagerActiveLoanSummary = ManagerActiveLoanSummary;
        this.ViewManagerPendingApprovalLoans = ViewManagerPendingApprovalLoans;
        this.ViewManagerEndOfMonthClosing = ViewManagerEndOfMonthClosing;
        this.ViewManagerCollection = ViewManagerCollection;
        this.ViewManagerPortfolio = ViewManagerPortfolio;
        this.ViewOfficerPortfolio = ViewOfficerPortfolio;
        this.ViewOfficerCollection = ViewOfficerCollection;
        this.ViewOfficerCustomers = ViewOfficerCustomers;
        this.ViewOfficerPendingApprovalLoans = ViewOfficerPendingApprovalLoans;
        this.pendingApprovalLoansDataGeneral = pendingApprovalLoansDataGeneral;
    }

}
