package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
                List<BigDecimal> turnoverList = new ArrayList<>();
                for (LocalDate date : dateList) {
                        LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
                        LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
                        Map map = new HashMap<>();
                        map.put("beginTime", beginTime);
                        map.put("endTime", endTime);
                        map.put("status", Orders.COMPLETED);
                        BigDecimal turnover = orderMapper.sumByMap(map);
                        turnover = turnover == null ? BigDecimal.ZERO : turnover;
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
}
