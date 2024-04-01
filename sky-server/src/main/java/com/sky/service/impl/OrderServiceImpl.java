package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

        @Autowired
        private OrderMapper orderMapper;
        @Autowired
        private OrderDetailMapper orderDetailMapper;
        @Autowired
        private AddressBookMapper addressBookMapper;
        @Autowired
        private ShoppingCartMapper shoppingCartMapper;
        @Autowired
        private UserMapper userMapper;
        @Autowired
        private WebSocketServer webSocketServer;
//        @Autowired
//        private WeChatPayUtil weChatPayUtil;

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
                if (shoppingCartList == null || shoppingCartList.isEmpty()) {
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

        /**
         * 订单支付
         *
         * @param ordersPaymentDTO
         * @return
         */
        public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
                // 当前登录用户id
                Long userId = BaseContext.getCurrentId();
                User user = userMapper.getById(userId);

//                //调用微信支付接口，生成预支付交易单
//                JSONObject jsonObject = weChatPayUtil.pay(
//                        ordersPaymentDTO.getOrderNumber(), //商户订单号
//                        new BigDecimal(0.01), //支付金额，单位 元
//                        "苍穹外卖订单", //商品描述
//                        user.getOpenid() //微信用户的openid
//                );
                //模拟微信支付，跳过支付流程
                JSONObject jsonObject = new JSONObject();
                if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
                        throw new OrderBusinessException("该订单已支付");
                }

                OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
                vo.setPackageStr(jsonObject.getString("package"));
                return vo;
        }

        /**
         * 支付成功，修改订单状态
         *
         * @param outTradeNo
         */
        public void paySuccess(String outTradeNo) {

                // 根据订单号查询订单
                Orders ordersDB = orderMapper.getByNumber(outTradeNo);

                // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
                Orders orders = Orders.builder()
                        .id(ordersDB.getId())
                        .status(Orders.TO_BE_CONFIRMED)
                        .payStatus(Orders.PAID)
                        .checkoutTime(LocalDateTime.now())
                        .build();

                orderMapper.update(orders);

                // 通过websocket向客户端浏览器推送消息
                Map map = new HashMap();
                map.put("type", 1);
                map.put("orderId", ordersDB.getId());
                map.put("content", "订单号:" + outTradeNo);
                String json = JSON.toJSONString(map);
                webSocketServer.sendToAllClient(json);
        }

        /**
         * 查询用户订单
         *
         * @param pageNum
         * @param pageSize
         * @param status
         * @return
         */
        @Override
        public PageResult pageQuery4User(Integer pageNum, Integer pageSize, Integer status) {
                // 设置分页
                PageHelper.startPage(pageNum, pageSize);
                OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
                ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
                ordersPageQueryDTO.setStatus(status);
                // 分页条件查询
                Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
                List<OrderVO> list = new ArrayList();
                // 查询出订单明细，并封装入OrderVO进行响应
                if (page != null && page.getTotal() > 0) {
                        for (Orders orders : page) {
                                Long orderId = orders.getId();// 订单id
                                // 查询订单明细
                                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                                OrderVO orderVO = new OrderVO();
                                BeanUtils.copyProperties(orders, orderVO);
                                orderVO.setOrderDetailList(orderDetails);
                                list.add(orderVO);
                        }
                }
                return new PageResult(page.getTotal(), list);
        }

        /**
         * 用户取消订单
         *
         * @param id
         */
        @Override
        public void userCancelById(Long id) {
                // 根据订单id查询订单
                Orders ordersDB = orderMapper.getById(id);
                // 检验订单是否存在
                if (ordersDB == null) {
                        throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
                }
                // 检验订单状态是否为可以取消
                if (!ordersDB.getStatus().equals(Orders.PAID) && !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }
                // 根据订单id更新订单的状态
                Orders orders = new Orders();
                orders.setId(id);
                // 如果订单状态为已支付，则需要退款
                if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        // 支付状态修改为退款
                        orders.setPayStatus(Orders.REFUND);
                }
                // 更新订单状态为取消
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason("用户取消");
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
        }

        /**
         * 再来一单
         *
         * @param id
         */
        @Override
        public void repetition(Long id) {
                // 查询当前用户id
                Long userId = BaseContext.getCurrentId();
                // 根据订单id查询订单详情
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
                // 将订单详情转换为购物车对象
                List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
                        ShoppingCart shoppingCart = new ShoppingCart();
                        // 复制属性，不复制id
                        BeanUtils.copyProperties(x, shoppingCart, "id");
                        shoppingCart.setUserId(userId);
                        shoppingCart.setDishId(x.getDishId());
                        return shoppingCart;
                }).collect(Collectors.toList());
                // 批量插入购物车
                shoppingCartMapper.insertBatch(shoppingCartList);
        }

        /**
         * 用户催单
         *
         * @param id
         */
        @Override
        public void reminder(Long id) {
                // 根据订单id查询订单
                Orders ordersDB = orderMapper.getById(id);
                // 验证订单是否存在
                if (ordersDB == null && !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }
                // 通过websocket向客户端浏览器推送消息
                Map map = new HashMap();
                map.put("type", 2);
                map.put("orderId", id);
                map.put("content", "订单号:" + ordersDB.getNumber());
                webSocketServer.sendToAllClient(JSON.toJSONString(map));
        }

        /**
         * 条件查询订单
         *
         * @param ordersPageQueryDTO
         * @return
         */
        @Override
        public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
                //设置分页
                PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
                Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
                //部分订单状态，将Orders转换为OrderVO
                List<OrderVO> orderVOList = getOrderVOList(page);
                return new PageResult(page.getTotal(), orderVOList);
        }

        private List<OrderVO> getOrderVOList(Page<Orders> page) {
                //需要返回订单菜品信息，自定义OrderVO响应结果
                List<OrderVO> orderVOList = new ArrayList<>();
                List<Orders> ordersList = page.getResult();
                for (Orders orders : ordersList) {
                        OrderVO orderVO = new OrderVO();
                        BeanUtils.copyProperties(orders, orderVO);
                        //根据订单id查询订单明细
                        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
                        orderVO.setOrderDetailList(orderDetailList);
                        orderVOList.add(orderVO);
                }
                return orderVOList;
        }

        /**
         * 获取订单菜品信息
         *
         * @param orders
         * @return
         */
        private String getOrderDishesStr(Orders orders) {
                // 查询订单菜品详情信息（订单中的菜品和数量）
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

                // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
                List<String> orderDishList = orderDetailList.stream().map(x -> {
                        String orderDish = x.getName() + "*" + x.getNumber() + ";";
                        return orderDish;
                }).collect(Collectors.toList());

                // 将该订单对应的所有菜品信息拼接在一起
                return String.join("", orderDishList);
        }

        /**
         * 订单统计
         *
         * @return
         */
        @Override
        public OrderStatisticsVO statistics() {
                // 根据状态，分别查询出待接单、待派送、派送中的订单数量
                Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
                Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
                Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
                // 封装成OrderStatisticsVO对象
                OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
                orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
                orderStatisticsVO.setConfirmed(confirmed);
                orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
                return orderStatisticsVO;
        }

        /**
         * 订单详情
         *
         * @param id
         * @return
         */
        @Override
        public OrderVO details(Long id) {
                //根据订单id查询订单信息
                Orders orders = orderMapper.getById(id);
                //根据订单id查询订单明细
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
                // 封装成OrderVO对象
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                return orderVO;
        }

        /**
         * 确认订单
         *
         * @param ordersConfirmDTO
         */
        @Override
        public void comfirm(OrdersConfirmDTO ordersConfirmDTO) {
                Orders orders = Orders.builder()
                        .id(ordersConfirmDTO.getId())
                        .status(Orders.CONFIRMED)
                        .build();
                orderMapper.update(orders);
        }


        /**
         * 拒绝订单
         *
         * @param ordersConfirmDTO
         */
        @Override
        public void rejection(OrdersConfirmDTO ordersConfirmDTO) {
                // 根据订单id更新订单的状态
                Orders ordersDB = orderMapper.getById(ordersConfirmDTO.getId());
                // 只有订单状态为待接单，则可以拒绝订单
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }
//                //支付状态
//                Integer payStatus = ordersDB.getPayStatus();
//                if (payStatus == Orders.PAID) {
//                        //用户已支付，需要退款
//                        String refund = weChatPayUtil.refund(
//                                ordersDB.getNumber(),
//                                ordersDB.getNumber(),
//                                new BigDecimal(0.01),
//                                new BigDecimal(0.01));
//                        log.info("申请退款：{}", refund);
//                }
                // 根据订单id更新订单的状态
                Orders orders = new Orders();
                orders.setId(ordersDB.getId());
                orders.setStatus(Orders.CANCELLED);
                orders.setRejectionReason(ordersDB.getRejectionReason());
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
        }

        /**
         * 取消订单
         *
         * @param ordersCancelDTO
         */
        @Override
        public void cancel(OrdersCancelDTO ordersCancelDTO) {
                // 根据id查询订单
                Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());
//                //支付状态
//                Integer payStatus = ordersDB.getPayStatus();
//                if (payStatus == 1) {
//                        //用户已支付，需要退款
//                        String refund = weChatPayUtil.refund(
//                                ordersDB.getNumber(),
//                                ordersDB.getNumber(),
//                                new BigDecimal(0.01),
//                                new BigDecimal(0.01));
//                        log.info("申请退款：{}", refund);
//                }
                // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
                Orders orders = new Orders();
                orders.setId(ordersCancelDTO.getId());
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelReason(ordersCancelDTO.getCancelReason());
                orders.setCancelTime(LocalDateTime.now());
                orderMapper.update(orders);
        }

        /**
         * 派送订单
         *
         * @param id
         */
        @Override
        public void delivery(Long id) {
                // 根据订单id更新订单的状态
                Orders ordersDB = orderMapper.getById(id);
                // 验证订单是否存在，订单状态是否为待派送
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }
                Orders orders = new Orders();
                orders.setId(id);
                // 更新订单状态为派送中
                orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
                orderMapper.update(orders);
        }

        /**
         * 完成订单
         *
         * @param id
         */
        @Override
        public void complete(Long id) {
                // 根据订单id查询订单
                Orders ordersDB = orderMapper.getById(id);
                // 验证订单是否存在，订单状态是否为派送中
                if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
                        throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
                }
                // 根据订单id更新订单的状态
                Orders orders = new Orders();
                orders.setId(id);
                orders.setDeliveryTime(LocalDateTime.now());
                orders.setStatus(Orders.COMPLETED);
        }
}
