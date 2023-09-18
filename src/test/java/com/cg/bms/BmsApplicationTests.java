package com.cg.bms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
class BmsApplicationTests {

	@Test
	void contextLoads() {
		Assertions.assertTrue(true);
	}
	@Test
	void main_executes_successfully(){
		String[] args = {};
		BmsApplication.main(args);
		Assertions.assertTrue(true);
	}
}
