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
public class DurationChartData {

    
    private final BigDecimal Current;
    private final BigDecimal PAR1;
    private final BigDecimal PAR31;
    private final BigDecimal PAR61;
    private final BigDecimal PAR91;
    private final Long productId;
   
    public DurationChartData(final BigDecimal Current,final BigDecimal PAR1,final BigDecimal PAR31,final BigDecimal PAR61,
    		final BigDecimal PAR91,final Long productId) {
		this.Current = Current;
		this.PAR1 = PAR1;
		this.PAR31 = PAR31;
		this.PAR61 = PAR61;
		this.PAR91 = PAR91;
		this.productId = productId;
	}

	public BigDecimal getCurrent() {
		return Current;
	}

	public BigDecimal getPAR1() {
		return PAR1;
	}

	public BigDecimal getPAR31() {
		return PAR31;
	}

	public BigDecimal getPAR61() {
		return PAR61;
	}

	public BigDecimal getPAR91() {
		return PAR91;
	}

	public Long getProductId() {
		return productId;
	}
    
}