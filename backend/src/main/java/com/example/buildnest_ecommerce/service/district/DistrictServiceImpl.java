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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * District reference-data operations (FR-LOC-01/02, ADR 0001, #561/#562).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistrictServiceImpl implements DistrictService {

    private final DistrictRepository districtRepository;
    private final SellerDistrictRepository sellerDistrictRepository;
    private final SellerRepository sellerRepository;

    @Override
    public List<DistrictResponseDTO> getAllDistricts() {
        return districtRepository.findAll().stream()
                .map(DistrictResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional
    public List<DistrictResponseDTO> updateSellerDistricts(
            Long sellerId, Set<Long> districtIds) {
        log.info("Updating declared districts for seller: {}", sellerId);

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Seller", sellerId));

        List<District> districts = districtRepository.findAllById(districtIds);
        if (districts.size() != districtIds.size()) {
            throw new ResourceNotFoundException(
                    "One or more districts not found in reference table");
        }

        sellerDistrictRepository.deleteAllBySeller_Id(sellerId);

        List<SellerDistrict> links = districts.stream().map(district -> {
            SellerDistrict link = new SellerDistrict();
            link.setSeller(seller);
            link.setDistrict(district);
            return link;
        }).toList();
        sellerDistrictRepository.saveAll(links);

        return districts.stream().map(DistrictResponseDTO::from).toList();
    }

    @Override
    public List<DistrictResponseDTO> getSellerDistricts(Long sellerId) {
        return sellerDistrictRepository.findAllBySeller_Id(sellerId).stream()
                .map(link -> DistrictResponseDTO.from(link.getDistrict()))
                .toList();
    }

    @Override
    @Transactional
    public void deriveBuyerDistrict(User user, String city) {
        if (city == null || city.isBlank()) {
            return;
        }
        districtRepository.findByNameIgnoreCase(city.trim())
                .ifPresent(user::setDistrict);
    }
}
