package com.example.buildnest_ecommerce.service.checkout;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.entity.Cart;
import com.example.buildnest_ecommerce.model.entity.CartItem;
import com.example.buildnest_ecommerce.model.entity.District;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.Seller;
import com.example.buildnest_ecommerce.model.entity.SellerDistrict;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.CartRepository;
import com.example.buildnest_ecommerce.repository.DistrictRepository;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.SellerDistrictRepository;
import com.example.buildnest_ecommerce.repository.SellerRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for #564 (FR-LOC-04): {@code validateCheckout} previously
 * had no {@code @Transactional} of its own — safe only because it read
 * nothing beyond lazy-proxy IDs. The new district check calls
 * {@code item.getProduct().getSeller()} on a lazy {@code CartItem.product}
 * proxy, which forces full initialization and requires an active Hibernate
 * session. {@code CheckoutController.validateCheckout()} calls this method
 * directly with no transaction of its own, so without
 * {@code @Transactional(readOnly = true)} on the service method itself this
 * would throw {@code LazyInitializationException} on that exact call path.
 *
 * <p>Deliberately NOT {@code @DataJpaTest} and NOT annotated
 * {@code @Transactional} at the class/method level — either would wrap the
 * whole test in one ambient transaction and mask the very gap being tested.
 * Setup data is persisted and committed in its own transaction via
 * {@link TransactionTemplate} first, so the assertion below runs against
 * fully-detached entities with zero ambient transaction, mirroring the
 * real controller call path.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class CheckoutValidateNoAmbientTransactionIT {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private SellerDistrictRepository sellerDistrictRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private CartRepository cartRepository;

    private Long buyerId;
    private Long cartId;

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            District puneDistrict = new District();
            puneDistrict.setName("Pune-564-" + System.nanoTime());
            puneDistrict = districtRepository.save(puneDistrict);

            User sellerUser = new User();
            sellerUser.setUsername("seller564-" + System.nanoTime());
            sellerUser.setEmail(sellerUser.getUsername() + "@example.com");
            sellerUser.setPassword("hashed");
            sellerUser = userRepository.save(sellerUser);

            Seller seller = new Seller();
            seller.setUser(sellerUser);
            seller.setBusinessName("Test Traders 564");
            seller = sellerRepository.save(seller);

            SellerDistrict sellerDistrict = new SellerDistrict();
            sellerDistrict.setSeller(seller);
            sellerDistrict.setDistrict(puneDistrict);
            sellerDistrictRepository.save(sellerDistrict);

            User buyer = new User();
            buyer.setUsername("buyer564-" + System.nanoTime());
            buyer.setEmail(buyer.getUsername() + "@example.com");
            buyer.setPassword("hashed");
            buyer.setDistrict(puneDistrict);
            buyer = userRepository.save(buyer);
            buyerId = buyer.getId();

            Product product = new Product();
            product.setName("District-Restricted Product 564");
            product.setPrice(BigDecimal.valueOf(100));
            product.setIsActive(true);
            product.setSeller(sellerUser);
            product = productRepository.save(product);

            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantityInStock(10);
            inventory.setMinimumStockLevel(1);
            inventoryRepository.save(inventory);

            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(1);
            item.setPrice(BigDecimal.valueOf(100));

            Cart cart = new Cart();
            cart.setUser(buyer);
            cart.setItems(java.util.List.of(item));
            item.setCart(cart);
            cart = cartRepository.save(cart);
            cartId = cart.getId();
        });
    }

    @Test
    @DisplayName("validateCheckout succeeds with no ambient transaction "
            + "when buyer is within the seller's declared district")
    void validateCheckout_noAmbientTransaction_succeeds() {
        // No @Transactional anywhere in this test — proves
        // @Transactional(readOnly = true) on the service method itself
        // (not the caller) is what makes the lazy Product/seller load
        // safe, matching the real CheckoutController call path.
        assertTrue(checkoutService.validateCheckout(buyerId, cartId));
    }

    @Test
    @DisplayName("validateCheckout blocks with no ambient transaction "
            + "when buyer district can't be verified against the seller's")
    void validateCheckout_noAmbientTransaction_blocksMismatch() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            User buyer = userRepository.findById(buyerId).orElseThrow();
            District otherDistrict = new District();
            otherDistrict.setName("Mumbai-564-" + System.nanoTime());
            otherDistrict = districtRepository.save(otherDistrict);
            buyer.setDistrict(otherDistrict);
            userRepository.save(buyer);
        });

        assertFalse(checkoutService.validateCheckout(buyerId, cartId));
    }
}
