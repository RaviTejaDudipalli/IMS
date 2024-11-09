package com.lic.InsuranceManagementPortal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.lic.InsuranceManagementPortal.dto.Client;

@Component
public interface ClientRepository extends JpaRepository<Client, Integer>{

}
