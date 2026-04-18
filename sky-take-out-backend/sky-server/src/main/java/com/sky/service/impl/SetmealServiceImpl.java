package com.sky.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private CategoryMapper categoryMapper;
     
    /**
     * 新增套餐
     * @param setmeal
     */
    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        /** 思路：
         * 1. 先创建空的Setmeal套餐对象
         * 2. 从setmealDTO中拷贝属性到setmeal对象，因为setmealDTO中有多余数据不需要
         * 3. 调用Mapper的新增方法，新增套餐
         * 4. 获取新增的套餐id
         * 5. 遍历套餐里面的菜品，并分别给菜品添加上套餐id
         * 6. 调用Mapper的批量插入方法，新增套餐里面的菜品到数据库中
         */
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        // 新增套餐
        setmealMapper.insert(setmeal);
        //定义套餐id
        Long setmealId = setmeal.getId();
        // 获取套餐里面的菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 遍历套餐里面的菜品，并分别给菜品添加上套餐id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        // 把绑定好套餐 ID 的所有菜品，批量插入到数据库里保存起来。
        setmealDishMapper.insertBatch(setmealDishes);
    }
    /**
     * 分页查询套餐
     * @param setmealPageQueryDTO   
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        /** 思路：
         * 1. 创建 mybatis-plus 的分页对象
         * 2. 使用 QueryWrapper 构建查询条件
         * 3. 使用 Mybatis-Plus 的 selectPage 方法查询
         * 4. 封装结果集为 PageResult 对象
         */
        Page<Setmeal> page = new Page<>(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        
        // 构建查询条件
        QueryWrapper<Setmeal> queryWrapper = new QueryWrapper<>();
        if (setmealPageQueryDTO.getStatus() != null) {
            queryWrapper.eq("status", setmealPageQueryDTO.getStatus());
        }
        if (setmealPageQueryDTO.getCategoryId() != null) {
            queryWrapper.eq("category_id", setmealPageQueryDTO.getCategoryId());
        }
        if (setmealPageQueryDTO.getName() != null && !setmealPageQueryDTO.getName().isEmpty()) {
            queryWrapper.like("name", setmealPageQueryDTO.getName());
        }
        queryWrapper.orderByDesc("create_time");
        
        // 使用 MyBatis-Plus 的 selectPage 方法
        Page<Setmeal> resultPage = (Page<Setmeal>) setmealMapper.selectPage(page, queryWrapper);
        
        // 将 Setmeal 转换为 SetmealVO（需要关联查询分类名称）
        List<SetmealVO> setmealVOList = resultPage.getRecords().stream().map(setmeal -> {
            SetmealVO setmealVO = new SetmealVO();
            BeanUtils.copyProperties(setmeal, setmealVO);
            // 查询分类名称
            Category category = categoryMapper.selectById(setmeal.getCategoryId());
            if (category != null) {
                setmealVO.setCategoryName(category.getName());
            }
            return setmealVO;
        }).collect(Collectors.toList());
        
        return new PageResult(resultPage.getTotal(), setmealVOList);
    }
    /**
     * 批量删除套餐
     * @param ids
     */
    @Override
    // ids ：参数名，表示要删除的套餐ID数组
    public void deleteBatch(List<Long> ids) {   
        /**
         * 思路：
         * 1. 遍历套餐id数组，调用Mapper的根据id查询套餐方法
         * 2. 如果套餐状态为1，说明是起售中的套餐，抛出异常
         * 3. 删除套餐表的数据
         * 4. 删除与套餐表关联的菜品关系表的数据
         */
        //起售中的套餐不能删除
        // id 是套餐表（ setmeal ）的主键
        // 这段代码使用增强for循环遍历套餐ID数组，
        // 逐个删除每个套餐及其关联的菜品数据，实现了批量删除套餐的功能。
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        // 这段代码使用增强for循环遍历套餐ID数组，
        // 删除每个套餐及其关联的菜品数据，实现了批量删除套餐的功能。
        for (Long id : ids) {
            Long setmealId = id;
            //删除套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除套餐菜品关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        }
    }
    /**
     * 根据id查询套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        // 查询套餐包含的所有菜品数据
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }
    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //1、修改套餐表，执行update
        setmealMapper.update(setmeal);
        //套餐id
        Long setmealId = setmealDTO.getId();
        //2、删除套餐和菜品的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealId);    
        // 获取套餐里面的菜品数据
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //3、重新插入套餐和菜品的关联关系，操作setmeal_dish表，执行insert
        for (SetmealDish setmealDish : setmealDTO.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealId);
        }
        // 把绑定好套餐 ID 的所有菜品，批量插入到数据库里保存起来。
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐起售停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品
        // 提示"套餐内包含未启售菜品，无法启售"
        if (status == StatusConstant.ENABLE) {
            // 根据套餐 ID，去数据库里查这条套餐关联的所有菜品数据，然后封装成一个 SetmealDish 对象列表返回给你。
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            // 遍历套餐里面的菜品，并分别判断菜品是否停售
            for (Dish dish : dishList) {
                // 如果菜品状态为停售，说明是停售中的菜品，抛出异常
                if (dish.getStatus() == StatusConstant.DISABLE ) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }   
            }
        }
        // 只封装 套餐 ID 和 状态，生成一个 Setmeal 对象，用于更新套餐状态
        // 只修改套餐的 起售 / 停售 状态，不改动其他信息
//         等价于原来的写法：
//         Setmeal setmeal = new Setmeal();
//         setmeal.setId(id);
//         setmeal.setStatus(status);
        Setmeal setmeal = Setmeal.builder()
            .id(id)
            .status(status)
            .build();
        setmealMapper.updateById(setmeal);
    }

        /**
     * 根据分类 id 查询套餐
     * @param setmeal
     * @return
     */
    @Override
    public List<Setmeal> listByCategoryId(Setmeal setmeal) {
        // 直接调用注解实现的方法
        List<Setmeal> list = setmealMapper.listByCategoryId(setmeal.getCategoryId(), setmeal.getStatus());
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
