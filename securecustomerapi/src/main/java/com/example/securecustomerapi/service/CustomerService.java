package com.example.securecustomerapi.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.securecustomerapi.dto.CustomerRequestDTO;
import com.example.securecustomerapi.dto.CustomerResponseDTO;
import com.example.securecustomerapi.entity.CustomerStatus;

public interface CustomerService {
    
    List<CustomerResponseDTO> getAllCustomers();
    
    Page<CustomerResponseDTO> getAllCustomers(Pageable pageable);
    
    List<CustomerResponseDTO> getAllCustomers(Sort sort);
    
    CustomerResponseDTO getCustomerById(Long id);
    
    CustomerResponseDTO createCustomer(CustomerRequestDTO requestDTO);
    
    CustomerResponseDTO updateCustomer(Long id, CustomerRequestDTO requestDTO);
    
    CustomerResponseDTO partialUpdateCustomer(Long id, com.example.securecustomerapi.dto.CustomerUpdateDTO updateDTO);
    
    void deleteCustomer(Long id);
    
    List<CustomerResponseDTO> searchCustomers(String keyword);
    
    List<CustomerResponseDTO> getCustomersByStatus(CustomerStatus status);
    
    List<CustomerResponseDTO> advancedSearch(String name, String email, String status);
}
