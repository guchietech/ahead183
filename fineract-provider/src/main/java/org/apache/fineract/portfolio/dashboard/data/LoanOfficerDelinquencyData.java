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
import java.time.LocalDate;

@SuppressWarnings("unused")
public class LoanOfficerDelinquencyData {

    private final String displayname;
    private final BigDecimal delinquentamount;
    private LocalDate duedate;

    public LoanOfficerDelinquencyData(final String displayname, final BigDecimal delinquentamount, final LocalDate duedate) {
        this.displayname = displayname;
        this.delinquentamount = delinquentamount;
        this.duedate = duedate;
    }

    public String getDisplayname() {
        return displayname;
    }

    public BigDecimal getDelinquentamount() {
        return delinquentamount;
    }

    public LocalDate getDuedate() {
        return duedate;
    }

    public void setDuedate(LocalDate duedate) {
        this.duedate = duedate;
    }

}
