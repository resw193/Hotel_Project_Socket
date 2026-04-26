package server.core.repository;

import common.entity.Employee;

import java.util.List;

public interface EmployeeRepository {
    Employee findById(String employeeId);

    Employee findByEmail(String email);

    List<Employee> findAll();

    List<Employee> findByTypeName(String typeName);

    boolean add(Employee employee);

    boolean updateProfile(Employee employee);

    boolean updateAvatar(String employeeId, String imgSource);

    boolean deleteById(String employeeId);
}
