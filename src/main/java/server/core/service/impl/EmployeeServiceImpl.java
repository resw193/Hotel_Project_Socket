package server.core.service.impl;

import common.dto.EmployeeDTO;
import common.entity.Account;
import common.entity.Employee;
import common.entity.EmployeeType;
import server.core.repository.AccountRepository;
import server.core.repository.EmployeeRepository;
import server.core.repository.EmployeeTypeRepository;
import server.core.service.EmployeeService;
import server.core.service.IdGeneratorService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeTypeRepository employeeTypeRepository;
    private final AccountRepository accountRepository;
    private final IdGeneratorService idGeneratorService;
    private final GenericDataMapper mapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeTypeRepository employeeTypeRepository, AccountRepository accountRepository,
                               IdGeneratorService idGeneratorService, GenericDataMapper mapper) {
        this.employeeRepository = employeeRepository;
        this.employeeTypeRepository = employeeTypeRepository;
        this.accountRepository = accountRepository;
        this.idGeneratorService = idGeneratorService;
        this.mapper = mapper;
    }

    @Override
    public EmployeeDTO getByID(String employeeID) {
        if (isBlank(employeeID)) {
            return null;
        }

        Employee employee = employeeRepository.findById(employeeID.trim());
        if (employee == null) {
            return null;
        }

        EmployeeDTO dto = mapper.toObject(mapper.toMap(employee), EmployeeDTO.class);

        if (employee.getEmployeeType() != null) {
            dto.setEmployeeTypeId(employee.getEmployeeType().getTypeId());
            dto.setEmployeeTypeName(employee.getEmployeeType().getTypeName());
        }

        return dto;
    }

    @Override
    public EmployeeDTO getByUsername(String username) {
        String normalizedUsername = normalize(username);
        if (normalizedUsername.isEmpty()) {
            return null;
        }

        Account account = accountRepository.findByUsername(normalizedUsername);
        if (account == null || account.getEmployee() == null) {
            return null;
        }

        Employee employee = account.getEmployee();
        EmployeeDTO dto = mapper.toObject(mapper.toMap(employee), EmployeeDTO.class);

        if (employee.getEmployeeType() != null) {
            dto.setEmployeeTypeId(employee.getEmployeeType().getTypeId());
            dto.setEmployeeTypeName(employee.getEmployeeType().getTypeName());
        }

        return dto;
    }

    @Override
    public List<EmployeeDTO> getAll() {
        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(employee -> {
                    EmployeeDTO dto = mapper.toObject(mapper.toMap(employee), EmployeeDTO.class);

                    if (employee.getEmployeeType() != null) {
                        dto.setEmployeeTypeId(employee.getEmployeeType().getTypeId());
                        dto.setEmployeeTypeName(employee.getEmployeeType().getTypeName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDTO> getByTypeName(String typeName) {
        if (isBlank(typeName) || "Tất cả".equalsIgnoreCase(typeName.trim())) {
            return getAll();
        }

        List<Employee> employees = employeeRepository.findByTypeName(typeName.trim());

        return employees.stream()
                .map(employee -> {
                    EmployeeDTO dto = mapper.toObject(mapper.toMap(employee), EmployeeDTO.class);

                    if (employee.getEmployeeType() != null) {
                        dto.setEmployeeTypeId(employee.getEmployeeType().getTypeId());
                        dto.setEmployeeTypeName(employee.getEmployeeType().getTypeName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean addEmployee(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            throw new IllegalArgumentException("Dữ liệu nhân viên không hợp lệ.");
        }

        String email = check(employeeDTO.getEmail());
        if (!email.isEmpty() && employeeRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        String employeeTypeName = check(employeeDTO.getEmployeeTypeName());

        if (employeeTypeName.isEmpty() && !isBlank(employeeDTO.getEmployeeTypeId())) {
            EmployeeType employeeType = employeeTypeRepository.findById(employeeDTO.getEmployeeTypeId());
            if (employeeType != null) {
                employeeTypeName = employeeType.getTypeName();
                employeeDTO.setEmployeeTypeName(employeeTypeName);
            }
        }

        String generatedEmployeeId = idGeneratorService.generateEmployeeId(employeeTypeName, employeeDTO.getFullName());
        employeeDTO.setEmployeeId(generatedEmployeeId);

        Employee employee = mapper.toObject(mapper.toMap(employeeDTO), Employee.class);

        if (!isBlank(employeeDTO.getEmployeeTypeId()) || !isBlank(employeeDTO.getEmployeeTypeName())) {
            Map<String, Object> employeeTypeMap = new HashMap<>();
            employeeTypeMap.put("typeId", employeeDTO.getEmployeeTypeId());
            employeeTypeMap.put("typeName", employeeDTO.getEmployeeTypeName());

            EmployeeType employeeType = mapper.toObject(employeeTypeMap, EmployeeType.class);
            employee.setEmployeeType(employeeType);
        }

        return employeeRepository.add(employee);
    }

    @Override
    public boolean updateProfile(EmployeeDTO employeeDTO) {
        if (employeeDTO == null || isBlank(employeeDTO.getEmployeeId())) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }

        Employee employee = mapper.toObject(mapper.toMap(employeeDTO), Employee.class);

        if (!isBlank(employeeDTO.getEmployeeTypeId()) || !isBlank(employeeDTO.getEmployeeTypeName())) {
            Map<String, Object> employeeTypeMap = new HashMap<>();
            employeeTypeMap.put("typeId", employeeDTO.getEmployeeTypeId());
            employeeTypeMap.put("typeName", employeeDTO.getEmployeeTypeName());

            EmployeeType employeeType = mapper.toObject(employeeTypeMap, EmployeeType.class);
            employee.setEmployeeType(employeeType);
        }

        return employeeRepository.updateProfile(employee);
    }

    @Override
    public boolean updateAvatar(String employeeID, String imgSource) {
        if (isBlank(employeeID)) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }

        return employeeRepository.updateAvatar(employeeID.trim(), imgSource == null ? "" : imgSource.trim());
    }

    @Override
    public boolean deleteEmployee(String employeeID) {
        if (isBlank(employeeID)) {
            throw new IllegalArgumentException("Thiếu mã nhân viên.");
        }

        return employeeRepository.deleteById(employeeID.trim());
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