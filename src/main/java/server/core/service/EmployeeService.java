package server.core.service;

import common.dto.EmployeeDTO;
import common.entity.Employee;

import java.util.List;

public interface EmployeeService {
    EmployeeDTO getByID(String employeeID);
    EmployeeDTO getByUsername(String username);

    List<EmployeeDTO> getAll();
    List<EmployeeDTO> getByTypeName(String typeName);

    boolean addEmployee(EmployeeDTO employeeDTO);
    boolean updateProfile(EmployeeDTO employeeDTO);
    boolean updateAvatar(String employeeID, String imgSource);
    boolean deleteEmployee(String employeeID);
}