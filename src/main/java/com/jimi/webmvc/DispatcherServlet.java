package com.jimi.webmvc;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jimi.webmvc.annotatiion.Controller;
import com.jimi.webmvc.annotatiion.RequestMapping;
import com.jimi.webmvc.annotatiion.ResponseBody;
import com.jimi.webmvc.annotatiion.RestController;

/**
 * Servlet implementation class DispatcherServlet
 */
@WebServlet(urlPatterns = "/", displayName = "DispatcherServlet", initParams = {
		@WebInitParam(name = "scanpackage", value = "com.jimi.webmvc.controller") })

public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	Map<String, MappingObject> mappingObjects = new HashMap<>();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DispatcherServlet() {
		super();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println(config.getInitParameterNames());
		String scanpackage = config.getInitParameter("scanpackage");
		Class<DispatcherServlet> clazz = DispatcherServlet.class;
		String classpath = clazz.getResource("/").getPath();
		String scanpath = classpath + scanpackage.replaceAll("\\.", "\\" + File.separator);
		System.out.println(scanpath);

		allClassName(scanpath).stream().forEach(name -> {
			Class<?> beanClass = null;
			try {
				beanClass = clazz.getClassLoader().loadClass(scanpackage + "." + name);
				Object bean = beanClass.newInstance();
				boolean isRest = beanClass.isAnnotationPresent(RestController.class);
				if (isRest || beanClass.isAnnotationPresent(Controller.class)) {
					Method[] methods = beanClass.getMethods();
					Arrays.stream(methods).forEach(method -> {
						if (method.isAnnotationPresent(RequestMapping.class)) {
							RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
							boolean isJson = method.isAnnotationPresent(ResponseBody.class) || isRest;
							Arrays.stream(requestMapping.value()).forEach(uri -> {
								MappingObject mappingObject = new MappingObject();
								mappingObject.setHttpMethod(requestMapping.method());
								if (!uri.startsWith("/")) {
									uri = "/" + uri;
								}
								mappingObject.setMappingObject(bean);
								mappingObject.setMappingMethod(method);
								mappingObject.setUri(uri);
								mappingObject.setJson(isJson);
								mappingObjects.put(uri, mappingObject);
							});
						}
					});
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		System.out.println(mappingObjects);
	}

	public static void main(String[] args) {

	}

	public static List<String> allClassName(String path) {
		File pathfile = new File(path);
		File[] files = pathfile.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("class");
			}
		});
		return Arrays.stream(files).map(file -> {
			return file.getName().replace(".class", "");
		}).collect(Collectors.toList());

	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println(req.getServletPath());
		MappingObject mappingObject = mappingObjects.get(req.getServletPath());
		if (mappingObject == null) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		Object result = null;
		try {
			result = mappingObject.getMappingMethod().invoke(mappingObject.getMappingObject());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null) {
			if (mappingObject.isJson()) {
				resp.getWriter().print(result);
				resp.getWriter().flush();
			} else {
				req.getRequestDispatcher("/" + result + ".jsp").forward(req, resp);
			}
		}
	}
}
