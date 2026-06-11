package com.my.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.my.blog.dao")
public class BlogApplication {
public static void main(String[] args) {
SpringApplication.run(BlogApplication.class, args);
}
}