package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

        @Autowired
        private OrderMapper orderMapper;

        @Autowired
        private UserMapper userMapper;

        /**
         * 营业额统计
         *
         * @param begin
         * @param end
         * @return
         */
        @Override
        public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(begin);
                while (!begin.equals(end)) {
                        begin = begin.plusDays(1);
                        dateList.add(begin);
                }
                List<Double> turnoverList = new ArrayList<>();
                for (LocalDate date : dateList) {
                        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                        Map map = new HashMap<>();
                        map.put("beginTime", beginTime);
                        map.put("endTime", endTime);
                        map.put("status", Orders.COMPLETED);
                        Double turnover = orderMapper.sumByMap(map);
                        turnover = turnover == null ? 0.0 : turnover;
                        turnoverList.add(turnover);
                }
                return TurnoverReportVO.builder()
                        .dateList(StringUtils.join(dateList, ","))
                        .turnoverList(StringUtils.join(turnoverList, ","))
                        .build();
        }

        /**
         * 用户统计
         *
         * @param begin
         * @param end
         * @return
         */
        @Override
        public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(begin);
                while (!begin.equals(end)) {
                        begin = begin.plusDays(1);
                        dateList.add(begin);
                }
                List<Integer> newUserList = new ArrayList<>();
                List<Integer> totalUserList = new ArrayList<>();
                for (LocalDate date : dateList) {
                        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                        Map map = new HashMap<>();
                        map.put("endTime", endTime);
                        // 总用户数
                        Integer totalUser = userMapper.countByMap(map);
                        map.put("beginTime", beginTime);
                        // 新用户数
                        Integer newUser = userMapper.countByMap(map);
                        newUserList.add(newUser);
                        totalUserList.add(totalUser);
                }
                // 封装进VO对象
                return UserReportVO.builder()
                        .dateList(StringUtils.join(dateList, ","))
                        .newUserList(StringUtils.join(newUserList, ","))
                        .totalUserList(StringUtils.join(totalUserList, ","))
                        .build();
        }

        /**
         * 订单统计
         *
         * @param begin
         * @param end
         * @return
         */
        @Override
        public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
                List<LocalDate> dateList = new ArrayList<>();
                dateList.add(begin);
                while (!begin.equals(end)) {
                        begin = begin.plusDays(1);
                        dateList.add(begin);
                }
                List<Integer> orderList = new ArrayList<>();
                List<Integer> completedList = new ArrayList<>();
                for (LocalDate date : dateList) {
                        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                        // 订单总数
                        Integer orderCount = getOrderCount(beginTime, endTime, null);
                        // 有效订单数
                        Integer completedCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);
                        orderList.add(orderCount);
                        completedList.add(completedCount);
                }
                // 订单完成率
                Integer totalOrderCount = orderList.stream().reduce(Integer::sum).get();
                Integer totalCompletedCount = completedList.stream().reduce(Integer::sum).get();
                Double completedRate = totalOrderCount == 0 ? 0 : totalCompletedCount * 1.0 / totalOrderCount;
                // 封装进VO对象
                return OrderReportVO.builder()
                        .dateList(StringUtils.join(dateList, ","))
                        .orderCountList(StringUtils.join(orderList, ","))
                        .validOrderCountList(StringUtils.join(completedList, ","))
                        .totalOrderCount(totalOrderCount)
                        .validOrderCount(totalCompletedCount)
                        .orderCompletionRate(completedRate)
                        .build();
        }

        /**
         * 条件订单统计
         *
         * @param beginTime
         * @param endTime
         * @param status
         * @return
         */
        private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
                Map map = new HashMap<>();
                map.put("beginTime", beginTime);
                map.put("endTime", endTime);
                map.put("status", status);
                return orderMapper.countByMap(map);
        }

        /**
         * 销量前十统计
         *
         * @param begin
         * @param end
         * @return
         */
        @Override
        public SalesTop10ReportVO getSalesTop10Statistics(LocalDate begin, LocalDate end) {
                LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
                List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
                List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
                String nameList = StringUtils.join(names, ",");

                List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
                String numberList = StringUtils.join(numbers, ",");

                return SalesTop10ReportVO.builder()
                        .nameList(nameList)
                        .numberList(numberList)
                        .build();
        }
}
