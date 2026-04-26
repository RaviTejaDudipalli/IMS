package com.ims.service;

import com.ims.dto.request.SupplierRequest;
import com.ims.dto.response.SupplierResponse;
import com.ims.entity.Supplier;
import com.ims.exception.BusinessException;
import com.ims.exception.DuplicateResourceException;
import com.ims.exception.ResourceNotFoundException;
import com.ims.mapper.SupplierMapper;
import com.ims.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        if (request.getEmail() != null
                && supplierRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Supplier already exists with email: " + request.getEmail());
        }
        Supplier saved = supplierRepository.save(Supplier.builder()
                .name(request.getName().trim())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail() != null ? request.getEmail().toLowerCase() : null)
                .phone(request.getPhone())
                .address(request.getAddress())
                .build());
        log.info("Created supplier id={} name={}", saved.getId(), saved.getName());
        return supplierMapper.toResponse(saved);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));

        if (request.getEmail() != null
                && (supplier.getEmail() == null
                || !supplier.getEmail().equalsIgnoreCase(request.getEmail()))
                && supplierRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("Supplier already exists with email: " + request.getEmail());
        }
        supplier.setName(request.getName().trim());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail() != null ? request.getEmail().toLowerCase() : null);
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        log.info("Updated supplier id={}", id);
        return supplierMapper.toResponse(supplier);
    }

    @Transactional(readOnly = true)
    public SupplierResponse getById(Long id) {
        return supplierMapper.toResponse(requireById(id));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAll(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = requireById(id);
        if (!supplier.getProducts().isEmpty()) {
            throw new BusinessException(
                    "Cannot delete supplier with existing products. Reassign products first.");
        }
        supplierRepository.delete(supplier);
        log.info("Deleted supplier id={}", id);
    }

    Supplier requireById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Supplier", id));
    }
}
