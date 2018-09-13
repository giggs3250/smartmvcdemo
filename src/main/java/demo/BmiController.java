package demo;

import base.annotation.RequestMapping;

public class BmiController {
	@RequestMapping("/toBmi.do")
	public String toBmi() {
		return "bmi";
	}
}
