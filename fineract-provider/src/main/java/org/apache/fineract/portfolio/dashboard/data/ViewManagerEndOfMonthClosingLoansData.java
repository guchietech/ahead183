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

public class ViewManagerEndOfMonthClosingLoansData {

    private final String accNo;
    private final String name;
    private final Long mobNo;
    private final String gender;
    private final String office;
    private final String officerName;
    private final Long installment;

    public ViewManagerEndOfMonthClosingLoansData(final String accNo, final String name, final Long mobNo, final String gender,
            final String office, final String officerName, final Long installment) {
        this.accNo = accNo;
        this.name = name;
        this.mobNo = mobNo;
        this.gender = gender;
        this.office = office;
        this.officerName = officerName;
        this.installment = installment;
    }
}
