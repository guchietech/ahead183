package org.apache.fineract.portfolio.forgotpassword.service;

import java.util.Collection;
import org.apache.fineract.portfolio.forgotpassword.domain.PasswordResetToken;

public interface ForgotPasswordReadPlatformService {

	Collection<PasswordResetToken> retrieveData();

}