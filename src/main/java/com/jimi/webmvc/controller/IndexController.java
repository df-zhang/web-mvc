package com.jimi.webmvc.controller;

import com.jimi.webmvc.annotatiion.Controller;
import com.jimi.webmvc.annotatiion.RequestMapping;
import com.jimi.webmvc.annotatiion.ResponseBody;

@Controller
public class IndexController {

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
