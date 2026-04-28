package com.sky.controller.user;

import com.sky.dto.CustomerServiceResult;
import com.sky.result.Result;
import com.sky.service.AiCustomerServiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端AI客服控制器
 */
@Slf4j
@RestController("userAiCustomerController")
@RequestMapping("/user/ai-customer")
@Api(tags = "用户端-AI客服接口")
public class AiCustomerController {

    @Autowired
    private AiCustomerServiceService aiCustomerServiceService;

    /**
     * AI客服问答接口
     *
     * @param question 用户问题
     * @return AI回答（含推荐商品）
     */
    @GetMapping("/ask")
    @ApiOperation("AI客服问答")
    public Result<CustomerServiceResult> ask(@RequestParam String question) {
        log.info("收到AI客服咨询，问题：{}", question);
        CustomerServiceResult result = aiCustomerServiceService.getAnswer(question);
        return Result.success(result);
    }
}
