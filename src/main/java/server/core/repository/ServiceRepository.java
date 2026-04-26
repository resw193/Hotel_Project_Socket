package server.core.repository;

import common.entity.Service;

import java.util.List;

public interface ServiceRepository {
    List<Service> findAll();

    List<Service> findByType(String serviceType);

    Service findById(String serviceId);

    Service findByName(String serviceName);

    boolean add(Service service);

    boolean update(Service service);

    boolean increaseQuantity(String serviceId, int add);
    boolean updateQuantity(String serviceId, int quantity);

    boolean deleteById(String serviceId);
}