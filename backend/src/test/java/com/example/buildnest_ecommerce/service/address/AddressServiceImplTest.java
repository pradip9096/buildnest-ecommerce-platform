package com.example.buildnest_ecommerce.service.address;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.AddressResponseDTO;
import com.example.buildnest_ecommerce.model.entity.Address;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.model.payload.CreateAddressRequest;
import com.example.buildnest_ecommerce.repository.AddressRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceImplTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User testUser;
    private CreateAddressRequest request;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        request = new CreateAddressRequest(
                "123 Main Street", "Mumbai", "Maharashtra", "400001", "India", "SHIPPING");
    }

    @Test
    void createAddress_firstAddress_isMarkedDefault() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
            Address a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        AddressResponseDTO result = addressService.createAddress(1L, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("123 Main Street", result.getStreetAddress());
        assertEquals("Mumbai", result.getCity());
        assertEquals("400001", result.getPostalCode());
        assertTrue(result.getIsDefault(), "first address for a user must be marked default");
    }

    @Test
    void createAddress_subsequentAddress_isNotMarkedDefault() {
        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(List.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponseDTO result = addressService.createAddress(1L, request);

        assertFalse(result.getIsDefault(), "non-first address must not be marked default");
    }

    @Test
    void createAddress_setsUserOnEntityBeforeSave() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(Collections.emptyList());
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.createAddress(1L, request);

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertEquals(testUser, captor.getValue().getUser());
    }

    @Test
    void createAddress_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> addressService.createAddress(99L, request));
        verify(addressRepository, org.mockito.Mockito.never()).save(any());
    }
}
