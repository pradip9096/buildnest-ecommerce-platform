package com.example.buildnest_ecommerce.repository;

import com.example.buildnest_ecommerce.model.entity.District;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.SellerDistrict;
import com.example.buildnest_ecommerce.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real H2-backed mapping tests for {@link District}/{@link SellerDistrict}
 * (FR-LOC-01/02, ADR 0001, #562) — a Mockito-mocked unit test cannot observe
 * FK/unique-constraint enforcement or the {@code @ManyToOne} mapping itself.
 */
@DataJpaTest
@ActiveProfiles("test")
@SuppressWarnings("null")
class DistrictRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private SellerDistrictRepository sellerDistrictRepository;

    private User seedUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("hashed");
        entityManager.persist(user);
        return user;
    }

    private Seller seedSeller(User user) {
        Seller seller = new Seller();
        seller.setUser(user);
        seller.setBusinessName("Test Traders");
        entityManager.persist(seller);
        return seller;
    }

    @Test
    @DisplayName("findByNameIgnoreCase matches regardless of case")
    void testFindByNameIgnoreCase() {
        District district = new District();
        district.setName("Pune");
        entityManager.persistAndFlush(district);
        entityManager.clear();

        Optional<District> found =
                districtRepository.findByNameIgnoreCase("PUNE");

        assertTrue(found.isPresent());
        assertEquals("Pune", found.get().getName());
    }

    @Test
    @DisplayName("district name uniqueness is enforced at the DB layer")
    void testDuplicateDistrictNameRejected() {
        District first = new District();
        first.setName("Mumbai City");
        entityManager.persistAndFlush(first);

        District duplicate = new District();
        duplicate.setName("Mumbai City");

        // District.id is GenerationType.IDENTITY — Hibernate must execute
        // the INSERT eagerly at persist() to obtain the generated key, so
        // the constraint violation surfaces here, not at a later flush().
        assertThrows(Exception.class,
                () -> entityManager.persist(duplicate));
    }

    @Test
    @DisplayName("SellerDistrict maps seller and district and enforces "
            + "seller+district uniqueness")
    void testSellerDistrictMappingAndUniqueness() {
        User user = seedUser("selleruser");
        Seller seller = seedSeller(user);

        District district = new District();
        district.setName("Bengaluru Urban");
        entityManager.persistAndFlush(district);

        SellerDistrict link = new SellerDistrict();
        link.setSeller(seller);
        link.setDistrict(district);
        entityManager.persistAndFlush(link);
        entityManager.clear();

        List<SellerDistrict> found =
                sellerDistrictRepository.findAllBySeller_Id(seller.getId());
        assertEquals(1, found.size());
        assertEquals("Bengaluru Urban", found.get(0).getDistrict().getName());

        SellerDistrict duplicateLink = new SellerDistrict();
        duplicateLink.setSeller(
                entityManager.find(Seller.class, seller.getId()));
        duplicateLink.setDistrict(
                entityManager.find(District.class, district.getId()));

        // SellerDistrict.id is also GenerationType.IDENTITY — same eager-
        // insert-at-persist() behavior as District above.
        assertThrows(Exception.class,
                () -> entityManager.persist(duplicateLink));
    }

    @Test
    @DisplayName("deleteAllBySeller_Id removes only that seller's declared "
            + "districts")
    void testDeleteAllBySellerId() {
        User userA = seedUser("sellerA");
        Seller sellerA = seedSeller(userA);
        User userB = seedUser("sellerB");
        Seller sellerB = seedSeller(userB);

        District district = new District();
        district.setName("Pune");
        entityManager.persistAndFlush(district);

        SellerDistrict linkA = new SellerDistrict();
        linkA.setSeller(sellerA);
        linkA.setDistrict(district);
        entityManager.persist(linkA);

        SellerDistrict linkB = new SellerDistrict();
        linkB.setSeller(sellerB);
        linkB.setDistrict(district);
        entityManager.persist(linkB);
        entityManager.flush();
        entityManager.clear();

        sellerDistrictRepository.deleteAllBySeller_Id(sellerA.getId());
        entityManager.flush();

        assertTrue(sellerDistrictRepository
                .findAllBySeller_Id(sellerA.getId()).isEmpty());
        assertEquals(1, sellerDistrictRepository
                .findAllBySeller_Id(sellerB.getId()).size());
    }
}
