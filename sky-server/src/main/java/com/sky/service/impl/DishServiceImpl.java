package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.DishEnableFailedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {
        @Autowired
        private DishMapper dishMapper;
        @Autowired
        private DishFlavorMapper dishFlavorMapper;
        @Autowired
        private SetmealDishMapper setmealDishMapper;

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

        /**
         * 菜品分页查询
         *
         * @param dishPageQueryDTO
         * @return
         */
        @Override
        public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
                PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
                Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
                return new PageResult(page.getTotal(), page.getResult());
        }

        /**
         * 菜品批量删除
         *
         * @param ids
         */
        @Override
        @Transactional
        public void deleteBatch(List<Long> ids) {
                // 判断当前菜品是否可以删除（启售中？）
                for (Long id : ids) {
                        Dish dish = dishMapper.getById(id);
                        if (dish.getStatus() == StatusConstant.ENABLE) {
                                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
                        }
                }
                // 判断当前菜品是否被套餐关联
                List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
                if (setmealIds != null && setmealIds.size() > 0) {
                        throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
                }
//                // 删除菜品表中的数据
//                for (Long id : ids) {
//                        dishMapper.deleteById(id);
//                        // 删除菜品关联的口味数据
//                        dishFlavorMapper.deleteByDishId(id);
//                }
                // 根据Ids集合批量删除数据
                dishMapper.deleteByIds(ids);
                // 根据dishIds集合批量删除口味数据
                dishFlavorMapper.deleteByDishIds(ids);
        }

        /**
         * 根据ID查询菜品
         *
         * @param id
         * @return
         */
        @Override
        public DishVO getByIdWithFlavor(Long id) {
                // 根据ID查询菜品数据
                Dish dish = dishMapper.getById(id);
                // 根据菜品ID查询口味数据
                List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
                // 封装数据
                DishVO dishVO = new DishVO();
                BeanUtils.copyProperties(dish, dishVO);
                dishVO.setFlavors(dishFlavors);
                return dishVO;
        }

        /**
         * 修改菜品
         *
         * @param dishDTO
         */
        @Override
        @Transactional
        public void updateWithFlavor(DishDTO dishDTO) {
                Dish dish = new Dish();
                BeanUtils.copyProperties(dishDTO, dish);
                // 修改菜品基本信息
                dishMapper.update(dish);
                // 修改口味数据
                dishFlavorMapper.deleteByDishId(dishDTO.getId());
                List<DishFlavor> dishFlavors = dishDTO.getFlavors();
                if (dishFlavors != null && dishFlavors.size() > 0) {
                        dishFlavors.forEach(dishFlavor -> {
                                dishFlavor.setDishId(dishDTO.getId());
                        });
                        dishFlavorMapper.insertBatch(dishFlavors);
                }
        }

        /**
         * 根据分类ID查询菜品
         *
         * @param categoryId
         * @return
         */
        @Override
        public List<Dish> getByCategoryId(Long categoryId) {
                Dish dish = Dish.builder()
                        .categoryId(categoryId)
                        .status(StatusConstant.ENABLE)
                        .build();
                return dishMapper.getByCategoryId(dish);
        }

        /**
         * 修改菜品状态
         *
         * @param status
         * @param id
         */
        @Override
        public void switchDish(Integer status, Long id) {
                if (status == StatusConstant.DISABLE) {
                        // 菜品是否属于在售套餐
                        List<Setmeal> setmeals = setmealDishMapper.getByDishId(id);
                        if (setmeals != null && setmeals.size() > 0) {
                                setmeals.forEach(setmeal -> {
                                        if (setmeal.getStatus() == StatusConstant.ENABLE) {
                                                throw new DishEnableFailedException(MessageConstant.DISH_RELATED_BY_SETMEAL);
                                        }
                                });
                        }
                }
                Dish dish = Dish.builder()
                        .status(status)
                        .id(id)
                        .build();
                dishMapper.update(dish);
        }

        /**
         * 条件查询菜品和口味
         *
         * @param dish
         * @return
         */
        public List<DishVO> listWithFlavor(Dish dish) {
                List<Dish> dishList = dishMapper.getByCategoryId(dish);

                List<DishVO> dishVOList = new ArrayList<>();

                for (Dish d : dishList) {
                        DishVO dishVO = new DishVO();
                        BeanUtils.copyProperties(d, dishVO);

                        //根据菜品id查询对应的口味
                        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

                        dishVO.setFlavors(flavors);
                        dishVOList.add(dishVO);
                }

                return dishVOList;
        }
}
