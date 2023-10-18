package com.pawelapps.ecommerce.service;

import com.pawelapps.ecommerce.dao.CartProductRepository;
import com.pawelapps.ecommerce.dao.CartRepository;
import com.pawelapps.ecommerce.dao.ProductRepository;
import com.pawelapps.ecommerce.dto.CartProductDto;
import com.pawelapps.ecommerce.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class CartProductServiceTest {

    @MockBean
    private CartProductRepository cartProductRepository;

    @MockBean
    private CartRepository cartRepository;

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private CartProductService cartProductService;

    private final String userEmail = "test@email.com";

    private User user;

    private Cart cart;

    private ProductCategory productCategory;

    private Product product1;

    private Product product2;

    private CartProductDto cartProductDto1;

    private CartProductDto cartProductDto2;

    private CartProduct cartProduct1;

    private CartProduct cartProduct2;

    private List<CartProduct> cartProducts;

    @BeforeEach
    void setUp() {
        cartProducts = new ArrayList<>();

        user = User.builder().email(userEmail).build();

        cart = Cart.builder().user(user).build();

        productCategory = ProductCategory.builder().categoryName("Test Category").build();

        product1 = Product.builder()
                .productCategory(productCategory)
                .id(1L)
                .sku("1")
                .name("Test Product 1")
                .description("Test Description 1")
                .unitsInStock(10)
                .unitPrice(BigDecimal.valueOf(1)).build();

        product2 = Product.builder()
                .productCategory(productCategory)
                .id(2l)
                .sku("2")
                .name("Test Product 2")
                .description("Test Description 2")
                .unitsInStock(5)
                .unitPrice(BigDecimal.valueOf(1)).build();

        cartProductDto1 = CartProductDto.builder()
                .cartProductId(1L)
                .product(product1)
                .cartProductId(product1.getId())
                .name(product1.getName())
                .description(product1.getDescription())
                .quantity(1)
                .build();

        cartProductDto2 = CartProductDto.builder()
                .cartProductId(2L)
                .product(product2)
                .cartProductId(product2.getId())
                .name(product2.getName())
                .description(product2.getDescription())
                .quantity(1)
                .build();

        cartProduct1 = CartProduct.builder()
                .cartProductId(cartProductDto1.getCartProductId())
                .product(product1)
                .cartProductId(cartProductDto1.getCartProductId())
                .name(cartProductDto1.getName())
                .description(cartProductDto1.getDescription())
                .quantity(cartProductDto1.getQuantity())
                .build();

        cartProduct2 = CartProduct.builder()
                .cartProductId(cartProductDto2.getCartProductId())
                .product(product2)
                .cartProductId(cartProductDto2.getCartProductId())
                .name(cartProductDto2.getName())
                .description(cartProductDto2.getDescription())
                .quantity(cartProductDto2.getQuantity())
                .build();

        cartProducts.add(cartProduct1);
        cartProducts.add(cartProduct2);
    }

    @Nested
    class SaveCartProductTests {

        @Test
        void shouldSaveCartProductWhenCartIsPresent() {
            when(cartRepository.findByUserEmail(userEmail)).thenReturn(cart);
            when(cartProductRepository.save(any(CartProduct.class))).thenReturn(cartProduct1);

            CartProductDto savedCartProductDto = cartProductService.saveCartProductToCart(cartProductDto1, userEmail);

            assertNotNull(savedCartProductDto);
            assertNotNull(savedCartProductDto.getCartProductId());
            assertEquals(cart, savedCartProductDto.getCart());

            verify(cartRepository).findByUserEmail(userEmail);
            verify(cartProductRepository).save(any(CartProduct.class));
        }

        @Test
        void shouldSaveCartProductWhenCartIsNotPresent() {
            when(cartRepository.findByUserEmail(userEmail)).thenReturn(null);
            when(cartProductRepository.save(any(CartProduct.class))).thenReturn(cartProduct1);

            CartProductDto savedCartProductDto = cartProductService.saveCartProductToCart(cartProductDto1, userEmail);

            assertNotNull(savedCartProductDto);
            assertNotNull(savedCartProductDto.getCartProductId());
            assertNotNull(savedCartProductDto.getCart());

            verify(cartRepository).findByUserEmail(userEmail);
            verify(cartProductRepository).save(any(CartProduct.class));
        }
    }

    @Nested
    class IncreaseCartProductQuantityByOneTests {

        @Test
        void shouldIncreaseQuantityWhenProductHasEnoughUnitsInStock() {
            Long cartProductId = cartProduct1.getCartProductId();

            product1.setUnitsInStock(10);
            cartProduct1.setQuantity(9);
            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(cartProduct1));
            when(productRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(product1));
            when(cartProductRepository.increaseCartProductQuantityByOne(cartProductId)).thenReturn(1);

            Integer updatedRows = cartProductService.increaseCartProductQuantityByOne(cartProductId);

            assertEquals(1, updatedRows);
        }

        @Test
        void shouldThrowExceptionWhenProductHasNotGotEnoughUnitsInStock() {
            Long cartProductId = cartProduct1.getCartProductId();

            product1.setUnitsInStock(10);
            cartProduct1.setQuantity(10);
            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(cartProduct1));
            when(productRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(product1));

            assertThrows(IllegalStateException.class, () -> cartProductService.increaseCartProductQuantityByOne(cartProductId));
        }
    }

    @Nested
    class DecreaseCartProductQuantityByOneTests {

        @Test
        void shouldDecreaseWhenQuantityIsHigherThanZero() {
            Long cartProductId = cartProduct1.getCartProductId();

            cartProduct1.setQuantity(10);
            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(cartProduct1));
            when(cartProductRepository.decreaseCartProductQuantityByOne(cartProductId)).thenReturn(1);

            Integer updatedRows = cartProductService.decreaseCartProductQuantityByOne(cartProductId);

            assertEquals(1, updatedRows);
        }

        @Test
        void shouldThrowExceptionWhenQuantityIsLowerThanOne() {
            Long cartProductId = cartProduct1.getCartProductId();

            cartProduct1.setQuantity(0);
            when(cartProductRepository.findById(cartProductId)).thenReturn(Optional.ofNullable(cartProduct1));

            assertThrows(IllegalStateException.class, () -> cartProductService.decreaseCartProductQuantityByOne(cartProductId));
        }
    }

    @Test
    void shouldFindCartProductsByUserEmail() {
        when(cartProductRepository.findCartProductsByUserEmail(userEmail)).thenReturn(cartProducts);

        List<CartProduct> receivedCartProducts = cartProductService.findCartProductsByUserEmail(userEmail);

        assertEquals(2, receivedCartProducts.size());

        verify(cartProductRepository).findCartProductsByUserEmail(userEmail);
    }

    @Test
    void shouldUpdateCartProduct() {

    }


    @Nested
    class GetCartProductByIdTests {

        @Test
        void shouldGetCartProductById() {

        }

        @Test
        void shouldThrowNotFoundExceptionWhenCartProductDoesNotExist() {

        }
    }


}
