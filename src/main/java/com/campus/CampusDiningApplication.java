package com.campus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园自助点餐系统 — 启动类
 *
 * @author Campus Team
 */
@SpringBootApplication
@MapperScan("com.campus.mapper")
public class CampusDiningApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusDiningApplication.class, args);
        System.out.println("============================================");
        System.out.println("  校园自助点餐系统启动成功！");
        System.out.println("  Knife4j 文档: http://localhost:8080/doc.html");
        System.out.println("============================================");
    }
}
