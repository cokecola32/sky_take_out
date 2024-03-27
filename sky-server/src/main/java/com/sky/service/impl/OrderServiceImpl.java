package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;

import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

        @Autowired
        private OrderMapper orderMapper;
        @Autowired
        private OrderDetailMapper orderDetailMapper;
        @Autowired
        private AddressBookMapper addressBookMapper;
        @Autowired
        private ShoppingCartMapper shoppingCartMapper;
        /**
         * 提交订单
         *
         * @param ordersSubmitDTO
         * @return
         */
        @Override
        @Transactional
        public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
                //判断地址簿是否为空     为空则返回错误
                AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
                if (addressBook == null) {
                        throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
                }
                //判断购物车是否为空     为空则返回错误
                Long userId = BaseContext.getCurrentId();
                ShoppingCart shoppingCart = new ShoppingCart();
                shoppingCart.setUserId(userId);
                List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
                if (shoppingCartList == null || shoppingCartList.size() == 0) {
                        throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
                }
                //生成1条订单
                Orders orders = new Orders();
                BeanUtils.copyProperties(ordersSubmitDTO, orders);
                orders.setOrderTime(LocalDateTime.now());
                orders.setPayStatus(Orders.UN_PAID);
                orders.setStatus(Orders.PENDING_PAYMENT);
                orders.setNumber(String.valueOf(System.currentTimeMillis()));
                orders.setPhone(addressBook.getPhone());
                orders.setConsignee(addressBook.getConsignee());
                orders.setUserId(userId);
                orderMapper.insert(orders);
                //生成订单详情
                List<OrderDetail> orderDetailList = new ArrayList<>();
                for (ShoppingCart cart : shoppingCartList) {
                        OrderDetail orderDetail = new OrderDetail();
                        BeanUtils.copyProperties(cart, orderDetail);
                        orderDetail.setOrderId(orders.getId());
                        orderDetailList.add(orderDetail);
                }
                orderDetailMapper.insertBatch(orderDetailList);
                //清空购物车
                shoppingCartMapper.deleteByUserId(userId);
                //返回订单信息
                OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                        .id(orders.getId())
                        .orderTime(orders.getOrderTime())
                        .orderNumber(orders.getNumber())
                        .orderAmount(orders.getAmount())
                        .build();
                return orderSubmitVO;
        }
}
