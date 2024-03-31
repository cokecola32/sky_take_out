package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Slf4j
@RequestMapping("/admin/order")
@Api(tags = "B端订单相关接口")
public class OrderController {

        @Autowired
        private OrderService orderService;

        /**
         * 订单查询
         *
         * @param ordersPageQueryDTO
         * @return
         */
        @GetMapping("/conditionSearch")
        @ApiOperation("订单查询")
        public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
                log.info("订单查询:{}", ordersPageQueryDTO);
                PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
                return Result.success(pageResult);
        }

        /**
         * 订单统计
         *
         * @return
         */
        @GetMapping("/statistics")
        @ApiOperation("订单统计")
        public Result<OrderStatisticsVO> statistics() {
                OrderStatisticsVO orderStatisticsVO = orderService.statistics();
                log.info("订单统计:{}", orderStatisticsVO);
                return Result.success(orderStatisticsVO);
        }

        /**
         * 订单详情
         *
         * @param id
         * @return
         */
        @GetMapping("/details/{id}")
        @ApiOperation("订单详情")
        public Result<OrderVO> details(@PathVariable Long id) {
                log.info("订单详情:{}", id);
                OrderVO orderVO = orderService.details(id);
                return Result.success(orderVO);
        }
        /**
         * 确认订单
         *
         * @param ordersConfirmDTO
         * @return
         */
        @PutMapping("/confirm")
        @ApiOperation("确认订单")
        public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
                log.info("确认订单:{}", ordersConfirmDTO);
                orderService.comfirm(ordersConfirmDTO);
                return Result.success();
        }
        /**
         * 拒绝订单
         *
         * @param ordersConfirmDTO
         * @return
         */
        @PutMapping("/rejection")
        @ApiOperation("拒绝订单")
        public Result rejection(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
                log.info("拒绝订单:{}", ordersConfirmDTO);
                orderService.rejection(ordersConfirmDTO);
                return Result.success();
        }
        /**
         * 取消订单
         *
         * @param ordersCancelDTO
         * @return
         */
        @PutMapping("/cancel")
        @ApiOperation("取消订单")
        public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
                log.info("取消订单:{}", ordersCancelDTO);
                orderService.cancel(ordersCancelDTO);
                return Result.success();
        }
        /**
         * 派送订单
         *
         * @param id
         * @return
         */
        @PutMapping("/delivery/{id}")
        @ApiOperation("派送订单")
        public Result delivery(@PathVariable("id") Long id) {
                log.info("派送订单:{}", id);
                orderService.delivery(id);
                return Result.success();
        }
        /**
         * 完成订单
         *
         * @param id
         * @return
         */
        @PutMapping("/complete/{id}")
        @ApiOperation("完成订单")
        public Result complete(@PathVariable("id") Long id) {
                log.info("完成订单:{}", id);
                orderService.complete(id);
                return Result.success();
        }
}
