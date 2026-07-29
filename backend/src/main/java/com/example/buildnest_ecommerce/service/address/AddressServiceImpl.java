package com.example.buildnest_ecommerce.service.address;

import com.example.buildnest_ecommerce.exception.AccessDeniedException;
import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Address;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.model.payload.UpdateAddressRequest;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.district.DistrictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final DistrictService districtService;

    @Override
    @Transactional
    public AddressResponseDTO createAddress(
            Long userId, CreateAddressRequest request) {
        log.info("Creating address for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userId));

        boolean isFirstAddress =
                addressRepository.findAllByUser_Id(userId).isEmpty();

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

        if (isFirstAddress) {
            districtService.deriveBuyerDistrict(user, saved.getCity());
        }

        return toDTO(saved);
    }

    @Override
    public List<AddressResponseDTO> getAddresses(Long userId) {
        log.info("Fetching addresses for user: {}", userId);

        return addressRepository.findAllByUser_Id(userId).stream()
                .sorted(Comparator.comparing(Address::getIsDefault).reversed()
                        .thenComparing(Address::getId))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(
            Long userId, Long addressId, UpdateAddressRequest request) {
        log.info("Updating address {} for user: {}", addressId, userId);

        Address address = findOwnedAddress(userId, addressId);

        address.setStreetAddress(request.getStreetAddress());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setAddressType(request.getAddressType());

        Address saved = addressRepository.save(address);

        if (Boolean.TRUE.equals(saved.getIsDefault())) {
            districtService.deriveBuyerDistrict(
                    saved.getUser(), saved.getCity());
        }

        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        log.info("Deleting address {} for user: {}", addressId, userId);

        Address address = findOwnedAddress(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());

        addressRepository.delete(address);

        if (wasDefault) {
            addressRepository.findAllByUser_Id(userId).stream()
                    .min(Comparator.comparing(Address::getId))
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Override
    @Transactional
    public AddressResponseDTO setDefaultAddress(
            Long userId, Long addressId) {
        log.info("Setting address {} as default for user: {}",
                addressId, userId);

        Address target = findOwnedAddress(userId, addressId);

        List<Address> all = addressRepository.findAllByUser_Id(userId);
        all.forEach(a -> a.setIsDefault(a.getId().equals(addressId)));
        addressRepository.saveAll(all);

        districtService.deriveBuyerDistrict(
                target.getUser(), target.getCity());

        return toDTO(target);
    }

    private Address findOwnedAddress(Long userId, Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Address", addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "Address does not belong to the requesting user");
        }

        return address;
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
