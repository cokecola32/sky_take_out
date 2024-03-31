package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
        /**
         * 提交订单
         *
         * @param ordersSubmitDTO
         * @return
         */
        OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

        /**
         * 订单支付
         *
         * @param ordersPaymentDTO
         * @return
         */
        OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

        /**
         * 支付成功，修改订单状态
         *
         * @param outTradeNo
         */
        void paySuccess(String outTradeNo);

        /**
         * 查询用户订单
         *
         * @param page
         * @param pageSize
         * @param status
         * @return
         */
        PageResult pageQuery4User(Integer page, Integer pageSize, Integer status);

        /**
         * 条件查询订单
         *
         * @param ordersPageQueryDTO
         * @return
         */
        PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

        /**
         * 订单统计
         *
         * @return
         */
        OrderStatisticsVO statistics();

        /**
         * 订单详情
         *
         * @param id
         * @return
         */
        OrderVO details(Long id);

        /**
         * 确认订单
         *
         * @param
         */
        void comfirm(OrdersConfirmDTO ordersConfirmDTO);

        /**
         * 拒绝订单
         *
         * @param ordersConfirmDTO
         */
        void rejection(OrdersConfirmDTO ordersConfirmDTO);

        /**
         * 取消订单
         *
         * @param ordersCancelDTO
         */
        void cancel(OrdersCancelDTO ordersCancelDTO);

        /**
         * 发货
         *
         * @param id
         */
        void delivery(Long id);

        /**
         * 完成订单
         *
         * @param id
         */
        void complete(Long id);

        /**
         * 用户取消订单
         *
         * @param id
         */
        void userCancelById(Long id);

        /**
         * 再来一单
         *
         * @param id
         */
        void repetition(Long id);
}
