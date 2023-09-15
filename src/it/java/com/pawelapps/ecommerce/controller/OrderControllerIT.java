package com.pawelapps.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.pawelapps.ecommerce.BaseIT;
import com.pawelapps.ecommerce.dto.OrderDto;
import com.pawelapps.ecommerce.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class OrderControllerIT extends BaseIT {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    private final String authorizedUserEmail = "authorized@example.com";
    private final String unauthorizedUserEmail = "unauthorized@example.com";
    private final String uri = "/api/orders";

    private Order order1;
    private Order order2;
    private Customer customer1;
    private Customer customer2;
    private ShippingAddress shippingAddress1;
    private ShippingAddress shippingAddress2;
    private User authorizedUser;
    private User unauthorizedUser;
    private Summary summary1;
    private Summary summary2;
    private OrderProduct orderProduct1;
    private OrderProduct orderProduct2;

    private Order getOrderFromDB(Long id) {
        Order order;
        TypedQuery<Order> query = entityManager.createQuery(
                "SELECT o FROM Order o WHERE o.id = :id", Order.class);
        query.setParameter("id", id);

        try {
            order = query.getSingleResult();
        } catch (NoResultException noResultException) {
            order = null;
        }

        entityManager.clear();

        return order;
    }

    @BeforeEach
    void setUp() {
        orderProduct1 = OrderProduct.builder()
                .name("Test Product One")
                .description("Test Description One")
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(1))
                .build();

        orderProduct2 = OrderProduct.builder()
                .name("Test Product Two")
                .description("Test Description Two")
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(2))
                .build();

        authorizedUser = User.builder()
                .email(authorizedUserEmail)
                .build();

        customer1 = Customer.builder()
                .firstName("First Name One")
                .lastName("Last Name One").phoneNumber(123456789)
                .email("email1@example.com")
                .build();

        shippingAddress1 = ShippingAddress.builder()
                .city("City One")
                .country("Country One")
                .zipCode("12-345")
                .streetAddress("Street One")
                .build();

        summary1 = Summary.builder()
                .totalCartValue(BigDecimal.valueOf(5))
                .totalQuantityOfProducts(3)
                .build();

        order1 = Order.builder()
                .user(authorizedUser)
                .customer(customer1)
                .shippingAddress(shippingAddress1)
                .summary(summary1)
                .build();

        order1.addOrderProduct(orderProduct1);
        order1.addOrderProduct(orderProduct2);

        customer2 = Customer.builder()
                .firstName("First Name Two")
                .lastName("Last Name Two")
                .phoneNumber(111222333)
                .email("email2@example.com")
                .build();

        shippingAddress2 = ShippingAddress.builder()
                .city("City Two")
                .country("Country Two")
                .zipCode("12-345")
                .streetAddress("Street Two")
                .build();

        summary2 = Summary.builder()
                .totalCartValue(BigDecimal.valueOf(5))
                .totalQuantityOfProducts(3)
                .build();

        order2 = Order.builder().user(authorizedUser).customer(customer2).shippingAddress(shippingAddress2).summary(summary2).build();
        order2.addOrderProduct(orderProduct1);
        order2.addOrderProduct(orderProduct2);

        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.flush();
    }

    @Nested
    class getAllOrdersTests {

        @Test
        @WithMockUser(authorities = "admin")
        void shouldGetAllOrdersForAuthorizedUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(uri).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @WithMockUser(authorities = "user")
        void shouldNotGetOrdersForUnauthorizedUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(uri).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

        }

        @Test
        @WithAnonymousUser
        void shouldNotGetOrdersForAnonymousUser() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get(uri).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

    }

    @Nested
    class SaveOrderTests {
        private OrderDto orderDtoForSave;

        @BeforeEach
        void setUp() {
            User authorizedUserForSave = User.builder()
                    .email(authorizedUserEmail)
                    .build();

            Customer customerForSave = Customer.builder()
                    .firstName("Saved First Name")
                    .lastName("Saved Last Name")
                    .phoneNumber(123456789)
                    .email("email1@example.com")
                    .build();

            ShippingAddress shippingAddressForSave = ShippingAddress.builder()
                    .city("Saved City")
                    .country("Saved Country")
                    .zipCode("12-345")
                    .streetAddress("Saved Street")
                    .build();

            Summary summaryForSave = Summary.builder()
                    .totalCartValue(BigDecimal.valueOf(2))
                    .totalQuantityOfProducts(2)
                    .build();

            OrderProduct orderProductForSave1 = OrderProduct.builder()
                    .name("Saved Product One")
                    .description("Saved Description One")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1))
                    .build();

            OrderProduct orderProductForSave2 = OrderProduct.builder()
                    .name("Saved Product Two")
                    .description("Saved Description Two")
                    .quantity(1)
                    .unitPrice(BigDecimal.valueOf(1)).build();

            List<OrderProduct> orderProductsForSave = new ArrayList<>();
            orderProductsForSave.add(orderProductForSave1);
            orderProductsForSave.add(orderProductForSave2);

            orderDtoForSave = OrderDto.builder()
                    .user(authorizedUserForSave)
                    .customer(customerForSave)
                    .shippingAddress(shippingAddressForSave)
                    .summary(summaryForSave)
                    .orderProducts(orderProductsForSave)
                    .build();
        }

        private void testUnauthorizedSave() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderDtoForSave)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(authorizedUserEmail)
        void shouldSaveOrderForAuthorizedUser() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderDtoForSave)))
                    .andExpect(status().isCreated()).andReturn();
            String content = result.getResponse().getContentAsString();
            JsonPath.parse(content).read("$.id", Long.class);
            Long id = JsonPath.parse(content).read("$.id", Long.class);
            assertNotNull(id);
        }

        @Test
        @WithMockUser(unauthorizedUserEmail)
        void shouldNotSaveOrderForUnauthorizedUser() throws Exception {
            testUnauthorizedSave();
        }

        @Test
        @WithAnonymousUser
        void shouldNotSaveOrderForAnonymousUser() throws Exception {
            testUnauthorizedSave();
        }
    }
}
