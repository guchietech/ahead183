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

import java.math.BigDecimal;

public class LoanPerformanceData {

    private final BigDecimal arrears;
    private final BigDecimal paid;
    private final BigDecimal owed;
    // private final BigDecimal due;
    private final BigDecimal loanAmount;

    public LoanPerformanceData(final BigDecimal arrears, final BigDecimal paid, final BigDecimal owed, final BigDecimal loanAmount) {
        this.arrears = arrears;
        this.paid = paid;
        this.owed = owed;
        // this.due =due;
        this.loanAmount = loanAmount;

    }

    public BigDecimal getArrears() {
        return arrears;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public BigDecimal getOwed() {
        return owed;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

}
