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
public class WeeklyChartData {

    
    private final BigDecimal Day1;
    private final BigDecimal Day2;
    private final BigDecimal Day3;
    private final BigDecimal Day4;
    private final BigDecimal Day5;
    private final BigDecimal Day6;
    private final BigDecimal Day7;
   
    public WeeklyChartData(final BigDecimal Day1,final BigDecimal Day2,final BigDecimal Day3,final BigDecimal Day4,
    		final BigDecimal Day5,final BigDecimal Day6,final BigDecimal Day7) {
		this.Day1 = Day1;
		this.Day2 = Day2;
		this.Day3 = Day3;
		this.Day4 = Day4;
		this.Day5 = Day5;
		this.Day6 = Day6;
		this.Day7 = Day7;
	}

	public BigDecimal getDay1() {
		return Day1;
	}

	public BigDecimal getDay2() {
		return Day2;
	}

	public BigDecimal getDay3() {
		return Day3;
	}

	public BigDecimal getDay4() {
		return Day4;
	}

	public BigDecimal getDay5() {
		return Day5;
	}

	public BigDecimal getDay6() {
		return Day6;
	}

	public BigDecimal getDay7() {
		return Day7;
	}

	
}