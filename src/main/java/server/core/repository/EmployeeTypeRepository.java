package server.core.repository;

import common.entity.EmployeeType;

import java.util.List;

public interface EmployeeTypeRepository {
    EmployeeType findById(String typeId);

    EmployeeType findByTypeName(String typeName);

    List<EmployeeType> findAll();

}