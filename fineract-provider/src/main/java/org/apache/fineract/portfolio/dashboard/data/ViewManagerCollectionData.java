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

import org.apache.fineract.infrastructure.core.domain.JdbcSupport;

public class ViewManagerCollectionData {
	private final String accNo; 
	private final String name; 
	private final String officerName;
	private final BigDecimal total;
	private final BigDecimal collected;
	private final BigDecimal due;
	public ViewManagerCollectionData(final String accNo,final String name,final String officerName,final BigDecimal total,
			final BigDecimal collected,final BigDecimal due) {
		this.accNo = accNo;
		this.name = name;
		this.officerName= officerName;
		this.total = total;
		this.collected = collected;
		this.due= due;
	}
}
