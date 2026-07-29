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

    @Mock
    private DistrictService districtService;

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
        verify(districtService).deriveBuyerDistrict(testUser, "Mumbai");
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
        verify(districtService, org.mockito.Mockito.never())
                .deriveBuyerDistrict(any(), any());
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

    @Test
    void getAddresses_returnsDefaultFirst() {
        Address nonDefault = new Address();
        nonDefault.setId(5L);
        nonDefault.setUser(testUser);
        nonDefault.setIsDefault(false);

        Address defaultAddress = new Address();
        defaultAddress.setId(10L);
        defaultAddress.setUser(testUser);
        defaultAddress.setIsDefault(true);

        when(addressRepository.findAllByUser_Id(1L)).thenReturn(List.of(nonDefault, defaultAddress));

        List<AddressResponseDTO> result = addressService.getAddresses(1L);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).getId(), "default address must be listed first");
        assertEquals(5L, result.get(1).getId());
    }

    @Test
    void getAddresses_noAddresses_returnsEmptyList() {
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(Collections.emptyList());

        assertTrue(addressService.getAddresses(1L).isEmpty());
    }

    @Test
    void updateAddress_ownedAddress_updatesFields() {
        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(testUser);
        existing.setStreetAddress("Old Street");
        existing.setIsDefault(true);

        UpdateAddressRequest update = new UpdateAddressRequest(
                "456 New Street", "Pune", "Maharashtra", "411001", "India", "BILLING");

        when(addressRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        AddressResponseDTO result = addressService.updateAddress(1L, 5L, update);

        assertEquals("456 New Street", result.getStreetAddress());
        assertEquals("Pune", result.getCity());
        assertEquals("411001", result.getPostalCode());
        assertEquals("BILLING", result.getAddressType());
        assertTrue(result.getIsDefault(), "updating an address must not clear its default flag");
        verify(districtService).deriveBuyerDistrict(testUser, "Pune");
    }

    @Test
    void updateAddress_notDefaultAddress_doesNotDeriveDistrict() {
        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(testUser);
        existing.setIsDefault(false);

        UpdateAddressRequest update = new UpdateAddressRequest(
                "456 New Street", "Pune", "Maharashtra", "411001", "India", "BILLING");

        when(addressRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        addressService.updateAddress(1L, 5L, update);

        verify(districtService, org.mockito.Mockito.never())
                .deriveBuyerDistrict(any(), any());
    }

    @Test
    void updateAddress_notOwnedByRequester_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);

        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(otherUser);

        UpdateAddressRequest update = new UpdateAddressRequest(
                "456 New Street", "Pune", "Maharashtra", "411001", "India", "BILLING");

        when(addressRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> addressService.updateAddress(1L, 5L, update));
        verify(addressRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void updateAddress_addressNotFound_throws() {
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateAddressRequest update = new UpdateAddressRequest(
                "456 New Street", "Pune", "Maharashtra", "411001", "India", "BILLING");

        assertThrows(ResourceNotFoundException.class, () -> addressService.updateAddress(1L, 99L, update));
    }

    @Test
    void deleteAddress_nonDefaultAddress_deletesWithoutPromotingAnother() {
        Address toDelete = new Address();
        toDelete.setId(5L);
        toDelete.setUser(testUser);
        toDelete.setIsDefault(false);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(toDelete));

        addressService.deleteAddress(1L, 5L);

        verify(addressRepository).delete(toDelete);
        verify(addressRepository, org.mockito.Mockito.never()).findAllByUser_Id(any());
    }

    @Test
    void deleteAddress_defaultAddress_promotesEarliestRemainingToDefault() {
        Address toDelete = new Address();
        toDelete.setId(5L);
        toDelete.setUser(testUser);
        toDelete.setIsDefault(true);

        Address remaining1 = new Address();
        remaining1.setId(20L);
        remaining1.setUser(testUser);
        remaining1.setIsDefault(false);

        Address remaining2 = new Address();
        remaining2.setId(15L);
        remaining2.setUser(testUser);
        remaining2.setIsDefault(false);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(toDelete));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(List.of(remaining1, remaining2));

        addressService.deleteAddress(1L, 5L);

        verify(addressRepository).delete(toDelete);
        assertTrue(remaining2.getIsDefault(), "the lowest-id remaining address must be promoted to default");
        assertFalse(remaining1.getIsDefault());
        verify(addressRepository).save(remaining2);
    }

    @Test
    void deleteAddress_defaultAddress_noneRemaining_doesNotThrow() {
        Address toDelete = new Address();
        toDelete.setId(5L);
        toDelete.setUser(testUser);
        toDelete.setIsDefault(true);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(toDelete));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> addressService.deleteAddress(1L, 5L));
        verify(addressRepository).delete(toDelete);
    }

    @Test
    void deleteAddress_notOwnedByRequester_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);

        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(otherUser);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> addressService.deleteAddress(1L, 5L));
        verify(addressRepository, org.mockito.Mockito.never()).delete(any(Address.class));
    }

    @Test
    void setDefaultAddress_marksTargetDefaultAndClearsOthers() {
        Address current = new Address();
        current.setId(5L);
        current.setUser(testUser);
        current.setIsDefault(true);

        Address target = new Address();
        target.setId(10L);
        target.setUser(testUser);
        target.setIsDefault(false);
        target.setCity("Pune");

        when(addressRepository.findById(10L)).thenReturn(Optional.of(target));
        when(addressRepository.findAllByUser_Id(1L)).thenReturn(List.of(current, target));
        when(addressRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        AddressResponseDTO result = addressService.setDefaultAddress(1L, 10L);

        assertTrue(result.getIsDefault());
        assertTrue(target.getIsDefault());
        assertFalse(current.getIsDefault(), "the previous default must be cleared");
        verify(districtService).deriveBuyerDistrict(testUser, "Pune");
    }

    @Test
    void setDefaultAddress_notOwnedByRequester_throwsAccessDenied() {
        User otherUser = new User();
        otherUser.setId(2L);

        Address existing = new Address();
        existing.setId(5L);
        existing.setUser(otherUser);

        when(addressRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(AccessDeniedException.class, () -> addressService.setDefaultAddress(1L, 5L));
        verify(addressRepository, org.mockito.Mockito.never()).saveAll(any());
    }
}
