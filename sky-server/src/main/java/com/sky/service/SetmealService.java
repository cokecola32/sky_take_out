package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
        /**
         * 新增套餐
         *
         * @param setmealDTO
         */
        void saveWithDishes(SetmealDTO setmealDTO);

        /**
         * 套餐分页查询
         *
         * @param setmealPageQueryDTO
         * @return
         */
        PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

        /**
         * 批量删除套餐
         *
         * @param ids
         */
        void deleteBatch(List<Long> ids);

        /**
         * 根据ID查询套餐
         *
         * @param id
         * @return
         */
        SetmealVO getById(Long id);

        /**
         * 更新套餐
         *
         * @param setmealDTO
         */
        void update(SetmealDTO setmealDTO);

        /**
         * 更改套餐状态
         *
         * @param status
         * @param id
         */
        void switchStatus(Integer status, Long id);
}
