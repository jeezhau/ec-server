package com.mofangyouxuan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.mofangyouxuan.mapper")
@EnableTransactionManagement
public class MfyxServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MfyxServerApplication.class, args);
	}
}
