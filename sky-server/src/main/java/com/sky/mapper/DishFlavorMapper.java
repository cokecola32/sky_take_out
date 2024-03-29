package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
        /**
         * 批量插入 口味数据
         *
         * @param dishFlavors
         */
        void insertBatch(List<DishFlavor> dishFlavors);

        /**
         * 根据菜品ID删除口味
         *
         * @param dishId
         */
        @Delete("delete from dish_flavor where dish_id = #{dishId}")
        void deleteByDishId(Long dishId);

        /**
         * 根据dishIds集合批量删除口味数据
         *
         * @param dishIds
         */
        void deleteByDishIds(List<Long> dishIds);

        /**
         * 根据菜品ID查询口味数据
         *
         * @param dishId
         * @return
         */
        @Select("select * from dish_flavor where dish_id = #{dishId}")
        List<DishFlavor> getByDishId(Long dishId);
}
