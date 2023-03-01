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

package org.apache.fineract.portfolio.dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import javax.ws.rs.core.UriInfo;
import org.joda.time.LocalDate;

import org.apache.commons.lang.time.DateUtils;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.dashboard.data.BranchManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.BranchManagerDelinquencyData;
import org.apache.fineract.portfolio.dashboard.data.BranchManagerPortfolioData;
import org.apache.fineract.portfolio.dashboard.data.BranchManagercollectionsData;
import org.apache.fineract.portfolio.dashboard.data.GeneralManagerDashboardData;
import org.apache.fineract.portfolio.dashboard.data.InputData;
import org.apache.fineract.portfolio.dashboard.data.LoanOfficerCollectionsData;
import org.apache.fineract.portfolio.dashboard.data.LoanOfficerCustomersData;
import org.apache.fineract.portfolio.dashboard.data.LoanOfficerDelinquencyData;
import org.apache.fineract.portfolio.dashboard.data.LoanOfficerPortfolioData;
import org.apache.fineract.portfolio.dashboard.data.LoanPerformanceData;
import org.apache.fineract.portfolio.dashboard.data.LoanPerformanceDueData;
import org.apache.fineract.portfolio.dashboard.data.ManagerActiveLoanSummaryData;
import org.apache.fineract.portfolio.dashboard.data.PendingApprovalLoansData;
import org.apache.fineract.portfolio.dashboard.data.PieChartData;
import org.apache.fineract.portfolio.dashboard.data.StackedChartData;
import org.apache.fineract.portfolio.dashboard.data.ToDoListData;
import org.apache.fineract.portfolio.dashboard.data.ViewDetailsData;
import org.apache.fineract.portfolio.dashboard.data.ViewManagerCollectionData;
import org.apache.fineract.portfolio.dashboard.data.ViewManagerEndOfMonthClosingLoansData;
import org.apache.fineract.portfolio.dashboard.data.ViewManagerPendingApprovalLoansData;
import org.apache.fineract.portfolio.dashboard.data.ViewManagerPortfolioData;
import org.apache.fineract.portfolio.dashboard.data.ViewOfficerCollectionData;
import org.apache.fineract.portfolio.dashboard.data.ViewOfficerCustomersData;
import org.apache.fineract.portfolio.dashboard.data.ViewOfficerPendingApprovalLoansData;
import org.apache.fineract.portfolio.dashboard.data.ViewOfficerPortfolioData;
import org.apache.fineract.portfolio.dashboard.data.DurationChartData;
import org.apache.fineract.portfolio.dashboard.data.WeeklyChartData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Service
public class DashboardReadPlatformServiceImpl implements DashboardReadPlatformService{
	private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private static final String SQL_DATEFORMAT = "yyyy-MM-dd";
	private static final String ACTIVE_CUSTOMERS = "SELECT mp.name as categories, COUNT(DISTINCT ml.client_id) as series FROM m_loan ml JOIN m_product_loan mp ON mp.id = ml.product_id WHERE ml.loan_status_id=300 AND ml.client_id IS NOT null GROUP BY ml.product_id";
	private static final String PAR_PER_BRANCH = "select mp.name AS categories, COUNT(ml.id) AS series from m_loan_arrears_aging la join m_loan ml on ml.id = la.loan_id JOIN m_product_loan mp ON mp.id=ml.product_id GROUP BY ml.product_id;" + 
			"";
	private static final String LOAN_IN_BAD_STANDING = "SELECT 1 AS productId, IFNULL(SUM(CASE Bucket WHEN 'Current' THEN overdue END),0) AS CurrentAmount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR1' THEN overdue END),0) AS PAR1Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR31' THEN overdue END),0) AS PAR31Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR61' THEN overdue END),0) AS PAR61Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR91' THEN overdue END),0) AS PAR91Amount\n" + 
			"							\n" + 
			"\n" + 
			"\n" + 
			"FROM \n" + 
			"(SELECT CASE WHEN IFNULL(DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)),0) BETWEEN 1 AND 30 THEN 'PAR1' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)),0) BETWEEN 31 AND 60 THEN 'PAR31' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)),0) BETWEEN 61 AND 90 THEN 'PAR61' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)),0)  > 91 THEN 'PAR91'\n" + 
			"\n" + 
			" ELSE 'Current' END AS Bucket,mla.total_overdue_derived AS overdue \n" + 
			" FROM m_loan_arrears_aging mla) mlr;";
	private static final String SAVINGS_BALANCE_PER_BRANCH = "SELECT mlr.productId, IFNULL(SUM(CASE Bucket WHEN 'Current' THEN outstanding END),0) AS CurrentAmount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR1' THEN outstanding END),0) AS PAR1Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR31' THEN outstanding END),0) AS PAR31Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR61' THEN outstanding END),0) AS PAR61Amount\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'PAR91' THEN outstanding END),0) AS PAR91Amount\n" + 
			"							\n" + 
			"\n" + 
			"\n" + 
			"FROM \n" + 
			"(SELECT CASE WHEN IFNULL(DATEDIFF(CURDATE(), (ms.activatedon_date)),0) BETWEEN 1 AND 30 THEN 'PAR1' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (ms.activatedon_date)),0) BETWEEN 31 AND 60 THEN 'PAR31' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (ms.activatedon_date)),0) BETWEEN 61 AND 90 THEN 'PAR61' \n" + 
			"WHEN IFNULL(DATEDIFF(CURDATE(), (ms.activatedon_date)),0)  > 91 THEN 'PAR91'\n" + 
			"\n" + 
			" ELSE 'Current' END AS Bucket,ms.account_balance_derived AS outstanding,ms.product_id AS productId FROM m_savings_account ms\n" + 
			"WHERE ms.deposit_type_enum = 100  \n" + 
			"AND ms.status_enum = 300 PRODUCT_CONDITION) mlr\n" + 
			"GROUP BY mlr.productId limit 1 ;";
	
	private static final String CUSTOMERS_NUMBER_BRANCH = "SELECT mca.ActiveCustomer,mca.NonActiveCustomer,mc.NonFundedCustomer FROM\n" + 
			"(SELECT IFNULL(SUM(CASE mc.status_enum WHEN 300 THEN 1 END),0) AS ActiveCustomer,\n" + 
			"IFNULL(SUM(CASE mc.status_enum WHEN 100 THEN 1 END),0) AS NonActiveCustomer\n" + 
			"FROM m_client mc ) mca,\n" + 
			"(SELECT COUNT(*) AS NonFundedCustomer FROM m_client mc\n" + 
			"WHERE mc.id NOT IN (SELECT ml.client_id FROM m_loan ml \n" + 
			"WHERE ml.client_id IS NOT null\n" + 
			"GROUP BY ml.client_id)) mc;";
	
	private static final String LOANS_TO_BE_PAID_IN_CURRENT_WEEK = "SELECT \n" + 
			"IFNULL(SUM(CASE Bucket WHEN 'DIFF1' THEN (ml.totalDue-totalPaid) END),0) AS DAY1\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF2' THEN (ml.totalDue-totalPaid) END),0) AS DAY2\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF3' THEN (ml.totalDue-totalPaid) END),0) AS DAY3\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF4' THEN (ml.totalDue-totalPaid) END),0) AS DAY4\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF5' THEN (ml.totalDue-totalPaid) END),0) AS DAY5\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF6' THEN (ml.totalDue-totalPaid) END),0) AS DAY6\n" + 
			"							, IFNULL(SUM(CASE Bucket WHEN 'DIFF7' THEN (ml.totalDue-totalPaid) END),0) AS DAY7\n" + 
			"\n" + 
			"FROM \n" + 
			"(SELECT CASE WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =1 THEN 'DIFF1' \n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =2 THEN 'DIFF2' \n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =3 THEN 'DIFF3' \n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =4 THEN 'DIFF4'\n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =5 THEN 'DIFF5'\n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =6 THEN 'DIFF6'\n" + 
			"WHEN IFNULL(DATEDIFF((mlr.duedate),CURDATE()),0) =7 THEN 'DIFF7'\n" + 
			"END AS Bucket,\n" + 
			"(IFNULL(mlr.principal_amount,0)+IFNULL(mlr.interest_amount,0)+\n" + 
			"   IFNULL(mlr.fee_charges_amount,0)+IFNULL(mlr.penalty_charges_amount,0)) AS totalDue,\n" + 
			"(IFNULL(mlr.principal_completed_derived,0)+IFNULL(mlr.principal_writtenoff_derived,0)+\n" + 
			"IFNULL(mlr.interest_completed_derived,0)+IFNULL(mlr.interest_writtenoff_derived,0)+IFNULL(mlr.interest_waived_derived,0)+\n" + 
			"IFNULL(mlr.penalty_charges_completed_derived,0)+IFNULL(mlr.penalty_charges_writtenoff_derived,0)+\n" + 
			"IFNULL(mlr.penalty_charges_waived_derived,0)+IFNULL(mlr.fee_charges_completed_derived,0)+\n" + 
			"IFNULL(mlr.fee_charges_writtenoff_derived,0)+IFNULL(mlr.fee_charges_waived_derived,0)) AS totalPaid\n" + 
			"FROM m_loan_repayment_schedule mlr\n" + 
			"WHERE mlr.duedate BETWEEN CURDATE() + INTERVAL 1 DAY AND CURDATE() + INTERVAL 7 DAY\n" + 
			") ml";
	
	private static final String NO_OF_CENTERS = "Select COUNT(*) AS count from m_group mg where mg.level_id=1\n" + 
			"AND mg.status_enum=300;";
	
	private static final String NO_OF_GROUPS = "Select COUNT(*) AS count from m_group mg where mg.level_id=2\n" + 
			"AND mg.status_enum=300;";
	
	private static final String NO_OF_BORROWERS = "Select COUNT(*) AS count from m_client mc\n" + 
			"WHERE mc.status_enum=300;";
	
	private static final String Branch_Manager_collections = "select  (SUM(ifnull(lrs.principal_amount, 0)) + SUM(ifnull(lrs.interest_amount, 0)) + SUM(ifnull(lrs.fee_charges_amount, 0)) + SUM(ifnull(lrs.penalty_charges_amount, 0))) as amount from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_loan_repayment_schedule lrs on lrs.loan_id = l.id where lrs.duedate between ? and ? and o.id = ?;";
	private static final String Branch_Manager_collected = "select (SUM(ifnull(ltrs.principal_portion_derived, 0)) + SUM(ifnull(ltrs.interest_portion_derived, 0)) + SUM(ifnull(ltrs.fee_charges_portion_derived, 0)) + SUM(ifnull(ltrs.penalty_charges_portion_derived, 0))) as amount from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_loan_repayment_schedule lrs on lrs.loan_id = l.id join m_loan_transaction_repayment_schedule_mapping ltrs on ltrs.loan_repayment_schedule_id = lrs.id where ltrs.loan_repayment_schedule_id in (select lrs.id from m_loan_repayment_schedule lrs join m_loan l on l.id = lrs.loan_id where lrs.duedate between ? and ? ) and o.id = ?";
	private static final String View_Branch_Manager_collected = "select c.display_name AS name, l.account_no as accNo, ms.display_name AS officerName, ((ifnull(lrs.principal_amount, 0)) + (ifnull(lrs.interest_amount, 0)) + (ifnull(lrs.fee_charges_amount, 0)) + (ifnull(lrs.penalty_charges_amount, 0))) as total, ((ifnull(lrs.principal_completed_derived, 0)) + (ifnull(lrs.interest_completed_derived, 0)) + (ifnull(lrs.fee_charges_completed_derived, 0)) + (ifnull(lrs.penalty_charges_completed_derived, 0))) as amount, (((ifnull(lrs.principal_amount, 0)) + (ifnull(lrs.interest_amount, 0)) + (ifnull(lrs.fee_charges_amount, 0)) + (ifnull(lrs.penalty_charges_amount, 0))) - ((ifnull(lrs.principal_completed_derived, 0)) + (ifnull(lrs.interest_completed_derived, 0)) + (ifnull(lrs.fee_charges_completed_derived, 0)) + (ifnull(lrs.penalty_charges_completed_derived, 0)))) as due from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id join m_loan_repayment_schedule lrs on lrs.loan_id = l.id join m_loan_transaction_repayment_schedule_mapping ltrs on ltrs.loan_repayment_schedule_id = lrs.id where ltrs.loan_repayment_schedule_id in (select lrs.id from m_loan_repayment_schedule lrs join m_loan l on l.id = lrs.loan_id where lrs.duedate between ? and ? ) and o.id = ?;";
	private static final String Branch_Manager_Portfolio = "select SUM(l.principal_amount) as amount FROM m_office o JOIN m_office ounder ON ounder.hierarchy LIKE CONCAT(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id WHERE l.disbursedon_date BETWEEN ? AND ? AND o.id=?";
	private static final String View_Branch_Manager_Portfolio = "select c.display_name AS name, l.account_no as accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName, l.principal_amount as principal from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id where l.disbursedon_date between ? and ?  and o.id = ?";	
	private static final String Branch_Manager_Delinquency = "SELECT c.display_name AS displayname, lrs.duedate AS duedate, (l.principal_outstanding_derived +l.interest_outstanding_derived +l.fee_charges_outstanding_derived +l.penalty_charges_outstanding_derived)AS delinquentamount FROM m_office o JOIN m_office ounder ON ounder.hierarchy like concat(o.hierarchy, '%') JOIN m_client c ON c.office_id = ounder.id JOIN m_loan l ON l.client_id = c.id JOIN m_loan_repayment_schedule lrs ON lrs.loan_id = l.id JOIN m_loan_arrears_aging mla ON mla.loan_id = l.id WHERE DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)) >= ? AND lrs.duedate BETWEEN ? AND ? AND lrs.completed_derived = 0 AND o.id = ?";
	private static final String Loan_Officer_Customers="SELECT COUNT(c.id) AS COUNT\n" + 
			"FROM m_client c\n" + 
			"WHERE c.activation_date BETWEEN ? AND ? AND c.staff_id = ?";
	private static final String View_Loan_Officer_Customers = "SELECT c.display_name AS name, c.account_no AS accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName\n" + 
			"FROM m_client c\n" + 
			"LEFT JOIN m_code_value mc ON mc.id = c.gender_cv_id\n" + 
			"LEFT JOIN m_staff ms ON ms.id = c.staff_id\n" + 
			"LEFT JOIN m_office o ON o.id = c.office_id\n" + 
			"WHERE c.activation_date BETWEEN ? AND ? AND c.staff_id = ?";
	private static final String Loan_Officer_Portfolio="select Sum(l.principal_amount) as amount from   m_loan l join m_client c on c.id = l.client_id where  l.disbursedon_date between ? and ? and l.loan_officer_id = ?";
	private static final String View_Officer_Portfolio="select c.display_name AS name, l.account_no as accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName, l.principal_amount as principal from   m_loan l join m_client c on c.id = l.client_id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id left join m_office o on o.id = c.office_id where  l.disbursedon_date between ? and ? and l.loan_officer_id = ?";	
	private static final String Loan_Officer_Collected="select(SUM(ifnull(m.principal_portion_derived, 0)) + SUM(ifnull(m.interest_portion_derived, 0)) + SUM(ifnull(m.fee_charges_portion_derived, 0)) + SUM(ifnull(m.penalty_charges_portion_derived, 0))) as amount from m_loan_transaction_repayment_schedule_mapping m where m.loan_repayment_schedule_id in (select lrs.id from m_loan_repayment_schedule lrs join m_loan l on l.id = lrs.loan_id where lrs.duedate between ? and ? and l.loan_officer_id= ? )";
	private static final String View_Officer_Collection="select c.display_name AS name, l.account_no as accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName, l.principal_amount as principal from m_loan_transaction_repayment_schedule_mapping m join m_loan_transaction lt on lt.id = m.loan_transaction_id join m_loan l on l.id = lt.loan_id join m_client c on c.id = l.client_id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id left join m_office o on o.id = c.office_id where m.loan_repayment_schedule_id in (select lrs.id from m_loan_repayment_schedule lrs join m_loan l on l.id = lrs.loan_id where lrs.duedate between ? and ? and l.loan_officer_id= ? )";
	private static final String Loan_Officer_Collections="select  (SUM(ifnull(lrs.principal_amount, 0)) + SUM(ifnull(lrs.interest_amount, 0)) + SUM(ifnull(lrs.fee_charges_amount, 0)) + SUM(ifnull(lrs.penalty_charges_amount, 0))) as amount from m_loan_repayment_schedule lrs join m_loan l on l.id = lrs.loan_id where lrs.duedate between ? and ? and l.loan_officer_id= ? ";
	private static final String Loan_Officer_Delinquency="select c.display_name as displayname, lrs.duedate as duedate, (l.principal_outstanding_derived + l.interest_outstanding_derived +l.fee_charges_outstanding_derived +l.penalty_charges_outstanding_derived) as delinquentamount from m_client c join m_loan l on l.client_id = c.id JOIN  m_loan_repayment_schedule lrs on lrs.loan_id = l.id join m_loan_arrears_aging mla on mla.loan_id = l.id where DATEDIFF(CURDATE(), (mla.overdue_since_date_derived)) >= ? AND lrs.duedate BETWEEN ? AND ? AND lrs.completed_derived = 0 and l.loan_officer_id = ?";
	private static final String Loan_Performance = "SELECT sum(l.principal_amount) AS loanAmount, sum(l.principal_repaid_derived+l.interest_repaid_derived+ l.fee_charges_repaid_derived+l.penalty_charges_repaid_derived) AS paid, sum(mla.principal_overdue_derived+mla.interest_overdue_derived+ mla.fee_charges_overdue_derived+mla.penalty_charges_overdue_derived) AS arrears, sum(l.principal_outstanding_derived+l.interest_outstanding_derived+ l.fee_charges_outstanding_derived+l.penalty_charges_outstanding_derived) AS owed FROM m_client c JOIN m_loan l ON c.id = l.client_id LEFT JOIN m_loan_arrears_aging mla ON mla.loan_id = l.id WHERE l.client_id= ? and l.loan_status_id in (300,700);";
	private static final String Loan_Performance_due = "SELECT ((IFNULL(SUM(mlr.principal_amount),0)- IFNULL(SUM(mlr.principal_writtenoff_derived),0)- IFNULL(SUM(mlr.principal_completed_derived),0))+ (IFNULL(SUM(mlr.interest_amount),0)- IFNULL(SUM(mlr.interest_completed_derived),0)- IFNULL(SUM(mlr.interest_writtenoff_derived),0)- IFNULL(SUM(mlr.interest_waived_derived),0))+ (IFNULL(SUM(mlr.penalty_charges_amount),0)- IFNULL(SUM(mlr.penalty_charges_completed_derived),0)- IFNULL(SUM(mlr.penalty_charges_writtenoff_derived),0)- IFNULL(SUM(mlr.penalty_charges_waived_derived),0))+ (IFNULL(SUM(mlr.fee_charges_amount),0)- IFNULL(SUM(mlr.fee_charges_completed_derived),0)- IFNULL(SUM(mlr.fee_charges_writtenoff_derived),0)- IFNULL(SUM(mlr.fee_charges_waived_derived),0))) AS due FROM m_client c JOIN m_loan l ON c.id = l.client_id LEFT JOIN m_loan_arrears_aging mla ON mla.loan_id = l.id LEFT JOIN m_loan_repayment_schedule mlr ON mlr.loan_id = l.id WHERE l.client_id= ? AND mlr.duedate BETWEEN SUBDATE(CURDATE(), WEEKDAY(CURDATE())) AND adddate(SUBDATE(CURDATE(), WEEKDAY(CURDATE())),6);";
	private static final String Manager_Active_Loan_Summary_Details = "select c.display_name AS name, l.account_no as accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName, l.principal_amount as principal from   m_office o join m_office ounder on ounder.hierarchy like Concat(o.hierarchy, '%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id where  l.loan_status_id = 300 and o.id= ?";
	
	
	private static final String Branch_Manager_PENDING_APPROVAL_LOANS = "SELECT count(aud.id) AS count\n" + 
			"FROM m_portfolio_command_source aud\n" + 
			"LEFT JOIN m_office o ON o.id = aud.office_id\n" + 
			"LEFT JOIN m_client c ON c.id = aud.client_id\n" + 
			"LEFT JOIN m_loan l ON l.id = aud.loan_id\n" + 
			"LEFT JOIN m_code_value mc ON mc.id = c.gender_cv_id\n" + 
			"LEFT JOIN m_staff ms ON ms.id = c.staff_id\n" + 
			"WHERE aud.processing_result_enum = 2 AND aud.action_name=\"CREATE\" AND aud.entity_name=\"LOAN\" AND aud.client_id IS NOT NULL AND c.office_id = ?\n" + 
			"";
	
	private static final String View_Branch_Manager_PENDING_APPROVAL_LOANS = "SELECT aud.id AS id, c.display_name AS name, c.account_no AS accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName FROM m_portfolio_command_source aud LEFT JOIN m_office o ON o.id = aud.office_id LEFT JOIN m_client c ON c.id = aud.client_id LEFT JOIN m_loan l ON l.id = aud.loan_id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = c.staff_id WHERE aud.processing_result_enum = 2 AND aud.action_name=\"CREATE\" AND aud.entity_name=\"LOAN\" AND aud.client_id is NOT null and c.office_id = ? GROUP BY aud.id ORDER BY aud.id ";
	private static final String View_Officer_PENDING_APPROVAL_LOANS = "SELECT aud.id AS id, c.display_name AS name, c.account_no AS accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName FROM m_portfolio_command_source aud LEFT JOIN m_office o ON o.id = aud.office_id LEFT JOIN m_client c ON c.id = aud.client_id LEFT JOIN m_loan l ON l.id = aud.loan_id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = c.staff_id WHERE aud.processing_result_enum = 2 AND aud.action_name=\"CREATE\" AND aud.entity_name=\"LOAN\" AND aud.client_id is NOT null and c.staff_id = ? GROUP BY aud.id ORDER BY aud.id ";
	/*
	 * private static final String PENDING_APPROVAL_LOANS =
	 * "select count(l.id) as count \n" + "from   m_client c \n" +
	 * "       join m_loan l on l.client_id = c.id \n" +
	 * "where  l.loan_status_id = 100 and c.office_id IN (SELECT ounder.id FROM m_office o\n"
	 * + "join m_office ounder\n" +
	 * "on ounder.hierarchy like Concat(o.hierarchy, '%')\n" + "WHERE o.id= ? )";
	 */
	
	/*
	 * private static final String VIEW_PENDING_APPROVAL_DETAILS_GENERAL
	 * ="SELECT c.display_name AS name,l.account_no as accNo,c.mobile_no AS mobNo,\n"
	 * +
	 * "mc.code_value AS gender,mo.name AS office,ms.display_name AS officerName\n"
	 * + "from   m_client c \n" + "join m_loan l on l.client_id = c.id \n" +
	 * "left JOIN m_code_value mc ON mc.id = c.gender_cv_id\n" +
	 * "left JOIN m_office mo on mo.id = c.office_id\n" +
	 * "LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id\n" +
	 * "where  l.loan_status_id = 100";
	 */
	private static final String VIEW_PENDING_APPROVAL_DETAILS_GENERAL ="SELECT aud.id AS id,c.display_name AS name, c.account_no AS accNo,\n" + 
			"c.mobile_no AS mobNo,mc.code_value AS gender,o.name AS office,ms.display_name AS officerName\n" + 
			"FROM m_portfolio_command_source aud\n" + 
			"LEFT JOIN m_office o ON o.id = aud.office_id\n" + 
			"LEFT JOIN m_client c ON c.id = aud.client_id\n" + 
			"LEFT JOIN m_loan l ON l.id = aud.loan_id\n" + 
			"left JOIN m_code_value mc ON mc.id = c.gender_cv_id\n" + 
			"LEFT JOIN m_staff ms ON ms.id = c.staff_id\n" + 
			"\n" + 
			"WHERE aud.processing_result_enum = 2\n" + 
			"AND aud.action_name=\"CREATE\"\n" + 
			"AND aud.entity_name=\"LOAN\"\n" + 
			"AND aud.client_id is NOT null\n" + 
			"GROUP BY aud.id\n" + 
			"ORDER BY aud.id";
	
	private static final String VIEW_END_OF_MONTH_CLOSING = "select c.display_name AS name, l.account_no as accNo, c.mobile_no AS mobNo, mc.code_value AS gender, o.name AS office, ms.display_name AS officerName , lrs.id as installmentid from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_loan_repayment_schedule lrs on lrs.loan_id = l.id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id where   lrs.installment = (select max(lrs1.installment) from m_loan_repayment_schedule lrs1 where lrs1.loan_id = lrs.loan_id) and lrs.duedate between (SELECT DATE_SUB(LAST_DAY(NOW()),INTERVAL DAY(LAST_DAY(NOW()))- 1 DAY)) and (select last_day(now())) and l.loan_status_id = 300 AND o.id = ?";
	private static final String END_OF_MONTH_CLOSING = "select  count(l.id) as count from m_office o JOIN m_office ounder on ounder.hierarchy like concat(o.hierarchy,'%') join m_client c on c.office_id = ounder.id join m_loan l on l.client_id = c.id join m_loan_repayment_schedule lrs on lrs.loan_id = l.id left JOIN m_code_value mc ON mc.id = c.gender_cv_id LEFT JOIN m_staff ms ON ms.id = l.loan_officer_id where   lrs.installment = (select max(lrs1.installment) from m_loan_repayment_schedule lrs1 where lrs1.loan_id = lrs.loan_id) and lrs.duedate between (SELECT DATE_SUB(LAST_DAY(NOW()),INTERVAL DAY(LAST_DAY(NOW()))- 1 DAY)) and (select last_day(now())) and l.loan_status_id = 300 AND o.id = ?";
	private static final String ACTIVE_LOANS_SUMMARY = "select count(l.id) as count \n" + 
			"			from   m_office o\n" + 
			"			       join m_office ounder \n" + 
			"			         on ounder.hierarchy like Concat(o.hierarchy, '%') \n" + 
			"			       join m_client c \n" + 
			"			         on c.office_id = ounder.id\n" + 
			"			       join m_loan l\n" + 
			"			         on l.client_id = c.id\n" + 
			"			where  l.loan_status_id = 300\n" + 
			"			        AND o.id=?";
	
	@Autowired
	public DashboardReadPlatformServiceImpl(final RoutingDataSource dataSource,
			final PlatformSecurityContext context,final GlobalConfigurationRepositoryWrapper globalConfigurationRepository) {
		
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.context = context;
		this.globalConfigurationRepository = globalConfigurationRepository;
	}

	@Override
	public JSONObject generalManagerLandingRetrieveDashboardTemplate(String chartType,DateParam from,DateParam to,Long productId, Long officeId) {

		List<PieChartData> DataList = new ArrayList<PieChartData>();
		List<DurationChartData> DurationDataList = new ArrayList<DurationChartData>();
		List<WeeklyChartData> WeeklyDataList = new ArrayList<WeeklyChartData>();
		List<StackedChartData> StackedDataList = new ArrayList<StackedChartData>();
		JSONObject jsonObject = new JSONObject();

		
		if(chartType.equalsIgnoreCase("activeCustomer")) {
			DataList = retrieveActiveCustomersForLanding(from,to).stream()
					.collect(Collectors.toList());
			String[] categories = new String[DataList.size()];
			BigDecimal[] series = new BigDecimal[DataList.size()];
			/*
			 * JSONObject chart = new JSONObject(); chart.put("width", "100%");
			 * chart.put("type", "pie");
			 */
			for(int i=0;i<DataList.size();i++) {
				categories[i] = DataList.get(i).getXaxis();
				series[i] = DataList.get(i).getYaxis();
			}			
			jsonObject.put("series", series);
			jsonObject.put("labels", categories);
			//jsonObject.put("chart", chart);
		}
		
		if(chartType.equalsIgnoreCase("PARPerBranch")) {
			DataList = retrievePARPerBranchForLanding(from,to).stream()
					.collect(Collectors.toList());
			String[] categories = new String[DataList.size()];
			BigDecimal[] series = new BigDecimal[DataList.size()];
			/*
			 * JSONObject chart = new JSONObject(); chart.put("width", "100%");
			 * chart.put("type", "pie");
			 */
			for(int i=0;i<DataList.size();i++) {
				categories[i] = DataList.get(i).getXaxis();
				series[i] = DataList.get(i).getYaxis();
			}			
			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
			//jsonObject.put("chart", chart);
		}
		
		if(chartType.equalsIgnoreCase("LoanInBadStanding")) {
			DurationDataList = retrieveLoanInBadStandingForLanding(from,to).stream()
					.collect(Collectors.toList());
			String[] categories = {"current","1-30 days","30-60 days","60-90 days","90+"};
			BigDecimal[] series = {DurationDataList.get(0).getCurrent(),DurationDataList.get(0).getPAR1(),DurationDataList.get(0).getPAR31()
					,DurationDataList.get(0).getPAR61(),DurationDataList.get(0).getPAR91()};
		
			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
		}
		
		if(chartType.equalsIgnoreCase("SavingsBalancePerBranch")) {
			DurationDataList = retrieveSavingsBalancePerBranchForLanding(from,to,productId).stream()
					.collect(Collectors.toList());
			if(productId==null) {
				productId = DurationDataList.get(0).getProductId();
			}
			String[] categories = {"current","1-30 days","30-60 days","60-90 days","90+"};

			
			BigDecimal[] series = new BigDecimal[5]; 
			if(DurationDataList.size() > 0) {
				BigDecimal[] series1 = {DurationDataList.get(0).getCurrent(),DurationDataList.get(0).getPAR1(),DurationDataList.get(0).getPAR31()
						,DurationDataList.get(0).getPAR61(),DurationDataList.get(0).getPAR91()};
				series = series1;
			}
			
		
			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
			jsonObject.put("productId", productId);

		}
		
		if(chartType.equalsIgnoreCase("CustomerNumbersBranch")) {
			StackedDataList = retrieveCustomersNumberBranchForLanding(from,to,officeId).stream()
					.collect(Collectors.toList());
			if(officeId==null) {
				officeId = 1L;
			}
			String[] categories = {"ActiveCustomers","NonActiveCustomer","Prospects"};
			BigDecimal[] series = {StackedDataList.get(0).getActiveCustomers(),StackedDataList.get(0).getNonActiveCustomer(),StackedDataList.get(0).getNonFundedCustomer()};

			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
			jsonObject.put("officeId", officeId);

		}
		
		if(chartType.equalsIgnoreCase("LoansToBePaidInCurrentWeek")) {
			WeeklyDataList = retrieveLoansToBePaidInCurrentWeekForLanding(from,to).stream()
					.collect(Collectors.toList());
			String[] categories = new String[7];
			for(int i=1;i<8;i++) {
				Date currentDate = new Date();
		        Calendar c = Calendar.getInstance();
		        c.add(Calendar.DATE, i);
		        Date currentDatePlus = c.getTime();

			    Format dateFormat = new SimpleDateFormat("EEE, MMM dd");
			    String res = dateFormat.format(currentDatePlus);
			    categories[i-1] = res;
			}
						
			BigDecimal[] series = {WeeklyDataList.get(0).getDay1(),WeeklyDataList.get(0).getDay2(),WeeklyDataList.get(0).getDay3()
					,WeeklyDataList.get(0).getDay4(),WeeklyDataList.get(0).getDay5(),WeeklyDataList.get(0).getDay6(),WeeklyDataList.get(0).getDay7()};
		
			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
		}
		
		if(chartType.equalsIgnoreCase("todolist")) {
			List<ToDoListData> noOfCenters = new ArrayList<ToDoListData>();
			noOfCenters = retrieveNoOfCentersForLanding().stream()
					.collect(Collectors.toList());
			
			List<ToDoListData> noOfGroups = new ArrayList<ToDoListData>();
			noOfGroups = retrieveNoOfGroupsForLanding().stream()
					.collect(Collectors.toList());
			

			List<ToDoListData> noOfBorrowers = new ArrayList<ToDoListData>();
			noOfBorrowers = retrieveNoOfBorrowersForLanding().stream()
					.collect(Collectors.toList());
			
			jsonObject.put("noOfCenters",noOfCenters.get(0).getSeries());
			jsonObject.put("noOfGroups",noOfGroups.get(0).getSeries());
			jsonObject.put("noOfBorrowers",noOfBorrowers.get(0).getSeries());
			
		}
				
		return jsonObject;
	}


	@Override
	public String generalManagerretrieveChartByFilter(InputData input, UriInfo uriInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject branchManagerLandingRetrieveDashboardTemplate(String chartType,DateParam from,DateParam to) {
		AppUser user = this.context.authenticatedUser();
		Long branchId = user.getOffice().getId();
		JSONObject jsonObject = new JSONObject();
		
		if(chartType.equalsIgnoreCase("collection")) {
	        if (from != null && to != null) {
	        	Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
	        	Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
	        	
	        	
	        	long difference_In_Time = fromDate.getTime() - toDate.getTime(); 
	        		 
	        	int difference_In_Days = (int) ((difference_In_Time / (1000 * 60 * 60 * 24))% 365); 
	        	
				Date previousfrom = DateUtils.addDays(fromDate,difference_In_Days);
				Date previousto = DateUtils.addDays(toDate,difference_In_Days);
	        
			List<BranchManagercollectionsData> collectedAmount = new ArrayList<BranchManagercollectionsData>();
			collectedAmount = retrieveBranchManagercollectionsForLanding(fromDate,toDate,branchId).stream()
					.collect(Collectors.toList());
			

			List<BranchManagercollectionsData> totalAmount = new ArrayList<BranchManagercollectionsData>();
			totalAmount = retrieveBranchManagercollectionsTotalAmountForLanding(fromDate,toDate,branchId).stream()
					.collect(Collectors.toList());

			BigDecimal collected = collectedAmount.get(0).getAmount();
			BigDecimal total = totalAmount.get(0).getAmount();
			BigDecimal result;
			if(collected.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(total.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				result = collected.divide(total, 2, RoundingMode.HALF_UP);
				result = result.multiply(new BigDecimal(100));
			}
				//result = collected.divide(total).multiply(new BigDecimal(100));
			
			List list=new ArrayList();
			list.add(result);
			jsonObject.put("series", list);
			jsonObject.put("collectedAmount",collectedAmount.get(0).getAmount());
			jsonObject.put("totalAmount",totalAmount.get(0).getAmount());
	        }
		}
		if(chartType.equalsIgnoreCase("Portfolio")) {

	        if (from != null && to != null) {
				Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
				Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
				//Date previousfrom = DateUtils.addDays(fromDate,-31);
				//Date previousto = DateUtils.addDays(toDate,-1);
				long difference_In_Time = fromDate.getTime() - toDate.getTime(); 
       		 
	        	int difference_In_Days = (int) ((difference_In_Time / (1000 * 60 * 60 * 24))% 365); 
	        	
				Date previousfrom = DateUtils.addDays(fromDate,difference_In_Days);
				Date previousto = DateUtils.addDays(toDate,difference_In_Days);
			List<BranchManagerPortfolioData> currentMonth = new ArrayList<BranchManagerPortfolioData>();
			currentMonth = retrieveBranchManagerPortfolioForLanding(fromDate,toDate,branchId).stream()
					.collect(Collectors.toList());
			

			
			List<BranchManagerPortfolioData> previousMonth = new ArrayList<BranchManagerPortfolioData>();
			previousMonth = retrieveBranchManagerPortfolioForLanding(previousfrom,previousto,branchId).stream()
					.collect(Collectors.toList());
			
			
			BigDecimal current = currentMonth.get(0).getAmount();
			BigDecimal previous = previousMonth.get(0).getAmount();
			BigDecimal result;
			if(current.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(previous.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				BigDecimal diff = current.subtract(previous);
				result = diff.divide(previous, 2, RoundingMode.HALF_UP);
			    result = result.multiply(new BigDecimal(100));
			}
				//result = current.divide(previous).multiply(new BigDecimal(100));
			BigDecimal diffAmount = current.subtract(previous);
			List list=new ArrayList();
			list.add(result);
			list.add(diffAmount);
			jsonObject.put("series", list);
			jsonObject.put("currentMonth", currentMonth.get(0).getAmount());
			jsonObject.put("previousMonth", previousMonth.get(0).getAmount());
	        }
		}
		if(chartType.equalsIgnoreCase("Delinquency")) {
			final GlobalConfigurationProperty property = this.globalConfigurationRepository
	        		.findOneByNameWithNotFoundDetection("Delinquent_Days");
			Long delinquentdays;
			if(property.isEnabled())
				delinquentdays = property.getValue();
			else
				delinquentdays = 0L;
			LocalDate toDate = LocalDate.now(); 
			int y = toDate.getYear();
			String g = y+"-01-01";
			Date newfromdate = null;
		    try {
				newfromdate=new SimpleDateFormat("yyyy-MM-dd").parse(g);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  

			LocalDate fromDate = LocalDate.parse(g);
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		    Date newtodate = new Date(); 
		    
			//LocalDate fromDate = new LocalDate("yyyy-MM-dd").parse(String.valueOf(new java.util.Date().getYear())+"-01-01");
			List<BranchManagerDelinquencyData> retrieveBranchManagerDelinquency = new ArrayList<BranchManagerDelinquencyData>();
			retrieveBranchManagerDelinquency = retrieveBranchManagerDelinquencyForLanding(newfromdate,newtodate,branchId,delinquentdays).stream()
					.collect(Collectors.toList());
			
			//String[] categories = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
			
			String[] months = new DateFormatSymbols().getMonths();
			
			List<String> categories = new ArrayList();
			for(int j=0;j<=toDate.getMonthOfYear()-1;j++) {
				categories.add(j,months[j]);
			}
			
			List<BigDecimal> series = new ArrayList();
			for(int i=0;i<=toDate.getMonthOfYear()-1;i++) {
				series.add(i, new BigDecimal(0));
			}
			
			Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
			for (BranchManagerDelinquencyData  retrieveData : retrieveBranchManagerDelinquency) {
				BigDecimal amount = retrieveData.getDelinquentamount();
				retrieveData.getDisplayname();
				LocalDate date = retrieveData.getDuedate();
				if(date!=null)
				{
					int month = date.getMonthOfYear();
				
					BigDecimal previousAmount = series.get(month-1);
					BigDecimal totalAmount = previousAmount.add(amount);
					series.set(month-1, totalAmount);
					//series.add(month,previousAmount.add(amount));
				}
			}

			int currentMonth = toDate.getMonthOfYear();
			BigDecimal current = series.get(currentMonth-1);
			BigDecimal previous = series.get(currentMonth-2);
			BigDecimal result;
			if(current.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(previous.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				BigDecimal diff = current.subtract(previous);
				result = diff.divide(previous, 2, RoundingMode.HALF_UP);
			    result = result.multiply(new BigDecimal(100));
			}
			
			jsonObject.put("series", series);
			jsonObject.put("categories", categories);
			jsonObject.put("map", retrieveBranchManagerDelinquency);
			jsonObject.put("result", result);
		}
		if(chartType.equalsIgnoreCase("todolist")) {
			List<ToDoListData> pendingApprovalLoans = new ArrayList<ToDoListData>();
			pendingApprovalLoans = retrieveManagerpendingApprovalLoans(branchId).stream()
					.collect(Collectors.toList());
			
			List<ToDoListData> endOfMonthClosing = new ArrayList<ToDoListData>();
			endOfMonthClosing = retrieveManagerEndOfMonthClosing(branchId).stream()
					.collect(Collectors.toList());
			

			List<ToDoListData> activeLoanSummary = new ArrayList<ToDoListData>();
			activeLoanSummary = retrieveManagerActiveLoanSummary(branchId).stream()
					.collect(Collectors.toList());
			jsonObject.put("pendingApprovalLoans",pendingApprovalLoans.get(0).getSeries());
			jsonObject.put("endOfMonthClosing",endOfMonthClosing.get(0).getSeries());
			jsonObject.put("activeLoanSummary",activeLoanSummary.get(0).getSeries());
			
		}

		return jsonObject;
	}
	@Override
	public JSONObject loanOfficerLandingLandingRetrieveDashboardTemplate(String chartType, DateParam from,
			DateParam to) {
		AppUser user = this.context.authenticatedUser();
		Long officerId = user.getStaffId();
		JSONObject jsonObject = new JSONObject();
		if(chartType.equalsIgnoreCase("Customers")) {
			
			if (from != null && to != null) {
				Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
				Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
				//Date previousfrom = DateUtils.addDays(fromDate,-31);
				//Date previousto = DateUtils.addDays(toDate,-1);
				long difference_In_Time = fromDate.getTime() - toDate.getTime(); 
       		 
	        	int difference_In_Days = (int) ((difference_In_Time / (1000 * 60 * 60 * 24))% 365); 
	        	
				Date previousfrom = DateUtils.addDays(fromDate,difference_In_Days);
				Date previousto = DateUtils.addDays(toDate,difference_In_Days);
			List<LoanOfficerCustomersData> currentCustomer = new ArrayList<LoanOfficerCustomersData>();
			currentCustomer = retrieveloanOfficerCustomersForLanding(fromDate,toDate,officerId).stream()
						.collect(Collectors.toList());			

			
			List<LoanOfficerCustomersData> previousCustomer = new ArrayList<LoanOfficerCustomersData>();
			previousCustomer = retrieveloanOfficerCustomersForLanding(previousfrom,previousto,officerId).stream()
					.collect(Collectors.toList());
			
			Long current = currentCustomer.get(0).getCount();
			Long previous = previousCustomer.get(0).getCount();
			Long result;
			if(current == 0) {
				result = (long) 0;
			}
			else if(previous == 0) {
				result  =  (long) 100;
			}
			else {
				Long diff = current-previous;
				result = (diff/previous)*100;
			}
			
			List list=new ArrayList();
			list.add(result);
			jsonObject.put("series", list);
			jsonObject.put("currentCustomer", currentCustomer.get(0).getCount());
			jsonObject.put("previousCustomer", previousCustomer.get(0).getCount());
	        }
		}
		if(chartType.equalsIgnoreCase("Collection")) {
			//List<LoanOfficerCollectionsData> loanOfficerCollectionsList = new ArrayList<LoanOfficerCollectionsData>();
			//loanOfficerCollectionsList = retrieveloanOfficerCollectionsForLanding(from,to).stream()
			//		.collect(Collectors.toList());
			
			if (from != null && to != null) {
	        	Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
	        	Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");

				//Date previousfrom = DateUtils.addDays(fromDate,-31);
				//Date previousto = DateUtils.addDays(toDate,-1);
	        	long difference_In_Time = fromDate.getTime() - toDate.getTime(); 
       		 
	        	int difference_In_Days = (int) ((difference_In_Time / (1000 * 60 * 60 * 24))% 365); 
	        	
				Date previousfrom = DateUtils.addDays(fromDate,difference_In_Days);
				Date previousto = DateUtils.addDays(toDate,difference_In_Days);
			List<LoanOfficerCollectionsData> collectedAmount = new ArrayList<LoanOfficerCollectionsData>();
			collectedAmount = retrieveloanOfficerCollectionsForLanding(fromDate,toDate,officerId).stream()
					.collect(Collectors.toList());
			

			List<LoanOfficerCollectionsData> totalAmount = new ArrayList<LoanOfficerCollectionsData>();
			totalAmount = retrieveloanOfficerCollectionsTotalAmountForLanding(fromDate,toDate,officerId).stream()
					.collect(Collectors.toList());
			
			BigDecimal collected = collectedAmount.get(0).getAmount();
			BigDecimal total = totalAmount.get(0).getAmount();
			BigDecimal result;
			if(collected.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(total.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				result = collected.divide(total, 2, RoundingMode.HALF_UP);
				result = result.multiply(new BigDecimal(100));
			}
				//result = collected.divide(total).multiply(new BigDecimal(100));
			
			
			List list=new ArrayList();
			list.add(result);
			jsonObject.put("series", list);
			jsonObject.put("collectedAmount", collectedAmount.get(0).getAmount());
			jsonObject.put("totalAmount", totalAmount.get(0).getAmount());
			
	        }
			
		}
		if(chartType.equalsIgnoreCase("Portfolio")) {
			//List<LoanOfficerPortfolioData> loanOfficerPortfolioList = new ArrayList<LoanOfficerPortfolioData>();
			//loanOfficerPortfolioList = retrieveloanOfficerPortfolioForLanding(from,to).stream()
			//		.collect(Collectors.toList());
			if (from != null && to != null) {
				Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
				Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
				//Date previousfrom = DateUtils.addDays(fromDate,-31);
				//Date previousto = DateUtils.addDays(toDate,-1);
				long difference_In_Time = fromDate.getTime() - toDate.getTime(); 
       		 
	        	int difference_In_Days = (int) ((difference_In_Time / (1000 * 60 * 60 * 24))% 365); 
	        	
				Date previousfrom = DateUtils.addDays(fromDate,difference_In_Days);
				Date previousto = DateUtils.addDays(toDate,difference_In_Days);
			List<LoanOfficerPortfolioData> currentMonth = new ArrayList<LoanOfficerPortfolioData>();
			currentMonth = retrieveloanOfficerPortfolioForLanding(fromDate,toDate,officerId).stream()
					.collect(Collectors.toList());
			

			
			List<LoanOfficerPortfolioData> previousMonth = new ArrayList<LoanOfficerPortfolioData>();
			previousMonth = retrieveloanOfficerPortfolioForLanding(previousfrom,previousto,officerId).stream()
					.collect(Collectors.toList());
			
			

			BigDecimal current = currentMonth.get(0).getAmount();
			BigDecimal previous = previousMonth.get(0).getAmount();
			BigDecimal result;
			if(current.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(previous.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				BigDecimal diff = current.subtract(previous);
				result = diff.divide(previous, 2, RoundingMode.HALF_UP);
			    result = result.multiply(new BigDecimal(100));
			}
			BigDecimal diffAmount = current.subtract(previous);
			List list=new ArrayList();
			list.add(result);
			list.add(diffAmount);
			jsonObject.put("series", list);
			jsonObject.put("currentMonth", currentMonth.get(0).getAmount());
			jsonObject.put("previousMonth", previousMonth.get(0).getAmount());
	        }
		}

		if(chartType.equalsIgnoreCase("Delinquency")) {
			//if (from != null && to != null) {
				//Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
				//Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
			final GlobalConfigurationProperty property = this.globalConfigurationRepository
	        		.findOneByNameWithNotFoundDetection("Delinquent_Days");
			Long delinquentdays;
			if(property.isEnabled())
				delinquentdays = property.getValue();
			else
				delinquentdays = 0L;
			LocalDate toDate = LocalDate.now(); 
			int y = toDate.getYear();
			String g = y+"-01-01";
			Date newfromdate = null;
		    try {
				newfromdate=new SimpleDateFormat("yyyy-MM-dd").parse(g);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  

			LocalDate fromDate = LocalDate.parse(g);
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		    Date newtodate = new Date(); 
			List<LoanOfficerDelinquencyData> loanOfficerDelinquencyList = new ArrayList<LoanOfficerDelinquencyData>();
			loanOfficerDelinquencyList = retrieveloanOfficerDelinquencyForLanding(newfromdate,newtodate,officerId,delinquentdays).stream()
					.collect(Collectors.toList());
			
			List<BigDecimal> series = new ArrayList();
			for(int i=0;i<=toDate.getMonthOfYear()-1;i++) {
				series.add(i, new BigDecimal(0));
			}
			
			Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
			for (LoanOfficerDelinquencyData  retrieveData : loanOfficerDelinquencyList) {
				BigDecimal amount = retrieveData.getDelinquentamount();
				retrieveData.getDisplayname();
				LocalDate date = retrieveData.getDuedate();
				if(date!=null)
				{
					int month = date.getMonthOfYear();
				
					BigDecimal previousAmount = series.get(month-1);
					BigDecimal totalAmount = previousAmount.add(amount);
					series.set(month-1, totalAmount);
					//series.add(month,previousAmount.add(amount));
				}
			}

			int currentMonth = toDate.getMonthOfYear();
			BigDecimal current = series.get(currentMonth-1);
			BigDecimal previous = series.get(currentMonth-2);
			BigDecimal result;
			if(current.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(0);
			}
			else if(previous.compareTo(BigDecimal.ZERO) == 0) {
				result  = new BigDecimal(100);
			}
			else {
				BigDecimal diff = current.subtract(previous);
				result = diff.divide(previous, 2, RoundingMode.HALF_UP);
			    result = result.multiply(new BigDecimal(100));
			}
			
			
			jsonObject.put("map", loanOfficerDelinquencyList);
			jsonObject.put("result", result);
			//}
			}
		
		return jsonObject;
	}
	@Override
	public String branchManagerretrieveChartByFilter(InputData input, UriInfo uriInfo) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public Collection<PieChartData> retrieveActiveCustomersForLanding(final DateParam from,final DateParam to) {
		final PieChartMapper mapper = new PieChartMapper();
		String sql = ACTIVE_CUSTOMERS;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<PieChartData> retrievePARPerBranchForLanding(final DateParam from,final DateParam to) {
		final PieChartMapper mapper = new PieChartMapper();
		String sql = PAR_PER_BRANCH;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<DurationChartData> retrieveLoanInBadStandingForLanding(final DateParam from,final DateParam to) {
		final DurationChartMapper mapper = new DurationChartMapper();
		String sql = LOAN_IN_BAD_STANDING;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<DurationChartData> retrieveSavingsBalancePerBranchForLanding(final DateParam from,final DateParam to,final Long productId) {
		final DurationChartMapper mapper = new DurationChartMapper();
		String sql = SAVINGS_BALANCE_PER_BRANCH;
		if (productId == null) {
			sql = sql.replaceAll("PRODUCT_CONDITION", "");
			return this.jdbcTemplate.query(sql, mapper);
		} else {
		sql = sql.replaceAll("PRODUCT_CONDITION", "AND ms.product_id =? ");
		return this.jdbcTemplate.query(sql, mapper, productId);
		}
		
	}
	public List<StackedChartData> retrieveCustomersNumberBranchForLanding(final DateParam from,final DateParam to,final Long officeId) {
		final StackedChartMapper mapper = new StackedChartMapper();
		String sql = CUSTOMERS_NUMBER_BRANCH;
		if(officeId == null) {
			sql = sql.replaceAll("OFFICE_ID", "1");
			return this.jdbcTemplate.query(sql, mapper);
		} else {
			sql = sql.replaceAll("OFFICE_ID", "?");
			return this.jdbcTemplate.query(sql, mapper, officeId,officeId);
		}
		
	}
	
	public Collection<WeeklyChartData> retrieveLoansToBePaidInCurrentWeekForLanding(final DateParam from,final DateParam to) {
		final WeeklyChartMapper mapper = new WeeklyChartMapper();
		String sql = LOANS_TO_BE_PAID_IN_CURRENT_WEEK;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<ToDoListData> retrieveNoOfCentersForLanding() {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = NO_OF_CENTERS;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<ToDoListData> retrieveNoOfGroupsForLanding() {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = NO_OF_GROUPS;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public Collection<ToDoListData> retrieveNoOfBorrowersForLanding() {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = NO_OF_BORROWERS;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	private static final class PieChartMapper implements RowMapper<PieChartData> {

		@Override
		public PieChartData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String categories = rs.getString("categories"); 
			final BigDecimal series = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "series");
			return new PieChartData(categories, series);
		}
	}
	
	private static final class StackedChartMapper implements RowMapper<StackedChartData> {

		@Override
		public StackedChartData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal ActiveCustomer = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "ActiveCustomer");
			final BigDecimal NonActiveCustomer = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "NonActiveCustomer");
			final BigDecimal NonFundedCustomer = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "NonFundedCustomer");
			return new StackedChartData(ActiveCustomer , NonActiveCustomer, NonFundedCustomer);
		}
	}
	
	private static final class DurationChartMapper implements RowMapper<DurationChartData> {

		@Override
		public DurationChartData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal current = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "CurrentAmount");
			final BigDecimal PAR1 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "PAR1Amount");
			final BigDecimal PAR31 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "PAR31Amount");
			final BigDecimal PAR61 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "PAR61Amount");
			final BigDecimal PAR91 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "PAR91Amount");
			final Long productId = rs.getLong("productId");
			return new DurationChartData(current, PAR1, PAR31, PAR61, PAR91, productId);
		}
	}
	
	private static final class WeeklyChartMapper implements RowMapper<WeeklyChartData> {

		@Override
		public WeeklyChartData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal Day1 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day1");
			final BigDecimal Day2 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day2");
			final BigDecimal Day3 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day3");
			final BigDecimal Day4 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day4");
			final BigDecimal Day5 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day5");
			final BigDecimal Day6 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day6");
			final BigDecimal Day7 = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "Day7");
			return new WeeklyChartData(Day1, Day2, Day3, Day4, Day5, Day6, Day7);
		}
	}

	public Collection<BranchManagercollectionsData> retrieveBranchManagercollectionsForLanding(final Date from,final Date to,Long branchId) {
		final BranchManagercollectionsMapper mapper = new BranchManagercollectionsMapper();
		String sql = Branch_Manager_collected;
		return this.jdbcTemplate.query(sql, mapper,from,to,branchId);
	}
	public Collection<BranchManagercollectionsData> retrieveBranchManagercollectionsTotalAmountForLanding(final Date from,final Date to,Long branchId) {
		final BranchManagercollectionsMapper mapper = new BranchManagercollectionsMapper();
		String sql = Branch_Manager_collections;
		return this.jdbcTemplate.query(sql, mapper,from,to,branchId);
	}
	
	public Collection<BranchManagerPortfolioData> retrieveBranchManagerPortfolioForLanding(final Date from,final Date to,Long branchId) {
		final BranchManagerPortfoliosMapper mapper = new BranchManagerPortfoliosMapper();
		String sql = Branch_Manager_Portfolio;
		return this.jdbcTemplate.query(sql, mapper,from,to,branchId);
	}
	public Collection<BranchManagerDelinquencyData> retrieveBranchManagerDelinquencyForLanding(final Date fromDate,final Date toDate,final Long branchId,final Long delinquentdays) {
		final BranchManagerDelinquencyMapper mapper = new BranchManagerDelinquencyMapper();
		String sql = Branch_Manager_Delinquency;
		return this.jdbcTemplate.query(sql, mapper,delinquentdays,fromDate,toDate,branchId);
	}
	
	
	public Collection<LoanOfficerCustomersData> retrieveloanOfficerCustomersForLanding(final Date from,final Date to, Long officerId) {
		final LoanOfficerCustomersMapper mapper = new LoanOfficerCustomersMapper();
		String sql = Loan_Officer_Customers;
		return this.jdbcTemplate.query(sql, mapper,from,to,officerId);
	}
	public Collection<LoanOfficerCollectionsData> retrieveloanOfficerCollectionsForLanding(final Date from,final Date to,final Long officerId) {
		final LoanOfficerCollectionsMapper mapper = new LoanOfficerCollectionsMapper();
		String sql = Loan_Officer_Collected;
		return this.jdbcTemplate.query(sql, mapper,from,to,officerId);
	}
	public Collection<LoanOfficerCollectionsData> retrieveloanOfficerCollectionsTotalAmountForLanding(final Date from,final Date to,final Long officerId) {
		final LoanOfficerCollectionsMapper mapper = new LoanOfficerCollectionsMapper();
		String sql = Loan_Officer_Collections;
		return this.jdbcTemplate.query(sql, mapper,from,to,officerId);
	}
	public Collection<LoanOfficerDelinquencyData> retrieveloanOfficerDelinquencyForLanding(final Date from,final Date to,final Long officerId,final Long delinquentdays) {
		final LoanOfficerDelinquencyMapper mapper = new LoanOfficerDelinquencyMapper();
		String sql = Loan_Officer_Delinquency;
		return this.jdbcTemplate.query(sql, mapper,delinquentdays,from,to,officerId);
	}

	public Collection<LoanOfficerPortfolioData> retrieveloanOfficerPortfolioForLanding(final Date from,final Date to,final Long officerId) {
		final LoanOfficerPortfolioMapper mapper = new LoanOfficerPortfolioMapper();
		String sql = Loan_Officer_Portfolio;
		return this.jdbcTemplate.query(sql, mapper,from,to,officerId);
	}
	
	public Collection<ToDoListData> retrieveManagerpendingApprovalLoans(final Long branchId) {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = Branch_Manager_PENDING_APPROVAL_LOANS;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	public Collection<ToDoListData> retrieveManagerEndOfMonthClosing(final Long branchId) {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = END_OF_MONTH_CLOSING;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	public Collection<ToDoListData> retrieveManagerActiveLoanSummary(final Long branchId) {
		final ToDoListMapper mapper = new ToDoListMapper();
		String sql = ACTIVE_LOANS_SUMMARY;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}

	
	private static final class BranchManagerDelinquencyMapper implements RowMapper<BranchManagerDelinquencyData> {

		@Override
		public BranchManagerDelinquencyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String displayname = rs.getString("displayname"); 
			final BigDecimal delinquentamount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "delinquentamount");
			//final Date duedate = rs.getDate("duedate");
			final org.joda.time.LocalDate duedate = JdbcSupport.getLocalDate(rs, "duedate");
			return new BranchManagerDelinquencyData(displayname, delinquentamount,duedate);
		}
	}

	private static final class LoanOfficerDelinquencyMapper implements RowMapper<LoanOfficerDelinquencyData> {

		@Override
		public LoanOfficerDelinquencyData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String displayname = rs.getString("displayname"); 
			final BigDecimal delinquentamount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "delinquentamount");
			final org.joda.time.LocalDate duedate = JdbcSupport.getLocalDate(rs, "duedate");
			return new LoanOfficerDelinquencyData(displayname, delinquentamount,duedate);
		}
	}
	
	private static final class ToDoListMapper implements RowMapper<ToDoListData> {

		@Override
		public ToDoListData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long count = JdbcSupport.getLong(rs, "count");
			return new ToDoListData(count);
		}
	}
	private static final class BranchManagercollectionsMapper implements RowMapper<BranchManagercollectionsData> {

		@Override
		public BranchManagercollectionsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
			return new BranchManagercollectionsData(amount);
		}
	}
	private static final class BranchManagerPortfoliosMapper implements RowMapper<BranchManagerPortfolioData> {

		@Override
		public BranchManagerPortfolioData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
			return new BranchManagerPortfolioData(amount);
		}
	}

	private static final class LoanOfficerCustomersMapper implements RowMapper<LoanOfficerCustomersData> {

		@Override
		public LoanOfficerCustomersData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long count = JdbcSupport.getLong(rs, "count");
			return new LoanOfficerCustomersData(count);
		}
	}
	
	private static final class LoanOfficerCollectionsMapper implements RowMapper<LoanOfficerCollectionsData> {

		@Override
		public LoanOfficerCollectionsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
			return new LoanOfficerCollectionsData(amount);
		}
	}

	private static final class LoanOfficerPortfolioMapper implements RowMapper<LoanOfficerPortfolioData> {

		@Override
		public LoanOfficerPortfolioData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
			return new LoanOfficerPortfolioData(amount);
		}
	}
		
	
	
	
	
	
	
	
	
	@Override
	public String loanOfficerretrieveChartByFilter(InputData input, UriInfo uriInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ViewDetailsData retrieveviewdetails(String ViewDetailsName, DateParam from, DateParam to) {
		AppUser user = this.context.authenticatedUser();
		Long branchId = user.getOffice().getId();
		Long officerId = user.getStaffId();
		List<PendingApprovalLoansData> pendingApprovalLoansData = new ArrayList<PendingApprovalLoansData>();
		List<PendingApprovalLoansData> pendingApprovalLoansDataGeneral = new ArrayList<PendingApprovalLoansData>();
		List<ManagerActiveLoanSummaryData> ManagerActiveLoanSummaryData = new ArrayList<ManagerActiveLoanSummaryData>();
		List<ViewManagerPendingApprovalLoansData> viewManagerPendingApprovalLoansData = new ArrayList<ViewManagerPendingApprovalLoansData>();
		List<ViewManagerEndOfMonthClosingLoansData> viewManagerEndOfMonthClosingLoansData = new ArrayList<ViewManagerEndOfMonthClosingLoansData>();
		List<ViewManagerCollectionData> viewManagerCollectionData = new ArrayList<ViewManagerCollectionData>();
		List<ViewManagerPortfolioData> viewManagerPortfolioData = new ArrayList<ViewManagerPortfolioData>();
		List<ViewOfficerPortfolioData> viewOfficerPortfolioData = new ArrayList<ViewOfficerPortfolioData>();
		List<ViewOfficerCollectionData> viewOfficerCollectionData = new ArrayList<ViewOfficerCollectionData>();
		List<ViewOfficerCustomersData> viewOfficerCustomersData = new ArrayList<ViewOfficerCustomersData>();
		List<ViewOfficerPendingApprovalLoansData> viewOfficerPendingApprovalLoansData = new ArrayList<ViewOfficerPendingApprovalLoansData>();
        
		if(ViewDetailsName.equalsIgnoreCase("generalLoanApproval")) {
			pendingApprovalLoansDataGeneral = retrievependingApprovalLoansDetailsForGeneral(from,to).stream()
					.collect(Collectors.toList());
		}

		if(ViewDetailsName.equalsIgnoreCase("ManagerCustomers")) {

			pendingApprovalLoansData = retrievependingApprovalLoans(from,to).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ManagerActiveLoanSummary")) {

			ManagerActiveLoanSummaryData = retrieveManagerActiveLoanSummaryDetails(branchId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewManagerPendingApprovalLoans")) {

			viewManagerPendingApprovalLoansData = ViewManagerPendingApprovalLoansSummaryDetails(branchId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewManagerEndOfMonthClosing")) {

			viewManagerEndOfMonthClosingLoansData = ViewManagerEndOfMonthClosingSummaryDetails(branchId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewManagerCollection")) {

			viewManagerCollectionData = viewManagerCollectionDataSummaryDetails(from,to,branchId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewManagerPortfolio")) {

			viewManagerPortfolioData = viewManagerPortfolioDataSummaryDetails(from,to,branchId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewOfficerPortfolio")) {

			viewOfficerPortfolioData = viewOfficerPortfolioDataSummaryDetails(from,to,officerId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewOfficerCollection")) {

			viewOfficerCollectionData = viewOfficerCollectionDataSummaryDetails(from,to,officerId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewOfficerCustomers")) {

			viewOfficerCustomersData = viewOfficerCustomersSummaryDetails(from,to,officerId).stream()
					.collect(Collectors.toList());
		}
		if(ViewDetailsName.equalsIgnoreCase("ViewOfficerPendingApprovalLoans")) {

			viewOfficerPendingApprovalLoansData = ViewOfficerPendingApprovalLoansSummaryDetails(officerId).stream()
					.collect(Collectors.toList());
		}
		return new ViewDetailsData(pendingApprovalLoansData,ManagerActiveLoanSummaryData,viewManagerPendingApprovalLoansData
				,viewManagerEndOfMonthClosingLoansData,viewManagerCollectionData,viewManagerPortfolioData,
				viewOfficerPortfolioData,viewOfficerCollectionData,viewOfficerCustomersData,viewOfficerPendingApprovalLoansData,pendingApprovalLoansDataGeneral);
	}
	
	public List<PendingApprovalLoansData> retrievependingApprovalLoans(final DateParam from,final DateParam to) {
		final PendingApprovalLoansMapper mapper = new PendingApprovalLoansMapper();
		String sql = "";
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	public List<PendingApprovalLoansData> retrievependingApprovalLoansDetailsForGeneral(final DateParam from,final DateParam to) {
		final PendingApprovalLoansMapper mapper = new PendingApprovalLoansMapper();
		String sql = VIEW_PENDING_APPROVAL_DETAILS_GENERAL;
		return this.jdbcTemplate.query(sql, mapper);
	}
	
	private static final class PendingApprovalLoansMapper implements RowMapper<PendingApprovalLoansData> {

		@Override
		public PendingApprovalLoansData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final Long id = rs.getLong("id");
			final String name = rs.getString("name"); 
			final String accNo = rs.getString("accNo"); 
			final String mobNo = rs.getString("mobNo"); 
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			return new PendingApprovalLoansData(id,name, accNo, gender, mobNo, officerName, office);
		}
	}
	public List<ManagerActiveLoanSummaryData> retrieveManagerActiveLoanSummaryDetails(final Long branchId) {
		final ManagerActiveLoanSummaryDetailsMapper mapper = new ManagerActiveLoanSummaryDetailsMapper();
		String sql = Manager_Active_Loan_Summary_Details;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	private static final class ManagerActiveLoanSummaryDetailsMapper implements RowMapper<ManagerActiveLoanSummaryData> {

		@Override
		public ManagerActiveLoanSummaryData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
			return new ManagerActiveLoanSummaryData(accNo, name,mobNo,gender,office,officerName,amount);
		}
	}
	public List<ViewManagerPendingApprovalLoansData> ViewManagerPendingApprovalLoansSummaryDetails(final Long branchId) {
		final ViewManagerPendingApprovalLoansMapper mapper = new ViewManagerPendingApprovalLoansMapper();
		String sql = View_Branch_Manager_PENDING_APPROVAL_LOANS;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	private static final class ViewManagerPendingApprovalLoansMapper implements RowMapper<ViewManagerPendingApprovalLoansData> {

		@Override
		public ViewManagerPendingApprovalLoansData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String id = rs.getString("id");
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			
			return new ViewManagerPendingApprovalLoansData(id,accNo, name,mobNo,gender,office,officerName);
		}
	}
	public List<ViewOfficerPendingApprovalLoansData> ViewOfficerPendingApprovalLoansSummaryDetails(final Long branchId) {
		final ViewOfficerPendingApprovalLoansMapper mapper = new ViewOfficerPendingApprovalLoansMapper();
		String sql = View_Officer_PENDING_APPROVAL_LOANS;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	private static final class ViewOfficerPendingApprovalLoansMapper implements RowMapper<ViewOfficerPendingApprovalLoansData> {

		@Override
		public ViewOfficerPendingApprovalLoansData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String id = rs.getString("id");
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			
			return new ViewOfficerPendingApprovalLoansData(id,accNo, name,mobNo,gender,office,officerName);
		}
	}
	public List<ViewManagerEndOfMonthClosingLoansData> ViewManagerEndOfMonthClosingSummaryDetails(final Long branchId) {
		final ViewManagerEndOfMonthClosingLoansMapper mapper = new ViewManagerEndOfMonthClosingLoansMapper();
		String sql = VIEW_END_OF_MONTH_CLOSING;
		return this.jdbcTemplate.query(sql, mapper,branchId);
	}
	private static final class ViewManagerEndOfMonthClosingLoansMapper implements RowMapper<ViewManagerEndOfMonthClosingLoansData> {

		@Override
		public ViewManagerEndOfMonthClosingLoansData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			final Long installmentid = rs.getLong("installmentid");
			return new ViewManagerEndOfMonthClosingLoansData(accNo, name,mobNo,gender,office,officerName,installmentid);
		}
	}
	public List<ViewManagerCollectionData> viewManagerCollectionDataSummaryDetails(final DateParam from,final DateParam to,final Long branchId) {
		final ViewManagerCollectionMapper mapper = new ViewManagerCollectionMapper();
			String sql = View_Branch_Manager_collected;
			Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
			Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
		
		return this.jdbcTemplate.query(sql, mapper,fromDate,toDate,branchId);
		
	}
	private static final class ViewManagerCollectionMapper implements RowMapper<ViewManagerCollectionData> {

		@Override
		public ViewManagerCollectionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final String officerName = rs.getString("officerName");
			final BigDecimal total = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "total");
			final BigDecimal collected = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
			final BigDecimal due = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "due");
			return new ViewManagerCollectionData(accNo, name,officerName,total,collected,due);
		}
	}
	public List<ViewManagerPortfolioData> viewManagerPortfolioDataSummaryDetails(final DateParam from,final DateParam to,final Long branchId) {
		final ViewManagerPortfolioMapper mapper = new ViewManagerPortfolioMapper();
			String sql = View_Branch_Manager_Portfolio;
			Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
			Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
		
		return this.jdbcTemplate.query(sql, mapper,fromDate,toDate,branchId);
		
	}
	private static final class ViewManagerPortfolioMapper implements RowMapper<ViewManagerPortfolioData> {

		@Override
		public ViewManagerPortfolioData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
			return new ViewManagerPortfolioData(accNo, name,mobNo,gender,office,officerName,amount);
		}
	}
	public List<ViewOfficerPortfolioData> viewOfficerPortfolioDataSummaryDetails(final DateParam from,final DateParam to,final Long officerId) {
		final ViewOfficerPortfolioMapper mapper = new ViewOfficerPortfolioMapper();
			String sql = View_Officer_Portfolio;
			Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
			Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
		
		return this.jdbcTemplate.query(sql, mapper,fromDate,toDate,officerId);
		
	}
	private static final class ViewOfficerPortfolioMapper implements RowMapper<ViewOfficerPortfolioData> {

		@Override
		public ViewOfficerPortfolioData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
			return new ViewOfficerPortfolioData(accNo, name,mobNo,gender,office,officerName,amount);
		}
	}
	public List<ViewOfficerCollectionData> viewOfficerCollectionDataSummaryDetails(final DateParam from,final DateParam to,final Long officerId) {
		final ViewOfficerCollectionMapper mapper = new ViewOfficerCollectionMapper();
			String sql = View_Officer_Collection;
			Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
			Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
		
		return this.jdbcTemplate.query(sql, mapper,fromDate,toDate,officerId);
		
	}
	private static final class ViewOfficerCollectionMapper implements RowMapper<ViewOfficerCollectionData> {

		@Override
		public ViewOfficerCollectionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final Long mobNo = rs.getLong("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principal");
			return new ViewOfficerCollectionData(accNo, name,mobNo,gender,office,officerName,amount);
		}
	}
	public List<ViewOfficerCustomersData> viewOfficerCustomersSummaryDetails(final DateParam from,final DateParam to,final Long officerId) {
		final ViewOfficerCustomersMapper mapper = new ViewOfficerCustomersMapper();
			String sql = View_Loan_Officer_Customers;
			Date fromDate = from.getDate("fromDate", SQL_DATEFORMAT, "en");
			Date toDate = to.getDate("toDate", SQL_DATEFORMAT, "en");
		
		return this.jdbcTemplate.query(sql, mapper,fromDate,toDate,officerId);
		
	}
	private static final class ViewOfficerCustomersMapper implements RowMapper<ViewOfficerCustomersData> {

		@Override
		public ViewOfficerCustomersData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final String accNo = rs.getString("accNo"); 
			final String name = rs.getString("name"); 
			final String mobNo = rs.getString("mobNo");
			final String gender = rs.getString("gender"); 
			final String office = rs.getString("office"); 
			final String officerName = rs.getString("officerName"); 
			return new ViewOfficerCustomersData(accNo, name,mobNo,gender,office,officerName);
		}
	}
		
	@Override
	public JSONObject retrieveClientDashboardTemplate(Long clientId) {
		JSONObject jsonObject = new JSONObject();
	        
		List<LoanPerformanceData> loanPerformanceData = new ArrayList<LoanPerformanceData>();
		loanPerformanceData = retrieveLoanPerformanceForLanding(clientId).stream()
						.collect(Collectors.toList());	
		
		List<LoanPerformanceDueData> loanPerformanceDueData = new ArrayList<LoanPerformanceDueData>();
		loanPerformanceDueData = retrieveLoanPerformanceDueForLanding(clientId).stream()
						.collect(Collectors.toList());
		
		
		BigDecimal arrears = loanPerformanceData.get(0).getArrears();
		BigDecimal owed = loanPerformanceData.get(0).getOwed();
		BigDecimal paid = loanPerformanceData.get(0).getPaid();
		BigDecimal due = loanPerformanceDueData.get(0).getDue();
		BigDecimal loanAmount = loanPerformanceData.get(0).getLoanAmount();

		List series=new ArrayList();
		List categories=new ArrayList();
		
		categories.add("Paid");
		categories.add("In Arrears");
		categories.add("Balance Owed");
		categories.add("Due This Week");
		
		series.add(paid);
		series.add(arrears);
		series.add(owed);
		series.add(due);
		series.add(loanAmount);
		jsonObject.put("series", series);
		jsonObject.put("categories", categories);
	        
		return jsonObject;
	}
	public List<LoanPerformanceData> retrieveLoanPerformanceForLanding(final Long clientId) {
		final LoanPerformanceMapper mapper = new LoanPerformanceMapper();
		String sql = Loan_Performance;
		return this.jdbcTemplate.query(sql, mapper,clientId);
	}
	
	private static final class LoanPerformanceMapper implements RowMapper<LoanPerformanceData> {

		@Override
		public LoanPerformanceData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal arrears = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "arrears");
			final BigDecimal paid = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "paid");
			final BigDecimal owed = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "owed");
			//final BigDecimal due = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "due");
			final BigDecimal loanAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "loanAmount");
			return new LoanPerformanceData(arrears, paid,owed,loanAmount);
		}
	}
	
	
	
	public List<LoanPerformanceDueData> retrieveLoanPerformanceDueForLanding(final Long clientId) {
		final LoanPerformanceDueMapper mapper = new LoanPerformanceDueMapper();
		String sql = Loan_Performance_due;
		return this.jdbcTemplate.query(sql, mapper,clientId);
	}
	
	private static final class LoanPerformanceDueMapper implements RowMapper<LoanPerformanceDueData> {

		@Override
		public LoanPerformanceDueData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			final BigDecimal due = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "due");
			return new LoanPerformanceDueData(due);
		}
	}

}
