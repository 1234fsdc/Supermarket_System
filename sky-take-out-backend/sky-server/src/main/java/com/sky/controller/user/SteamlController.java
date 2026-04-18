package com.sky.controller.user;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

// 作用：声明这是一个控制器，专门用来写接口，返回 JSON / 字符串等数据，不跳转页面
// ("userSetmealController")这是给 Bean 指定名称
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "C端-套餐浏览接口")
public class SteamlController {
    @Autowired
    private SetmealService setmealService;

    /**
     * 条件查询
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    // 从redis缓存中查询数据，有数据直接返回，没有从sql数据库查询
    @Cacheable(cacheNames = "setmealCache",key = "#categoryId")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> listByCategoryId(Long categoryId) {
        //设置查询的条件，根据分类id和状态查询套餐
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        // 根据分类id查询套餐列表
        List<Setmeal> list = setmealService.listByCategoryId(setmeal);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品列表")
    public Result<List<DishItemVO>> dishList(@PathVariable("id") Long id) {
        List<DishItemVO> list = setmealService.getDishItemById(id);
        return Result.success(list);
    }
}
