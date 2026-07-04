package com.example.buildnest_ecommerce.service.address;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Address;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AddressResponseDTO createAddress(Long userId, CreateAddressRequest request) {
        log.info("Creating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        boolean isFirstAddress = addressRepository.findAllByUser_Id(userId).isEmpty();

        Address address = new Address();
        address.setUser(user);
        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(request.getAddressType());
        address.setIsDefault(isFirstAddress);

        Address saved = addressRepository.save(address);

        return toDTO(saved);
    }

    private AddressResponseDTO toDTO(Address address) {
        return new AddressResponseDTO(
                address.getId(),
                address.getStreetAddress(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.getIsDefault(),
                address.getAddressType());
    }
}
