package base.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * @RequestMapping注解的作用，是用来配置smartmvc框架，
 * 请求路径与处理器的对应关系。
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
	public String value();
}
