package server.core.service.impl;

import common.dto.AccountDTO;
import server.core.repository.AccountRepository;
import server.core.service.AccountService;
import server.infrastructure.mapper.GenericDataMapper;
import other.EmailService;
import other.OtpService;

import java.security.SecureRandom;

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final GenericDataMapper mapper;

    public AccountServiceImpl(AccountRepository accountRepository, GenericDataMapper mapper) {
        this.accountRepository = accountRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean authentication(String username, String password) {
        username = normalize(username);
        password = check(password);

        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username và password không được trống.");
        }

        return accountRepository.existsByUsernameAndPassword(username, password);
    }

    @Override
    public AccountDTO getAccountByUsername(String username) {
        username = normalize(username);
        if (username.isEmpty()) {
            return null;
        }

        common.entity.Account account = accountRepository.findByUsername(username);
        if (account == null) {
            return null;
        }

        AccountDTO dto = mapper.toObject(mapper.toMap(account), AccountDTO.class);

        if (account.getEmployee() != null) {
            dto.setEmployeeId(account.getEmployee().getEmployeeId());
            dto.setEmployeeName(account.getEmployee().getFullName());
        }

        return dto;
    }

    @Override
    public String sendOtpForReset(String username) throws Exception {
        String key = normalize(username);

        common.entity.Account account = accountRepository.findByUsername(key);
        if (account == null || account.getEmployee() == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        }

        String email = check(account.getEmployee().getEmail());
        if (email.isEmpty()) {
            throw new IllegalArgumentException("Tài khoản chưa có email.");
        }

        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        OtpService.put(key, otp, 5 * 60);

        EmailService.sendOtp(email, otp);
        return email;
    }

    @Override
    public boolean resetPasswordWithOtp(String username, String otp, String newPassword, String confirmPassword) {
        String key = normalize(username);

        if (!OtpService.verifyPassword(key, check(otp))) {
            throw new IllegalArgumentException("OTP không đúng hoặc đã hết hạn.");
        }

        validatePassword(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }

        return accountRepository.updatePasswordByUsername(key, newPassword);
    }

    @Override
    public boolean changePasswordByEmployeeID(String employeeID, String newPassword, String confirmPassword) {
        if (isBlank(employeeID)) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }

        validatePassword(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp.");
        }

        return accountRepository.updatePasswordByEmployeeId(employeeID.trim(), newPassword);
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải tối thiểu 8 ký tự.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String check(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        return check(value).toLowerCase();
    }
}