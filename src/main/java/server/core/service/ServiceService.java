package server.core.service;

import common.dto.ServiceDTO;

import java.util.List;

public interface ServiceService {
    List<ServiceDTO> getAll();

    List<ServiceDTO> getByType(String type);

    ServiceDTO getByID(String id);

    ServiceDTO getByName(String name);

    boolean add(ServiceDTO serviceDTO);

    boolean updateInfo(ServiceDTO serviceDTO);

    boolean updateQuantity(String serviceID, int newQty);

    boolean increaseQuantity(String serviceID, int add);

    boolean delete(String serviceID);
}