package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "C端订单相关接口")
@Slf4j
public class OrderController {

        @Autowired
        private OrderService orderService;

        /**
         * 提交订单
         *
         * @param ordersSubmitDTO
         * @return
         */
        @PostMapping("/submit")
        @ApiOperation("提交订单")
        public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
                log.info("提交订单:{}", ordersSubmitDTO);
                OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
                return Result.success(orderSubmitVO);
        }

        /**
         * 订单支付
         *
         * @param ordersPaymentDTO
         * @return
         */
        @PutMapping("/payment")
        @ApiOperation("订单支付")
        public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
                log.info("订单支付：{}", ordersPaymentDTO);
                OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
                log.info("生成预支付交易单：{}", orderPaymentVO);
                //模拟交易成功，修改数据库订单状态
                orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
                log.info("模拟交易成功:{}", ordersPaymentDTO.getOrderNumber());
                return Result.success(orderPaymentVO);
        }

        /**
         * 历史订单查询
         *
         * @param page
         * @param pageSize
         * @param status
         * @return
         */
        @GetMapping("/historyOrders")
        @ApiOperation("历史订单查询")
        public Result<PageResult> page(Integer page, Integer pageSize, Integer status) {
                log.info("历史订单查询：page={},pageSize={},status={}", page, pageSize, status);
                PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
                return Result.success(pageResult);
        }

        /**
         * 订单详情
         *
         * @param id
         * @return
         */
        @GetMapping("/orderDetail/{id}")
        @ApiOperation("订单详情")
        public Result<OrderVO> details(@PathVariable("id") Long id) {
                log.info("订单详情:{}", id);
                OrderVO orderVO = orderService.details(id);
                return Result.success(orderVO);
        }

        /**
         * 取消订单
         *
         * @param id
         * @return
         */
        @PutMapping("/cancel/{id}")
        @ApiOperation("取消订单")
        public Result cancel(@PathVariable("id") Long id) {
                log.info("取消订单:{}", id);
                orderService.userCancelById(id);
                return Result.success();
        }

        /**
         * 再来一单
         *
         * @param id
         * @return
         */
        @PostMapping("/repetition/{id}")
        @ApiOperation("再来一单")
        public Result repetition(@PathVariable("id") Long id) {
                log.info("取消订单:{}", id);
                orderService.repetition(id);
                return Result.success();
        }
}
