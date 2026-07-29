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

    /** The full set of district IDs the seller declares delivery to. */
    @NotEmpty(message = "At least one district must be declared")
    private Set<Long> districtIds;
}
