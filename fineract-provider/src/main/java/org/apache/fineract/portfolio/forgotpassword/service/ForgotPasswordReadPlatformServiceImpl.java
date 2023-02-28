package org.apache.fineract.portfolio.forgotpassword.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.portfolio.forgotpassword.domain.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordReadPlatformServiceImpl implements ForgotPasswordReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ForgotPasswordReadPlatformServiceImpl(final RoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Collection<PasswordResetToken> retrieveData() {

		RegistrationMapper rm = new RegistrationMapper();
		final String sql = "select " + rm.schema();
		return this.jdbcTemplate.query(sql, rm, new Object[] {});
	}

	private static final class RegistrationMapper implements RowMapper<PasswordResetToken> {

		@Override
		public PasswordResetToken mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			final String AccountName = rs.getString("account_name");

			return new PasswordResetToken();
		}

		public String schema() {
			return " fp.token, fp.user_id, fp.expiry_date from  request_audit_table_registration ratr ";
		}
	}

}