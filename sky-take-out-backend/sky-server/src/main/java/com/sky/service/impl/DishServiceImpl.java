package com.sky.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    // @Autowired
// private SetmealDishMapper setmealDishMapper;
// @Autowired
// private SetmealMapper setmealMapper;


    /**
     * 新增菜品和口味数据
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 向菜品表插入一条数据
        dishMapper.insert(dish);

        //获取insert语句生成的主键值
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            // 遍历flavors列表,为每个口味数据添加dishId
            for(DishFlavor df : flavors){
                df.setDishId(dishId);
            }
            // 向口味表插入多条数据,insertBatch批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
        
    }
    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        /** 思路：
         * 创建mybatis-plus的分页对象
         * 调用Mapper的分页查询方法
         * 封装结果集为PageResult对象
         */
        Page<DishVO> page = new Page<>(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> pageResult = dishMapper.pageQuery(page, dishPageQueryDTO);
        return new PageResult(pageResult.getTotal(), pageResult.getRecords());
        
    }
    /**
     * 菜品批量删除
     * @param ids
     */
    @Override
    @Transactional
    // ids 指的是要删除的菜品 ID 列表 。
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否能够删除---是否存在起售中的菜品
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        // 判断当前菜品是否能够删除---是否被套餐关联了
        // List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        // if(setmealIds != null && setmealIds.size() > 0){
        //     // 有套餐关联,不能删除
        //     throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        // }

        //删除菜品表中的菜品数据
        // for(Long id : ids){
        //     dishMapper.deleteById(id);
        //     //删除菜品关联的口味数据
        //     dishFlavorMapper.deleteByDishId(id);
        // }

        // 根据菜品id批量删除菜品表中的数据
        dishMapper.deleteByIds(ids);
        // 根据菜品id批量删除口味表中的数据
        dishFlavorMapper.deleteByDishIds(ids);

    }
    /**
     * 根据id查询菜品和口味数据
     * @param id
     */
    @Override

    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据id查询口味数据
        List<DishFlavor> flavors =  dishFlavorMapper.getByDishId(id);
        //将查询到的数据封装到VO中
        DishVO dishVO = new DishVO();
        // DishVO是新建的空对象 ，不是从数据库查出来的。
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品和口味数据
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        // dto中有多余数据不需要
        BeanUtils.copyProperties(dishDTO, dish);
        //修改菜品表基本信息
        dishMapper.update(dish); 
        //删除原有的口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        // 插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            // 遍历flavors列表,为每个口味数据添加dishId
            for(DishFlavor df : flavors){
                df.setDishId(dishDTO.getId());
            }
            // 向口味表插入多条数据,insertBatch批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }
    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        // Dish.builder() 创建 Builder 对象 
        //  .categoryId(categoryId) 设置 categoryId 字段
        //  .status(StatusConstant.ENABLE) 设置 status 字段
        //  .build() 构建并返回 Dish 对象
        // 根据分类 ID 查询该分类下所有启用状态的菜品 。
        Dish dish = Dish.builder()
            .categoryId(categoryId)
            .status(StatusConstant.ENABLE)
            .build();
        return dishMapper.getByCategoryId(dish);
    }
    /**
     * 根据分类id查询菜品
     * @param categoryId    
     * @return
     */
	@Override
	public List<DishVO> listWithFlavor(Dish dish) {
        /**
         * 思路：
         * 1. 根据查询条件查询菜品列表
         * 2. 遍历菜品列表，为每个菜品添加口味数据
         * 3. 将菜品VO添加到列表中
         * 4. 返回菜品VO列表
         */
        // 根据查询条件查询菜品列表
        List<Dish> list = dishMapper.list(dish);
        // 初始化菜品VO列表，因为需要返回的是VO列表
        List<DishVO> dishVOList = new ArrayList<>();
        // 遍历菜品列表，为每个菜品添加口味数据
        for(Dish d : list){
            // 初始化菜品VO
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            //    再去获取他们的口味数据
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());
            // 将口味数据添加到菜品VO中
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }
        return dishVOList;
	}
    @Override
    public void startOrStop(Integer status, Long id) {
        // 根据菜品ID查询菜品数据
        Dish dish = dishMapper.getById(id);
        // 修改菜品状态
        dish.setStatus(status);
        // 更新菜品表中的数据
        dishMapper.update(dish);
    }
}
