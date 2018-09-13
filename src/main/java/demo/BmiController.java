package demo;

import base.annotation.RequestMapping;

public class BmiController {
	@RequestMapping("/toBmi.do")
	public String toBmi() {
		System.out.println("测试");
		return "bmi";
	}
}
