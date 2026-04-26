package server.core.service.impl;

import common.dto.EmployeeTypeDTO;
import common.entity.EmployeeType;
import server.core.repository.EmployeeTypeRepository;
import server.core.service.EmployeeTypeService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.List;
import java.util.stream.Collectors;

public class EmployeeTypeServiceImpl implements EmployeeTypeService {

    private final EmployeeTypeRepository employeeTypeRepository;
    private final GenericDataMapper mapper;

    public EmployeeTypeServiceImpl(EmployeeTypeRepository employeeTypeRepository, GenericDataMapper mapper) {
        this.employeeTypeRepository = employeeTypeRepository;
        this.mapper = mapper;
    }

    @Override
    public EmployeeTypeDTO getByID(String employeeTypeID) {
        if (isBlank(employeeTypeID)) {
            return null;
        }

        EmployeeType employeeType = employeeTypeRepository.findById(employeeTypeID.trim());
        if (employeeType == null) {
            return null;
        }

        return mapper.toObject(mapper.toMap(employeeType), EmployeeTypeDTO.class);
    }

    @Override
    public EmployeeTypeDTO getByTypeName(String typeName) {
        if (isBlank(typeName)) {
            return null;
        }

        EmployeeType employeeType = employeeTypeRepository.findByTypeName(typeName.trim());
        if (employeeType == null) {
            return null;
        }

        return mapper.toObject(mapper.toMap(employeeType), EmployeeTypeDTO.class);
    }

    @Override
    public List<EmployeeTypeDTO> getAll() throws Exception {
        List<EmployeeType> employeeTypes = employeeTypeRepository.findAll();

        return employeeTypes.stream()
                .map(employeeType -> mapper.toObject(mapper.toMap(employeeType), EmployeeTypeDTO.class))
                .collect(Collectors.toList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}