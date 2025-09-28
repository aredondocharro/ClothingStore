package com.aredondocharro.ClothingStore;

import org.springframework.boot.SpringApplication;

public class TestClothingStoreApplication {

	public static void main(String[] args) {
		SpringApplication.from(ClothingStoreApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
