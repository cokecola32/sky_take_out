package com.sky.service.impl;

import com.alibaba.druid.sql.parser.NotAllowCommentException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
        @Autowired
        private SetmealMapper setmealMapper;
        @Autowired
        private SetmealDishMapper setmealDishMapper;
        @Autowired
        private DishMapper dishMapper;

        /**
         * 新增菜品，同时保存菜品和套餐的关系
         *
         * @param setmealDTO
         */
        @Override
        @Transactional
        public void saveWithDishes(SetmealDTO setmealDTO) {
                Setmeal setmeal = new Setmeal();
                BeanUtils.copyProperties(setmealDTO, setmeal);
                // 向套餐表中插入数据
                setmealMapper.insert(setmeal);
                //获取生成套餐的ID
                Long setmealId = setmeal.getId();
                List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
                setmealDishes.forEach(setmealDish -> {
                        setmealDish.setSetmealId(setmealId);
                });
                // 插入套餐中的菜品数据
                setmealDishMapper.insertBatch(setmealDishes);
        }

        /**
         * 套餐分页查询
         *
         * @param setmealPageQueryDTO
         * @return
         */
        @Override
        public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
                PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
                Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
                return new PageResult(page.getTotal(), page.getResult());
        }

        /**
         * 批量删除套餐
         *
         * @param ids
         */
        @Override
        @Transactional
        public void deleteBatch(List<Long> ids) {
                // 启售套餐不可删除
                ids.forEach(id -> {
                        Setmeal setmeal = setmealMapper.getById(id);
                        if (setmeal.getStatus() == StatusConstant.ENABLE) {
                                throw new NotAllowCommentException(MessageConstant.SETMEAL_ON_SALE);
                        }
                });
                ids.forEach(setmealId -> {
                        // 删除套餐
                        setmealMapper.deleteById(setmealId);
                        // 删除套餐菜品
                        setmealDishMapper.deleteBySetmealId(setmealId);
                });
        }

        /**
         * 根据ID查询套餐和套餐菜品关系
         *
         * @param id
         * @return
         */
        @Override
        public SetmealVO getById(Long id) {
                Setmeal setmeal = setmealMapper.getById(id);
                List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
                SetmealVO setmealVO = new SetmealVO();
                BeanUtils.copyProperties(setmeal, setmealVO);
                setmealVO.setSetmealDishes(setmealDishes);
                return setmealVO;
        }

        /**
         * 更新套餐
         *
         * @param setmealDTO
         */
        @Override
        @Transactional
        public void update(SetmealDTO setmealDTO) {
                // 修改套餐
                Setmeal setmeal = new Setmeal();
                BeanUtils.copyProperties(setmealDTO, setmeal);
                setmealMapper.update(setmeal);
                // 修改套餐菜品
                Long setmealId = setmealDTO.getId();
                setmealDishMapper.deleteBySetmealId(setmealId);
                List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
                setmealDishes.forEach(setmealDish -> {
                        setmealDish.setSetmealId(setmealId);
                });
                setmealDishMapper.insertBatch(setmealDishes);
        }

        /**
         * 更改套餐状态
         *
         * @param status
         * @param id
         */
        @Override
        public void switchStatus(Integer status, Long id) {
                if (status == StatusConstant.ENABLE) {
                        List<Dish> dishes = dishMapper.getBySetmealId(id);
                        if (dishes != null && dishes.size() > 0) {
                                dishes.forEach(dish -> {
                                        if (dish.getStatus() == StatusConstant.DISABLE) {
                                                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                                        }
                                });
                        }
                }
                Setmeal setmeal = Setmeal.builder()
                        .id(id)
                        .status(status)
                        .build();
                setmealMapper.update(setmeal);
        }

        /**
         * 条件查询
         *
         * @param setmeal
         * @return
         */
        public List<Setmeal> list(Setmeal setmeal) {
                List<Setmeal> list = setmealMapper.list(setmeal);
                return list;
        }

        /**
         * 根据id查询菜品选项
         *
         * @param id
         * @return
         */
        public List<DishItemVO> getDishItemById(Long id) {
                return setmealMapper.getDishItemBySetmealId(id);
        }
}
