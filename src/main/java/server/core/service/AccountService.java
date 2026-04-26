package server.core.service;

import common.dto.AccountDTO;

public interface AccountService {
    boolean authentication(String username, String password);

    AccountDTO getAccountByUsername(String username);

    String sendOtpForReset(String username) throws Exception;

    boolean resetPasswordWithOtp(String username, String otp, String newPassword, String confirmPassword);

    boolean changePasswordByEmployeeID(String employeeID, String newPassword, String confirmPassword);
}