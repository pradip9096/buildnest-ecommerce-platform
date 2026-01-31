package com.example.buildnest_ecommerce.util;

import com.example.buildnest_ecommerce.model.dto.PublicProductDTO;
import com.example.buildnest_ecommerce.model.dto.PublicUserDTO;
import com.example.buildnest_ecommerce.model.dto.PublicOrderDTO;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Address;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility component for mapping between entities and DTOs.
 * Ensures consistent transformation of data for API responses.
 * Handles security-focused transformations to exclude sensitive fields.
 */
@Component
public class MapperUtil {

    /**
     * Maps Product entity to PublicProductDTO for API responses.
     * Excludes sensitive fields like supplier details and internal costs.
     */
    public PublicProductDTO toPublicProductDTO(Product product) {
        if (product == null) {
            return null;
        }

        PublicProductDTO dto = new PublicProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setIsActive(product.getIsActive());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        return dto;
    }

    /**
     * Maps list of Product entities to PublicProductDTOs.
     */
    public List<PublicProductDTO> toPublicProductDTOs(List<Product> products) {
        if (products == null) {
            return null;
        }
        return products.stream()
                .map(this::toPublicProductDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps User entity to PublicUserDTO for API responses.
     * Excludes sensitive fields like passwords, roles, and internal security data.
     */
    public PublicUserDTO toPublicUserDTO(User user) {
        if (user == null) {
            return null;
        }

        PublicUserDTO dto = new PublicUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhoneNumber());

        // Get primary address or null
        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            Address primaryAddress = user.getAddresses().stream()
                    .filter(Address::getIsDefault)
                    .findFirst()
                    .orElse(user.getAddresses().stream().findFirst().orElse(null));

            if (primaryAddress != null) {
                dto.setAddress(primaryAddress.getStreetAddress() + ", "
                        + primaryAddress.getCity() + ", "
                        + primaryAddress.getState() + " "
                        + primaryAddress.getPostalCode());
            }
        }

        return dto;
    }

    /**
     * Maps list of User entities to PublicUserDTOs.
     */
    public List<PublicUserDTO> toPublicUserDTOs(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(this::toPublicUserDTO)
                .collect(Collectors.toList());
    }

    /**
     * Maps Order entity to PublicOrderDTO for API responses.
     * Excludes internal status fields and sensitive transaction details.
     */
    public PublicOrderDTO toPublicOrderDTO(Order order) {
        if (order == null) {
            return null;
        }

        PublicOrderDTO dto = new PublicOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus() != null ? order.getStatus().toString() : null);
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Format shipping address
        if (order.getShippingAddress() != null) {
            Address addr = order.getShippingAddress();
            dto.setShippingAddress(addr.getStreetAddress() + ", "
                    + addr.getCity() + ", "
                    + addr.getState() + " "
                    + addr.getPostalCode() + ", "
                    + addr.getCountry());
        }

        if (order.getOrderItems() != null) {
            List<PublicOrderDTO.PublicOrderItemDTO> items = order.getOrderItems().stream()
                    .map(item -> new PublicOrderDTO.PublicOrderItemDTO(
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getPrice()))
                    .collect(Collectors.toList());
            dto.setItems(items);
        }

        return dto;
    }

    /**
     * Maps list of Order entities to PublicOrderDTOs.
     */
    public List<PublicOrderDTO> toPublicOrderDTOs(List<Order> orders) {
        if (orders == null) {
            return null;
        }
        return orders.stream()
                .map(this::toPublicOrderDTO)
                .collect(Collectors.toList());
    }
}
