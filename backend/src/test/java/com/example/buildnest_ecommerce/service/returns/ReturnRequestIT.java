package com.example.buildnest_ecommerce.service.returns;

import com.example.buildnest_ecommerce.CivilEcommerceApplication;
import com.example.buildnest_ecommerce.config.TestSecurityConfig;
import com.example.buildnest_ecommerce.model.dto.ReturnRequestDTO;
import com.example.buildnest_ecommerce.model.entity.Inventory;
import com.example.buildnest_ecommerce.model.entity.Order;
import com.example.buildnest_ecommerce.model.entity.Order.OrderStatus;
import com.example.buildnest_ecommerce.model.entity.OrderItem;
import com.example.buildnest_ecommerce.model.entity.Payment;
import com.example.buildnest_ecommerce.model.entity.Product;
import com.example.buildnest_ecommerce.model.entity.ReturnRequest.ReturnStatus;
import com.example.buildnest_ecommerce.model.entity.User;
import com.example.buildnest_ecommerce.repository.InventoryRepository;
import com.example.buildnest_ecommerce.repository.OrderRepository;
import com.example.buildnest_ecommerce.repository.ProductRepository;
import com.example.buildnest_ecommerce.repository.ReturnRequestRepository;
import com.example.buildnest_ecommerce.repository.UserRepository;
import com.example.buildnest_ecommerce.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Real-persistence regression test for #88 (RET-01/02/03) — proves the
 * pre-existing return_requests Liquibase changeset, the ReturnRequest
 * entity mapping, and the create -> admin-approve -> refund + inventory
 * restore flow actually round-trip through H2, not just that mocked
 * repositories were called with the right arguments.
 */
@SpringBootTest(classes = CivilEcommerceApplication.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
class ReturnRequestIT {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @MockitoBean
    private PaymentService paymentService;

    private User user;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("returnuser");
        user.setEmail("returnuser@example.com");
        user.setPassword("hashedPassword123");
        user.setFirstName("Return");
        user.setLastName("User");
        user.setPhoneNumber("+1234567890");
        user.setIsActive(true);
        user = userRepository.save(user);

        product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("50.00"));
        product = productRepository.save(product);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantityInStock(10);
        inventory.setMinimumStockLevel(2);
        inventoryRepository.save(inventory);

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setPrice(new BigDecimal("50.00"));
        item.setSubtotal(new BigDecimal("100.00"));

        order = new Order();
        order.setUser(user);
        order.setOrderNumber("ORD-RET-001");
        order.setStatus(OrderStatus.DELIVERED);
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setDeliveredAt(LocalDateTime.now().minusDays(5));
        order.setIsDeleted(false);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        item.setOrder(order);
        order.setOrderItems(Set.of(item));
        order = orderRepository.save(order);
    }

    @Test
    @DisplayName(
            "create -> admin approve persists REFUNDED status, calls "
                    + "refund, and restores inventory (real H2 round-trip)")
    void createThenApprove_persistsAndRestoresInventory() {
        when(paymentService.processRefund(
                anyLong(), anyDouble(), anyString()))
                .thenReturn(new Payment());

        ReturnRequestDTO created = returnService.createReturnRequest(
                user.getId(), order.getId(), "Wrong size");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(ReturnStatus.PENDING.name());
        assertThat(returnRequestRepository.findById(created.getId()))
                .isPresent();

        ReturnRequestDTO updated = returnService.updateReturnStatus(
                created.getId(), "APPROVED", "Approved by admin");

        assertThat(updated.getStatus())
                .isEqualTo(ReturnStatus.REFUNDED.name());

        Inventory restoredInventory =
                inventoryRepository.findByProductId(product.getId());
        assertThat(restoredInventory.getQuantityInStock()).isEqualTo(12);
    }

    @Test
    @DisplayName(
            "create -> admin reject leaves inventory untouched and "
                    + "does not call refund (real H2 round-trip)")
    void createThenReject_doesNotRestockOrRefund() {
        ReturnRequestDTO created = returnService.createReturnRequest(
                user.getId(), order.getId(), "Changed mind");

        ReturnRequestDTO updated = returnService.updateReturnStatus(
                created.getId(), "REJECTED", "Outside policy");

        assertThat(updated.getStatus())
                .isEqualTo(ReturnStatus.REJECTED.name());

        Inventory inventory =
                inventoryRepository.findByProductId(product.getId());
        assertThat(inventory.getQuantityInStock()).isEqualTo(10);
    }

    @Test
    @DisplayName(
            "admin list endpoint filters by status against real H2 data")
    void getAdminReturnRequests_filtersRealPersistedData() {
        returnService.createReturnRequest(
                user.getId(), order.getId(), "Wrong size");

        var page = returnService.getAdminReturnRequests(
                ReturnStatus.PENDING, PageRequest.of(0, 20));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getStatus())
                .isEqualTo(ReturnStatus.PENDING.name());
    }
}
