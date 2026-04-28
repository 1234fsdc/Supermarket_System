package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController//让这个类能处理前端请求并直接返回JSON 数据
@RequestMapping("/category")//设置接口父路径
@Slf4j//@Slf4j = Lombok 自动生成日志对象，省去手动写 Logger 代码；

@Api(tags = "分类相关接口")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分类分页查询")
//    Result<PageResult> 的意思：后端给前端返回的【统一格式响应结果】，里面包裹了【分页数据】
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询{}",categoryPageQueryDTO);
        PageResult pageResult =categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 新增分类
     * @param categoryDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO) {
        // 在程序运行时，输出「新增分类」的业务提示 + 分类对象的详细数据，
        // 用于记录程序执行轨迹、排查问题和监控业务操作。
        log.info("新增分类：{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("启用/禁用分类")
    public Result starOrStop(@PathVariable Integer status,Long id){
        log.info("启用/禁用分类：{},分类id:{}",status,id);
        categoryService.starOrStop(status, id);
        return Result.success();
    }
    /**
     * 修改分类
     * @param categoryDTO
     * @return
     */ 
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }
    /**
     * 根据id删除分类
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result delete(@RequestParam Long id){
        log.info("删除分类：{}",id);
        
        categoryService.delete(id);
        return Result.success();
    }   
    
    /**
     * 分类列表查询
     * @return
     */
    @GetMapping("list")
    @ApiOperation("根据类型查询分类")
    public Result list(@RequestParam(required = false) Integer type){
        return Result.success(categoryService.list(type));
    }

}
