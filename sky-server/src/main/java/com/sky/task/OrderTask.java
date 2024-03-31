package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，处理订单状态
 */
@Component
@Slf4j
public class OrderTask {

        @Autowired
        private OrderMapper orderMapper;

        /**
         * 处理超时订单
         */
        @Scheduled(cron = "0 * * * * ?") // 每分钟触发一次
        public void handleTimeoutOrder() {
                log.info("定时处理超市订单:{}", LocalDateTime.now());
                LocalDateTime time = LocalDateTime.now().minusMinutes(15);
                List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
                if (!ordersList.isEmpty()) {
                        ordersList.forEach(orders -> {
                                orders.setStatus(Orders.CANCELLED);
                                orders.setCancelReason("超时未支付，自动取消");
                                orders.setCancelTime(LocalDateTime.now());
                                orderMapper.update(orders);
                        });
                }
        }

        /**
         * 处理一直处于派送中的订单
         */
        @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点触发
        public void handleDeliveryOrder() {
                log.info("定时处理派送中订单:{}", LocalDateTime.now());
                LocalDateTime time = LocalDateTime.now().minusHours(1);
                List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);
                if (!ordersList.isEmpty()) {
                        ordersList.forEach(orders -> {
                                orders.setStatus(Orders.COMPLETED);
                                orderMapper.update(orders);
                        });
                }
        }
}
