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
package org.apache.fineract.cob.loan;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.infrastructure.core.exception.MultiException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.service.LoanAccrualPlatformService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddPeriodicAccrualEntriesBusinessStep implements LoanCOBBusinessStep {

    private final LoanAccrualPlatformService loanAccrualPlatformService;

    @Override
    public Loan execute(Loan loan) {
        try {
            loanAccrualPlatformService.addPeriodicAccruals(DateUtils.getBusinessLocalDate(), loan);
        } catch (MultiException e) {
            throw new BusinessStepException(String.format("Fail to process period accrual for loan id [%s]", loan.getId()), e);
        }
        return loan;
    }

    @Override
    public String getEnumStyledName() {
        return "ADD_PERIODIC_ACCRUAL_ENTRIES";
    }

    @Override
    public String getHumanReadableName() {
        return "Add periodic accrual entries";
    }
}
