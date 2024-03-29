package com.pawelapps.ecommerce.dto;

import com.pawelapps.ecommerce.entity.CartProduct;
import com.pawelapps.ecommerce.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private User user;
    private List<CartProduct> cartProducts;
}
