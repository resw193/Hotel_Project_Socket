package server.core.service.impl;

import common.dto.ServiceDTO;
import common.entity.Service;
import common.entity.UnitPrice;
import server.core.repository.ServiceRepository;
import server.core.service.IdGeneratorService;
import server.core.service.ServiceService;
import server.infrastructure.mapper.GenericDataMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final IdGeneratorService idGeneratorService;
    private final GenericDataMapper mapper;

    public ServiceServiceImpl(ServiceRepository serviceRepository, IdGeneratorService idGeneratorService, GenericDataMapper mapper) {
        this.serviceRepository = serviceRepository;
        this.idGeneratorService = idGeneratorService;
        this.mapper = mapper;
    }

    @Override
    public List<ServiceDTO> getAll() {
        return serviceRepository.findAll()
                .stream()
                .map(service -> {
                    ServiceDTO dto = mapper.toObject(mapper.toMap(service), ServiceDTO.class);
                    if (service.getUnitPrice() != null) {
                        dto.setUnitId(service.getUnitPrice().getUnitId());
                        dto.setUnitName(service.getUnitPrice().getUnitName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ServiceDTO> getByType(String type) {
        String normalizedType = capitalize(normalize(type));
        if (normalizedType.isEmpty()) {
            return getAll();
        }

        return serviceRepository.findByType(normalizedType)
                .stream()
                .map(service -> {
                    ServiceDTO dto = mapper.toObject(mapper.toMap(service), ServiceDTO.class);
                    if (service.getUnitPrice() != null) {
                        dto.setUnitId(service.getUnitPrice().getUnitId());
                        dto.setUnitName(service.getUnitPrice().getUnitName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ServiceDTO getByID(String id) {
        id = check(id);
        if (id.isEmpty()) {
            return null;
        }

        Service service = serviceRepository.findById(id);
        if (service == null) {
            return null;
        }

        ServiceDTO dto = mapper.toObject(mapper.toMap(service), ServiceDTO.class);
        if (service.getUnitPrice() != null) {
            dto.setUnitId(service.getUnitPrice().getUnitId());
            dto.setUnitName(service.getUnitPrice().getUnitName());
        }

        return dto;
    }

    @Override
    public ServiceDTO getByName(String name) {
        name = check(name);
        if (name.isEmpty()) {
            return null;
        }

        Service service = serviceRepository.findByName(name);
        if (service == null) {
            return null;
        }

        ServiceDTO dto = mapper.toObject(mapper.toMap(service), ServiceDTO.class);
        if (service.getUnitPrice() != null) {
            dto.setUnitId(service.getUnitPrice().getUnitId());
            dto.setUnitName(service.getUnitPrice().getUnitName());
        }

        return dto;
    }

    @Override
    public boolean add(ServiceDTO serviceDTO) {
        if (serviceDTO == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu dịch vụ.");
        }

        if (isBlank(serviceDTO.getImgSource())) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh dịch vụ.");
        }

        if (isBlank(serviceDTO.getServiceId())) {
            serviceDTO.setServiceId(idGeneratorService.generateServiceId());
        }

        serviceDTO.setServiceName(check(serviceDTO.getServiceName()));
        serviceDTO.setServiceType(capitalize(normalize(serviceDTO.getServiceType())));

        attachUnitAutomatically(serviceDTO);
        validateData(serviceDTO);

        Service service = mapper.toObject(mapper.toMap(serviceDTO), Service.class);

        Map<String, Object> unitMap = new HashMap<>();
        unitMap.put("unitId", serviceDTO.getUnitId());
        unitMap.put("unitName", serviceDTO.getUnitName());

        UnitPrice unitPrice = mapper.toObject(unitMap, UnitPrice.class);
        service.setUnitPrice(unitPrice);

        return serviceRepository.add(service);
    }

    @Override
    public boolean updateInfo(ServiceDTO serviceDTO) {
        if (serviceDTO == null) {
            throw new IllegalArgumentException("Thiếu dữ liệu dịch vụ.");
        }

        String serviceId = check(serviceDTO.getServiceId());
        if (serviceId.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã dịch vụ.");
        }

        Service current = serviceRepository.findById(serviceId);
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
        }

        serviceDTO.setServiceId(serviceId);
        serviceDTO.setServiceName(check(serviceDTO.getServiceName()));
        serviceDTO.setServiceType(capitalize(normalize(serviceDTO.getServiceType())));

        if (isBlank(serviceDTO.getImgSource())) {
            serviceDTO.setImgSource(current.getImgSource());
        }

        attachUnitAutomatically(serviceDTO);
        validateData(serviceDTO);

        Service service = mapper.toObject(mapper.toMap(serviceDTO), Service.class);

        Map<String, Object> unitMap = new HashMap<>();
        unitMap.put("unitId", serviceDTO.getUnitId());
        unitMap.put("unitName", serviceDTO.getUnitName());

        UnitPrice unitPrice = mapper.toObject(unitMap, UnitPrice.class);
        service.setUnitPrice(unitPrice);

        return serviceRepository.update(service);
    }

    @Override
    public boolean updateQuantity(String serviceID, int newQty) {
        serviceID = check(serviceID);
        if (serviceID.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã dịch vụ.");
        }
        if (newQty < 0) {
            throw new IllegalArgumentException("Số lượng phải >= 0.");
        }

        return serviceRepository.updateQuantity(serviceID, newQty);
    }

    @Override
    public boolean increaseQuantity(String serviceID, int add) {
        serviceID = check(serviceID);

        if (serviceID.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã dịch vụ.");
        }

        if (add <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải > 0.");
        }

        Service current = serviceRepository.findById(serviceID);
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ.");
        }

        return serviceRepository.increaseQuantity(serviceID, add);
    }

    @Override
    public boolean delete(String serviceID) {
        serviceID = check(serviceID);
        if (serviceID.isEmpty()) {
            throw new IllegalArgumentException("Thiếu mã dịch vụ.");
        }

        return serviceRepository.deleteById(serviceID);
    }

    private void attachUnitAutomatically(ServiceDTO serviceDTO) {
        if (!isBlank(serviceDTO.getUnitName())) {
            return;
        }

        String unitName = guessUnitName(serviceDTO);
        serviceDTO.setUnitName(unitName);
    }

    private String guessUnitName(ServiceDTO serviceDTO) {
        String type = normalize(serviceDTO.getServiceType());
        String name = normalize(serviceDTO.getServiceName());

        if ("food".equals(type)) {
            return "phần";
        }

        if ("laundry".equals(type)) {
            return "lượt";
        }

        if ("drink".equals(type)) {
            if (name.contains("rượu") || name.contains("ruou")
                    || name.contains("vang") || name.contains("soju") || name.contains("chai")) {
                return "chai";
            }

            if (name.contains("bia")) {
                return "thùng";
            }

            return "lon";
        }

        return "lượt";
    }

    private void validateData(ServiceDTO serviceDTO) {
        if (isBlank(serviceDTO.getServiceName())) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }

        if (isBlank(serviceDTO.getServiceType())) {
            throw new IllegalArgumentException("Loại dịch vụ không được trống.");
        }

        if (serviceDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá phải > 0.");
        }

        if (serviceDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng phải >= 0.");
        }
    }

    private String check(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalize(String s) {
        return check(s).toLowerCase();
    }

    private String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}