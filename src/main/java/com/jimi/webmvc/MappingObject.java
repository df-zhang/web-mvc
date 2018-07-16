package com.jimi.webmvc;

import java.lang.reflect.Method;

import com.jimi.webmvc.annotatiion.HttpMethod;

public class MappingObject {
	private Object mappingObject;
	private Method mappingMethod;
	private HttpMethod httpMethod;
	private String uri;
	private boolean json;

	public Object getMappingObject() {
		return mappingObject;
	}

	public void setMappingObject(Object mappingObject) {
		this.mappingObject = mappingObject;
	}

	public Method getMappingMethod() {
		return mappingMethod;
	}

	public void setMappingMethod(Method mappingMethod) {
		this.mappingMethod = mappingMethod;
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public boolean isJson() {
		return json;
	}

	public void setJson(boolean json) {
		this.json = json;
	}

}
