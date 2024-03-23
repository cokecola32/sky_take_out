package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

        @Autowired
        private ShoppingCartMapper shoppingCartMapper;

        @Autowired
        private DishMapper dishMapper;

        @Autowired
        private SetmealMapper setmealMapper;

        /**
         * 添加购物车
         *
         * @param shoppingCartDTO
         */
        @Override
        public void addshoppingCart(ShoppingCartDTO shoppingCartDTO) {
                // 判断购物车中是否有物品
                ShoppingCart shoppingCart = new ShoppingCart();
                BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
                Long userId = BaseContext.getCurrentId();
                shoppingCart.setUserId(userId);
                List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
                // 如果有，对应物品+1
                if (list != null && list.size() > 0) {
                        ShoppingCart cart = list.get(0);
                        cart.setNumber(cart.getNumber() + 1);// update shopping_cart set number = ? where id = ?
                        shoppingCartMapper.updateNumberById(cart);
                } else {
                        // 如果没有，插入一条数据
                        Long dishId = shoppingCartDTO.getDishId();
                        if (dishId != null) {
                                Dish dish = dishMapper.getById(dishId);
                                shoppingCart.setName(dish.getName());
                                shoppingCart.setImage(dish.getImage());
                                shoppingCart.setAmount(dish.getPrice());
                        } else {
                                Long setmealId = shoppingCart.getSetmealId();
                                Setmeal setmeal = setmealMapper.getById(setmealId);
                                shoppingCart.setName(setmeal.getName());
                                shoppingCart.setImage(setmeal.getImage());
                                shoppingCart.setAmount(setmeal.getPrice());

                        }
                        shoppingCart.setNumber(1);
                        shoppingCart.setCreateTime(LocalDateTime.now());
                        shoppingCartMapper.insert(shoppingCart);
                }
        }

        /**
         * 查看购物车
         *
         * @return
         */
        @Override
        public List<ShoppingCart> showShoppingCart() {
                Long userId = BaseContext.getCurrentId();
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId)
                        .build();
                List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
                return list;
        }
}
