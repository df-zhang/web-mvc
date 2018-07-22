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
 * DispatcherServlet，用于接收来自页面的所有请求，然后将请求转发到各个uri对应的controller.method中处理。<br>
 * Servlet3.0开始提供注解，@WebServlet描述该类为Servlet，urlPatterns为匹配uri，initParams为初始化参数。<br>
 * 对应xml如下： <br>
 * &lt;servlet&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;servlet-name&gt;DispatcherServlet&lt;/servlet-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;servlet-class&gt;com.jimi.webmvc.DispatcherServlet&lt;/servlet-class&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;init-param&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-name&gt;scanpackage&lt;/param-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;param-value&gt;com.jimi.webmvc.controller&lt;/param-value&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/init-param&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/servlet&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;servlet-mapping&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;servlet-name&gt;DispatcherServlet&lt;/servlet-name&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url-pattern&gt;/&lt;/url-pattern&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/servlet-mapping&gt;<br>
 */
@WebServlet(urlPatterns = "/", displayName = "DispatcherServlet", initParams = {
		@WebInitParam(name = DispatcherServlet.INIT_PARAM_SCANPACKAGE, value = "com.jimi.webmvc.controller") })
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String INIT_PARAM_SCANPACKAGE = "scanpackage";

	/**
	 * uri-method映射对象集合
	 */
	private static Map<String, MappingObject> mappingObjects = new HashMap<>();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		// 加载classpath目录，即根目录
		Class<DispatcherServlet> clazz = DispatcherServlet.class;
		String classpath = clazz.getResource("/").getPath();

		// 加载初始化参数
		String scanpackage = config.getInitParameter(INIT_PARAM_SCANPACKAGE);

		// 将点换成目录符号
		String scanpath = classpath + scanpackage.replaceAll("\\.", "" + File.separator);

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
		// 加载classpath目录，即根目录
		Class<DispatcherServlet> clazz = DispatcherServlet.class;
		String classpath = clazz.getResource("/").getPath();

		// 加载初始化参数
		String scanpackage = "com.jimi.webmvc.controller";

		// 将点换成目录符号
		String scanpath = classpath + scanpackage.replaceAll("\\.", "\\" + File.separator);

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

	/**
	 * 获得指定目录下所有class文件的名称。不含.class
	 * 
	 * @param path
	 * @return
	 */
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
