package com.ims.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

    @NotBlank(message = "Supplier name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 150)
    private String contactPerson;

    @Email(message = "Email must be valid")
    @Size(max = 150)
    private String email;

    @Size(max = 30)
    private String phone;

    @Size(max = 500)
    private String address;
}
