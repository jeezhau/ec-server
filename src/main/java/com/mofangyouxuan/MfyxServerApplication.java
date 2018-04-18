package com.mofangyouxuan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.mofangyouxuan.mapper")
public class MfyxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfyxServerApplication.class, args);
	}
}
