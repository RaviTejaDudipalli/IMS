package com.lic.InsuranceManagementPortal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.lic.InsuranceManagementPortal.dto.InsurancePolicy;

@Component
public interface InsurancePolicyRepository extends JpaRepository<InsurancePolicy, Integer>{

}
