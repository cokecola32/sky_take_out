package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
        @Autowired
        private DishMapper dishMapper;
        @Autowired
        private DishFlavorMapper dishFlavorMapper;

        /**
         * 新增菜品和 对应的口味
         *
         * @param dishDTO
         */
        @Transactional
        @Override
        public void saveWithFlavor(DishDTO dishDTO) {
                Dish dish = new Dish();
                BeanUtils.copyProperties(dishDTO, dish);
                // 向菜品表中插入1条数据
                dishMapper.insert(dish);
                // 获取INSERT语句返回的主键值
                Long dishId = dish.getId();
                // 向口味表重插入n条数据
                List<DishFlavor> dishFlavors = dishDTO.getFlavors();
                if (dishFlavors != null && dishFlavors.size() > 0) {
                        dishFlavors.forEach(dishFlavor -> {
                                dishFlavor.setDishId(dishId);
                        });
                        dishFlavorMapper.insertBatch(dishFlavors);
                }
        }
}
