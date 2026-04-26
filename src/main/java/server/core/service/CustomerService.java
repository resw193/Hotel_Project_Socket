package server.core.service;

import common.dto.CustomerDTO;

import java.util.List;

public interface CustomerService {
    List<CustomerDTO> getAll();

    List<CustomerDTO> getByLoyalty(int loyalty);

    List<CustomerDTO> searchByName(String keyword);

    List<CustomerDTO> filterAndSearch(String keyword, Integer minLoyalty);

    CustomerDTO getById(String customerID);

    CustomerDTO getByPhone(String phone);

    CustomerDTO getByCCCD(String idCard);

    boolean addCustomer(CustomerDTO customerDTO);

    boolean updateCustomer(CustomerDTO customerDTO);
}