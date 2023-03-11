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

public class PendingApprovalLoansData {

    private final Long id;
    private final String Name;
    private final String Account;
    private final String Gender;
    private final String MobileNumber;
    private final String Officer;
    private final String Branch;

    public PendingApprovalLoansData(final Long id, final String Name, final String Account, final String Gender, final String MobileNumber,
            final String Officer, final String Branch) {
        this.id = id;
        this.Name = Name;
        this.Account = Account;
        this.Gender = Gender;
        this.MobileNumber = MobileNumber;
        this.Officer = Officer;
        this.Branch = Branch;
    }

    public String getName() {
        return Name;
    }

    public String getAccount() {
        return Account;
    }

    public String getGender() {
        return Gender;
    }

    public String getMobileNumber() {
        return MobileNumber;
    }

    public String getOfficer() {
        return Officer;
    }

    public String getBranch() {
        return Branch;
    }

    public Long getId() {
        return id;
    }

}
