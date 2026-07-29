package com.example.buildnest_ecommerce.service.district;

import com.example.buildnest_ecommerce.exception.ResourceNotFoundException;
import com.example.buildnest_ecommerce.model.dto.DistrictResponseDTO;
import com.example.buildnest_ecommerce.model.entity.District;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.SellerDistrict;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.DistrictRepository;
import com.example.buildnest_ecommerce.repository.SellerDistrictRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DistrictServiceImplTest {

    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private SellerDistrictRepository sellerDistrictRepository;
    @Mock
    private SellerRepository sellerRepository;

    private DistrictServiceImpl districtService;

    @BeforeEach
    void setUp() {
        districtService = new DistrictServiceImpl(
                districtRepository, sellerDistrictRepository,
                sellerRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateSellerDistricts_validDistricts_replacesDeclaredSet() {
        Seller seller = new Seller();
        seller.setId(5L);
        District pune = new District();
        pune.setId(1L);
        pune.setName("Pune");
        District mumbai = new District();
        mumbai.setId(2L);
        mumbai.setName("Mumbai City");

        when(sellerRepository.findById(5L)).thenReturn(Optional.of(seller));
        when(districtRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(List.of(pune, mumbai));

        List<DistrictResponseDTO> result = districtService
                .updateSellerDistricts(5L, Set.of(1L, 2L));

        assertThat(result).extracting(DistrictResponseDTO::name)
                .containsExactlyInAnyOrder("Pune", "Mumbai City");
        verify(sellerDistrictRepository).deleteAllBySeller_Id(5L);

        ArgumentCaptor<List<SellerDistrict>> captor =
                ArgumentCaptor.forClass(List.class);
        verify(sellerDistrictRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(2);
        assertThat(captor.getValue()).allMatch(
                link -> link.getSeller() == seller);
        assertThat(captor.getValue())
                .extracting(link -> link.getDistrict().getName())
                .containsExactlyInAnyOrder("Pune", "Mumbai City");
    }

    @Test
    void getAllDistricts_returnsAllAsDto() {
        District pune = new District();
        pune.setId(1L);
        pune.setName("Pune");
        District mumbai = new District();
        mumbai.setId(2L);
        mumbai.setName("Mumbai City");
        when(districtRepository.findAll()).thenReturn(List.of(pune, mumbai));

        List<DistrictResponseDTO> result = districtService.getAllDistricts();

        assertThat(result).extracting(DistrictResponseDTO::name)
                .containsExactlyInAnyOrder("Pune", "Mumbai City");
    }

    @Test
    void getSellerDistricts_returnsDeclaredDistrictsAsDto() {
        Seller seller = new Seller();
        seller.setId(5L);
        District pune = new District();
        pune.setId(1L);
        pune.setName("Pune");
        SellerDistrict link = new SellerDistrict();
        link.setSeller(seller);
        link.setDistrict(pune);
        when(sellerDistrictRepository.findAllBySeller_Id(5L))
                .thenReturn(List.of(link));

        List<DistrictResponseDTO> result =
                districtService.getSellerDistricts(5L);

        assertThat(result).extracting(DistrictResponseDTO::name)
                .containsExactly("Pune");
    }

    @Test
    void updateSellerDistricts_unknownSeller_throwsResourceNotFoundException() {
        when(sellerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> districtService
                .updateSellerDistricts(99L, Set.of(1L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateSellerDistricts_unknownDistrictId_throwsResourceNotFoundException() {
        Seller seller = new Seller();
        seller.setId(5L);
        when(sellerRepository.findById(5L)).thenReturn(Optional.of(seller));
        when(districtRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> districtService
                .updateSellerDistricts(5L, Set.of(1L, 2L)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deriveBuyerDistrict_matchingCity_setsDistrictOnUser() {
        User user = new User();
        District pune = new District();
        pune.setId(1L);
        pune.setName("Pune");
        when(districtRepository.findByNameIgnoreCase("Pune"))
                .thenReturn(Optional.of(pune));

        districtService.deriveBuyerDistrict(user, "Pune");

        assertThat(user.getDistrict()).isEqualTo(pune);
    }

    @Test
    void deriveBuyerDistrict_noMatch_leavesUserDistrictUnchanged() {
        User user = new User();
        when(districtRepository.findByNameIgnoreCase("Nowhere"))
                .thenReturn(Optional.empty());

        districtService.deriveBuyerDistrict(user, "Nowhere");

        assertThat(user.getDistrict()).isNull();
    }

    @Test
    void deriveBuyerDistrict_blankCity_isNoOp() {
        User user = new User();

        districtService.deriveBuyerDistrict(user, "  ");

        verify(districtRepository, never()).findByNameIgnoreCase(any());
    }
}
