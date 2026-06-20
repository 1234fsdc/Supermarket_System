package com.sky.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("开始插入填充");
        fillField(metaObject, OperationType.INSERT);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("开始更新填充");
        fillField(metaObject, OperationType.UPDATE);
    }

    private void fillField(MetaObject metaObject, OperationType operationType) {
        try {
            Object originalObject = metaObject.getOriginalObject();
            Field[] fields = originalObject.getClass().getDeclaredFields();
            
            for (Field field : fields) {
                String fieldName = field.getName();
                boolean shouldFill = false;
                
                if (fieldName.equals("createTime") && operationType == OperationType.INSERT) {
                    shouldFill = true;
                } else if (fieldName.equals("updateTime")) {
                    shouldFill = true;
                } else if (fieldName.equals("createUser") && operationType == OperationType.INSERT) {
                    shouldFill = true;
                } else if (fieldName.equals("updateUser")) {
                    shouldFill = true;
                }
                
                if (shouldFill) {
                    if (fieldName.equals("createTime") || fieldName.equals("updateTime")) {
                        metaObject.setValue(fieldName, LocalDateTime.now());
                    } else if (fieldName.equals("createUser") || fieldName.equals("updateUser")) {
                        Long currentId = BaseContext.getCurrentId();
                        metaObject.setValue(fieldName, currentId != null ? currentId : 1L);
                    }
                }
            }
        } catch (Exception e) {
            log.error("自动填充失败", e);
        }
    }
}
