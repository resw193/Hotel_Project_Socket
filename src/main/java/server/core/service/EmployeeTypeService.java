package server.core.service;

import common.dto.EmployeeTypeDTO;

import java.util.List;

public interface EmployeeTypeService {
    EmployeeTypeDTO getByID(String employeeTypeID);

    EmployeeTypeDTO getByTypeName(String typeName);

    List<EmployeeTypeDTO> getAll() throws Exception;
}