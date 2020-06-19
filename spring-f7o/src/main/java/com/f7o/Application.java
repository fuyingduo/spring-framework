package com.f7o;

import com.f7o.config.AppConfig;
import com.f7o.processor.TestDefinitionRegistryPostProessor;
import com.f7o.services.IndexService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * created by fuyd on 2020-06-16
 * @author fuei
 */
public class Application {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.addBeanFactoryPostProcessor(new TestDefinitionRegistryPostProessor());
		context.register(AppConfig.class);
		context.refresh();
		IndexService indexService = (IndexService) context.getBean("indexService");
		indexService.query();
	}
}
