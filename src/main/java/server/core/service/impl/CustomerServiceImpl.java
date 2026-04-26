package server.core.service.impl;

import common.dto.CustomerDTO;
import common.entity.Customer;
import server.core.repository.CustomerRepository;
import server.core.service.CustomerService;
import server.core.service.IdGeneratorService;
import server.infrastructure.mapper.GenericDataMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final IdGeneratorService idGeneratorService;
    private final GenericDataMapper mapper;

    public CustomerServiceImpl(CustomerRepository customerRepository, IdGeneratorService idGeneratorService, GenericDataMapper mapper) {
        this.customerRepository = customerRepository;
        this.idGeneratorService = idGeneratorService;
        this.mapper = mapper;
    }

    @Override
    public List<CustomerDTO> getAll() {
        return customerRepository.findAll()
                .stream()
                .map(customer -> mapper.toObject(mapper.toMap(customer), CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> getByLoyalty(int loyalty) {
        return customerRepository.findByMinLoyalty(loyalty)
                .stream()
                .map(customer -> mapper.toObject(mapper.toMap(customer), CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> searchByName(String keyword) {
        String kw = normalize(keyword);
        if (kw.isEmpty()) return getAll();

        return customerRepository.findByNameContaining(kw)
                .stream()
                .map(customer -> mapper.toObject(mapper.toMap(customer), CustomerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerDTO> filterAndSearch(String keyword, Integer minLoyalty) {
        List<CustomerDTO> customers = (minLoyalty == null || minLoyalty <= 0)
                ? getAll()
                : getByLoyalty(minLoyalty);

        if (customers.isEmpty()) return customers;

        String kw = normalize(keyword);
        if (kw.isEmpty()) return customers;

        return customers.stream()
                .filter(customer -> {
                    String id = normalize(customer.getCustomerId());
                    String name = normalize(customer.getFullName());

                    return (!id.isEmpty() && id.contains(kw)) || (!name.isEmpty() && name.contains(kw));
                })
                .collect(Collectors.toList());
    }

    @Override
    public CustomerDTO getById(String customerID) {
        Customer customer = customerRepository.findById(check(customerID));
        return customer == null ? null : mapper.toObject(mapper.toMap(customer), CustomerDTO.class);
    }

    @Override
    public CustomerDTO getByPhone(String phone) {
        Customer customer = customerRepository.findByPhone(check(phone));
        return customer == null ? null : mapper.toObject(mapper.toMap(customer), CustomerDTO.class);
    }

    @Override
    public CustomerDTO getByCCCD(String idCard) {
        Customer customer = customerRepository.findByIdCard(check(idCard));
        return customer == null ? null : mapper.toObject(mapper.toMap(customer), CustomerDTO.class);
    }

    @Override
    public boolean addCustomer(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không hợp lệ.");
        }

        String fullName = checkName(customerDTO.getFullName());
        String phone = checkPhone(customerDTO.getPhone());
        String email = checkEmail(customerDTO.getEmail());
        String idCard = checkIdCard(customerDTO.getIdCard());

        if (customerRepository.findByPhone(phone) != null) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại.");
        }

        if (!email.isEmpty() && customerRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        if (!idCard.isEmpty() && customerRepository.findByIdCard(idCard) != null) {
            throw new IllegalArgumentException("CCCD đã tồn tại.");
        }

        customerDTO.setCustomerId(idGeneratorService.generateCustomerId());
        customerDTO.setFullName(fullName);
        customerDTO.setPhone(phone);
        customerDTO.setEmail(email);
        customerDTO.setIdCard(idCard.isEmpty() ? null : idCard);
        customerDTO.setRegisDate(LocalDateTime.now());
        customerDTO.setLoyaltyPoint(0);

        Customer customer = mapper.toObject(mapper.toMap(customerDTO), Customer.class);
        return customerRepository.add(customer);
    }

    @Override
    public boolean updateCustomer(CustomerDTO customerDTO) {
        if (customerDTO == null) {
            throw new IllegalArgumentException("Dữ liệu khách hàng không hợp lệ.");
        }

        String customerID = check(customerDTO.getCustomerId());
        if (customerID.isEmpty()) {
            throw new IllegalArgumentException("customerID không hợp lệ.");
        }

        customerDTO.setCustomerId(customerID);
        customerDTO.setFullName(checkName(customerDTO.getFullName()));
        customerDTO.setPhone(checkPhone(customerDTO.getPhone()));
        customerDTO.setEmail(checkEmail(customerDTO.getEmail()));

        Customer customer = mapper.toObject(mapper.toMap(customerDTO), Customer.class);
        return customerRepository.update(customer);
    }

    private String check(String s) {
        return s == null ? "" : s.trim();
    }

    private String normalize(String s) {
        return check(s).toLowerCase();
    }

    private String checkName(String s) {
        s = check(s);
        if (s.isEmpty()) throw new IllegalArgumentException("Họ tên không được để trống.");
        return s;
    }

    private String checkPhone(String s) {
        s = check(s);
        if (!s.matches("^0\\d{9}$")) {
            throw new IllegalArgumentException("Số điện thoại phải 10 chữ số và bắt đầu bằng 0.");
        }
        return s;
    }

    private String checkEmail(String s) {
        s = check(s);
        if (s.isEmpty()) return "";
        if (!s.matches("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }
        return s.toLowerCase();
    }

    private String checkIdCard(String s) {
        s = check(s);
        if (s.isEmpty()) return "";
        if (!s.matches("^\\d{12}$")) {
            throw new IllegalArgumentException("CCCD phải đủ 12 chữ số.");
        }
        return s;
    }
}