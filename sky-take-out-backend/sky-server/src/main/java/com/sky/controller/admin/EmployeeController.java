package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
//controller层专门接收前端请求、返回响应，是前端和后端的「桥梁」。
/**
 * 员工管理
 */
@RestController
@RequestMapping("/employee")
@Slf4j

@Api(tags = "员工相关接口")//描述类的业务功能

 class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")

    @ApiOperation(value = "员工登录")//描述方法的业务功能

    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation("员工退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     * @return
     */
    @PostMapping//是 Spring MVC/Spring Boot 中用来定义 HTTP POST 请求接口的注解，
//    简单说：告诉 Spring，这个方法专门处理前端发来的 POST 类型请求。
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
//        DTO专门用来在接口、服务、网络之间传递数据的 Java 类。
        log.info("新增员工：{}",employeeDTO);
        System.out.println("当前线程的id:"+ Thread.currentThread().getId());
        employeeService.save(employeeDTO);
        return Result.success();
    }
    /**
     * 员工分页查询
     *
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询：{}",employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 员工状态启用/禁用
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")//状态 1启用 0禁用
    @ApiOperation("员工状态启用/禁用")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("员工状态：{},员工id:{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }
    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation("根据id查询员工")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }
    /**
     * 编辑员工信息
     *
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑员工信息")
    private Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工信息：{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
