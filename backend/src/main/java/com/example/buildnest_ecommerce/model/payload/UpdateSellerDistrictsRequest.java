package com.example.buildnest_ecommerce.model.payload;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSellerDistrictsRequest {

    @NotEmpty(message = "At least one district must be declared")
    private Set<Long> districtIds;
}
