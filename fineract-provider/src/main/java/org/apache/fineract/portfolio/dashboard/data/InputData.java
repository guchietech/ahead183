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

@SuppressWarnings("unused")
public class InputData {

    private final String chartID;
    private final String filterID;
    private final Long filterValue;
    private final String fromDate;
    private final String toDate;
    private final Long productId;

    private InputData(final String chartID, final String filterID, final Long filterValue, final String fromDate, final String toDate,
            final Long productId) {
        this.chartID = chartID;
        this.filterID = filterID;
        this.filterValue = filterValue;
        this.fromDate = fromDate;
        this.toDate = toDate;

        this.productId = productId;
    }

    public String getChartID() {
        return chartID;
    }

    public String getFilterID() {
        return filterID;
    }

    public Long getFilterValue() {
        return filterValue;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public Long getProductd() {
        return productId;
    }

}
