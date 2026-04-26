package server.core.repository;

import common.entity.Account;

public interface AccountRepository {
    boolean existsByUsernameAndPassword(String username, String password);

    Account findByUsername(String username);

    boolean updatePasswordByUsername(String username, String newPassword);

    boolean updatePasswordByEmployeeId(String employeeId, String newPassword);
}