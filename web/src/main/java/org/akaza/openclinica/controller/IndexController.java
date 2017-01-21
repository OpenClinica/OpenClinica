package org.akaza.openclinica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/multi")
public class IndexController {

	@RequestMapping
	public String index() {
		return "index";
	}
}
