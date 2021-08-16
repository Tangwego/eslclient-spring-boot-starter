package top.wdcc.eslclient.client;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 实现自定义Listener注解供扩展使用
 */
@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EslListener{
    String eventName() default "";
    String subClassName() default "";
}
