package com.lic.InsuranceManagementPortal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.lic.InsuranceManagementPortal.dto.Claim;

@Component
public interface ClaimRepository extends JpaRepository<Claim, Integer>{

}
