package com.chenchen.rpc.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 * @author chenchen
 * @create 2017-07-26 13:31
 **/
@Target({ ElementType.TYPE })// 表明RpcInterface这个注解放到Class, interface (including annotation type), or enum 上
@Retention(RetentionPolicy.RUNTIME)// VM将在运行期也保留注释，因此可以通过反射机制读取注解的信息
@Component // spring的注解，用于spring扫描
public @interface RpcInterface {
    // 返回自定义注解中的value
    String value();
}
