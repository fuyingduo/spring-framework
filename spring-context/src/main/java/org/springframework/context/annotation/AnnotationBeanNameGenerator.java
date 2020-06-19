/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.beans.Introspector;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link org.springframework.beans.factory.support.BeanNameGenerator}
 * implementation for bean classes annotated with the
 * {@link org.springframework.stereotype.Component @Component} annotation
 * or with another annotation that is itself annotated with
 * {@link org.springframework.stereotype.Component @Component} as a
 * meta-annotation. For example, Spring's stereotype annotations (such as
 * {@link org.springframework.stereotype.Repository @Repository}) are
 * themselves annotated with
 * {@link org.springframework.stereotype.Component @Component}.
 *
 * <p>Also supports Java EE 6's {@link javax.annotation.ManagedBean} and
 * JSR-330's {@link javax.inject.Named} annotations, if available. Note that
 * Spring component annotations always override such standard annotations.
 *
 * <p>If the annotation's value doesn't indicate a bean name, an appropriate
 * name will be built based on the short name of the class (with the first
 * letter lower-cased). For example:
 *
 * <pre class="code">com.xyz.FooServiceImpl -&gt; fooServiceImpl</pre>
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @since 2.5
 * @see org.springframework.stereotype.Component#value()
 * @see org.springframework.stereotype.Repository#value()
 * @see org.springframework.stereotype.Service#value()
 * @see org.springframework.stereotype.Controller#value()
 * @see javax.inject.Named#value()
 */
public class AnnotationBeanNameGenerator implements BeanNameGenerator {

	private static final String COMPONENT_ANNOTATION_CLASSNAME = "org.springframework.stereotype.Component";


	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {

		// 判断Bean描述对象是不是一个注解描述对象 也就是 `AnnotatedBeanDefinition`
		if (definition instanceof AnnotatedBeanDefinition) {

			// 获取注解中的名字，进入方法查看详细注释，获取到beanName后判断是否为空，如果值存在则返回
			String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
			if (StringUtils.hasText(beanName)) {
				// Explicit bean name found.
				return beanName;
			}
		}
		// 当在上一步获取beanName 值为NULL是则获取beanName默认名称，进入方法看详情
		// Fallback: generate a unique default bean name.
		return buildDefaultBeanName(definition, registry);
	}

	/**
	 * 这个方法主要是冲注解中获取Bean的名称
	 * Derive a bean name from one of the annotations on the class.
	 * @param annotatedDef the annotation-aware bean definition
	 * @return the bean name, or {@code null} if none is found
	 */
	@Nullable
	protected String determineBeanNameFromAnnotation(AnnotatedBeanDefinition annotatedDef) {

		// 获取存入注解描述对象AnnotatedBeanDefinition 中的元数据（其实就是初始化对象的数据）
		AnnotationMetadata amd = annotatedDef.getMetadata();

		// 在元数据中获取所有注解的类型 比如 Service...
		Set<String> types = amd.getAnnotationTypes();

		// 设置一个Bean的名称为NULL
		String beanName = null;
		for (String type : types) {

			// 通过注解的类型在AnnotatedBeanDefinition 中获取注解的属性
			AnnotationAttributes attributes = AnnotationConfigUtils.attributesFor(amd, type);

			/**
			 * <p>
			 * 主要判断在注解属性不为NULL情况下，判断当前注解以及注解的子类是否包含 org.springframework.stereotype.Component注解
			 * 同时判断注解的属性类型是否是 javax.annotation.ManagedBean 、javax.inject.Named
			 * 如果包含并且 attributes 属性中存在value值 则返回 true， 其中value值指的就是 @service("value") 中的value
			 * </p>
			 * 如果验证不通过则beanName返回NULL
			 */
			if (attributes != null && isStereotypeWithNameValue(type, amd.getMetaAnnotationTypes(type), attributes)) {

				/**
				 * 从属性 attributes 中获取value值
				 * 判断value值是否是String类型，并且不为空，
				 * 将属性value赋值给beanName 返回
				 */
				Object value = attributes.get("value");
				if (value instanceof String) {
					String strVal = (String) value;
					if (StringUtils.hasLength(strVal)) {
						if (beanName != null && !strVal.equals(beanName)) {
							throw new IllegalStateException("Stereotype annotations suggest inconsistent " +
									"component names: '" + beanName + "' versus '" + strVal + "'");
						}
						beanName = strVal;
					}
				}
			}
		}
		return beanName;
	}

	/**
	 * Check whether the given annotation is a stereotype that is allowed
	 * to suggest a component name through its annotation {@code value()}.
	 * @param annotationType the name of the annotation class to check
	 * @param metaAnnotationTypes the names of meta-annotations on the given annotation
	 * @param attributes the map of attributes for the given annotation
	 * @return whether the annotation qualifies as a stereotype with component name
	 */
	protected boolean isStereotypeWithNameValue(String annotationType,
			Set<String> metaAnnotationTypes, @Nullable Map<String, Object> attributes) {

		boolean isStereotype = annotationType.equals(COMPONENT_ANNOTATION_CLASSNAME) ||
				metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME) ||
				annotationType.equals("javax.annotation.ManagedBean") ||
				annotationType.equals("javax.inject.Named");

		return (isStereotype && attributes != null && attributes.containsKey("value"));
	}

	/**
	 * Derive a default bean name from the given bean definition.
	 * <p>The default implementation delegates to {@link #buildDefaultBeanName(BeanDefinition)}.
	 * @param definition the bean definition to build a bean name for
	 * @param registry the registry that the given bean definition is being registered with
	 * @return the default bean name (never {@code null})
	 */
	protected String buildDefaultBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		return buildDefaultBeanName(definition);
	}

	/**
	 *
	 * 获取beanName 默认名称
	 * Derive a default bean name from the given bean definition.
	 * <p>The default implementation simply builds a decapitalized version
	 * of the short class name: e.g. "mypackage.MyJdbcDao" -> "myJdbcDao".
	 * <p>Note that inner classes will thus have names of the form
	 * "outerClassName.InnerClassName", which because of the period in the
	 * name may be an issue if you are autowiring by name.
	 * @param definition the bean definition to build a bean name for
	 * @return the default bean name (never {@code null})
	 */
	protected String buildDefaultBeanName(BeanDefinition definition) {
		// 主要通过BeanDefinition 获取class 对象，并将对象的全线定名返回 如：com.f7o.xxx
		String beanClassName = definition.getBeanClassName();
		Assert.state(beanClassName != null, "No bean class name set");
		// 截取最后一个符号为 `.` 后的名称，并判断名称是否包含`$`如果包含则需替换为`.` 返回名称 如 person$a 则返回的为person.a
		String shortClassName = ClassUtils.getShortName(beanClassName);
		// 将默认生成的beanName 按驼峰命名返回
		return Introspector.decapitalize(shortClassName);
	}

}
