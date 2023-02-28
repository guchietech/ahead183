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

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Immutable data object represent the important time-line events of a loan
 * application and loan.
 */
@SuppressWarnings("unused")
public class StackedChartData {

    
    private final BigDecimal activeCustomers;
    private final BigDecimal nonActiveCustomer;
    private final BigDecimal nonFundedCustomer;
   
    public StackedChartData(final BigDecimal activeCustomers, final BigDecimal nonActiveCustomer,
    		final BigDecimal nonFundedCustomer) {
		this.activeCustomers = activeCustomers;
		this.nonActiveCustomer = nonActiveCustomer;
		this.nonFundedCustomer = nonFundedCustomer;
		
	}

	public BigDecimal getActiveCustomers() {
		return activeCustomers;
	}

	public BigDecimal getNonActiveCustomer() {
		return nonActiveCustomer;
	}

	public BigDecimal getNonFundedCustomer() {
		return nonFundedCustomer;
	}

    
}