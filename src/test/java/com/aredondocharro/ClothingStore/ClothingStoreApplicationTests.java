package com.aredondocharro.ClothingStore;

import com.aredondocharro.ClothingStore.testconfig.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ClothingStoreApplicationTests {

	@Test
	void contextLoads() {
	}

}
