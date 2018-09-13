package base.web;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import base.common.Handler;
import base.common.HandlerMapping;




/**
 * 控制器：负责接收所有请求，然后依据HandlerMapping的配置调用对应的Controller（处理器）来处理。
 * 依据处理器返回的处理结果（视图名）调用对应的jsp。
 */
public class DisPathcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private HandlerMapping handlerMapping;
	@Override
	/**
	 * 在init方法里面，通过读取smartmvc.xml文件获得处理器类名（比如demo.HelloController）,
	 * 然后将处理器实例化。接下来，将处理器实例化交给HandlerMapping（即映射处理器）来处理。
	 * 注：HandlerMapping负责建立请求路径与处理器的对应关系。
	 */
	public void init() throws ServletException {
		/*
		 * 利用dom4j解析smartmvc.xml
		 */
//		System.out.println(System.getProperty("java.class.path"));
		SAXReader reader = new SAXReader();
		//读取配置文件的位置及文件名
		String fileName = getServletConfig().getInitParameter("configLocation");
		/*/
		 * 调用类加载器（ClassLoader）的方法来获得用来读取smartmvc.xml文件的输入流。
		 */
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(fileName);
		try {
			//解析smartmvc的配置文件的内容
			Document doc = reader.read(inStream);
			//获得根元素
			Element root = doc.getRootElement();
			//获得跟元素下面的所有子元素
			List<Element> elements = root.elements();
			List beans = new ArrayList();
			//遍历所有子元素
			for(Element ele : elements) {
				//读取class属性值（即处理器类名）
				String className = ele.attributeValue("class");
				System.out.println("类名是："+className);
				//将处理器实例化
				Object bean = Class.forName(className).newInstance();
				//为了方便处理，将处理器实例添加到List集合里面。
				beans.add(bean);
				System.out.println("beans:"+beans);
				
				//将处理器实例交给HandlerMapping来处理
				handlerMapping = new HandlerMapping();
				handlerMapping.process(beans);
			}
		} catch (Exception e) {
			e.printStackTrace();
			//将异常抛给容器处理
			throw new ServletException(e);
		}
		
	}


	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*
		 * 获得请求资源路径，然后截取其中的一部分，获得请求路径（path），接下来，调用
		 * HadnlerMapping的getHandler方法（以请求路径作为参数）获得Handler对象。
		 * 通过Handler对象就可以调用处理器的方法了。
		 */
		String uri = request.getRequestURI();//获得请求资源路径
		System.out.println("请求路径uri："+uri);
		//获得应用名
		String contextPath = request.getContextPath();
		System.out.println("contextPath:"+contextPath);
		//截取请求资源路径的一部分
		String path = uri.substring(contextPath.length());
		System.out.println("path:"+path);
		
		//调用HandlerMapping的方法获得Handler对象
		Handler handler = handlerMapping.getHandler(path);
		
		System.out.println("handler:"+handler);
		
		//利用Handler对象来调用处理器的方法
		Method mh = handler.getMh();
		Object obj = handler.getObj();
		Object returnVal = null;
		try {
			/*
			 * 利用java反射分析处理器的方法，如果处理器的方法带有参数，则将该参数传给处理器方法。
			 * 注：目前版本只支持两个参数：request，response
			 */
			Class[] types = mh.getParameterTypes();//获得方法的参数类型
			if(types.length>0) {
				Object[] params = new Object[types.length];
				//方法带有参数
				for(int i = 0;i<types.length;i++) {
					if(types[i]==HttpServletRequest.class) {
						params[i] = request;
					}
					if(types[i]==HttpServletResponse.class) {
						params[i] = response;
					}
				}
				returnVal = mh.invoke(obj, params);
			}else {
				//方法没有参数
				returnVal = mh.invoke(obj);
			}
			
			System.out.println("returnVal返回值是："+returnVal);
			
			/*
			 * 将视图名映射成真正的jsp，jsp地址="/WEB-INF/"+视图名+".jsp"
			 */
			String viewName = returnVal.toString();//获得视图名
			String jspPath = null;
			//如果视图名是以“redirect：”开头，则重定向，否则转发。
			if(viewName.startsWith("redirect:")) {
				//将视图名中的“redirect：”去掉，生成真正的重定向地址。
				jspPath = contextPath+"/"+viewName.substring("redirect:".length());
				response.sendRedirect(jspPath);
			}else {
				jspPath = "/WEB-INF/"+viewName+".jsp";
				request.getRequestDispatcher(jspPath).forward(request, response);
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		} 
	}




}
