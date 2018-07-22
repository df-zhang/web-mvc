package com.jimi.webmvc.controller;

import com.jimi.webmvc.annotatiion.Controller;
import com.jimi.webmvc.annotatiion.RequestMapping;
import com.jimi.webmvc.annotatiion.ResponseBody;

@Controller
public class IndexController {
	static {
		System.out.println("静态代码块被执行");
	}

	@RequestMapping({ "", "/", "/index" })
	public String index() {
		return "index";
	}

	@RequestMapping("get")
	@ResponseBody
	public String get() {
		return "{'name': 'getMethod'}";
	}
}
