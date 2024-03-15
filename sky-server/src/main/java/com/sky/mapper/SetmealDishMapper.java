package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
        /**
         * 根据菜品ID查询套餐ID
         *
         * @param dishIds
         * @return
         */
        List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

        /**
         * 插入套餐中的菜品数据
         *
         * @param setmealDishes
         */
        void insertBatch(List<SetmealDish> setmealDishes);

        /**
         * 根据套餐ID删除套餐菜品
         *
         * @param setmealId
         */
        @Delete("delete from setmeal_dish where dish_id = #{setmealId}")
        void deleteBySetmealId(Long setmealId);
}
