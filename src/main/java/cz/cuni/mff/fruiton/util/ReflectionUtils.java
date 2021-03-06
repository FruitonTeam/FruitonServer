package cz.cuni.mff.fruiton.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class ReflectionUtils {

    private static final Logger logger = Logger.getLogger(ReflectionUtils.class.getName());

    private ReflectionUtils() {

    }

    public static Set<Method> getMethodsWithAnnotation(
            final Set<Class<?>> classes,
            final Class<? extends Annotation> annotation
    ) {
        Set<Method> methods = new HashSet<>();

        for (Class<?> cl : classes) {
            for (Method m : cl.getDeclaredMethods()) {
                if (m.isAnnotationPresent(annotation)) {
                    methods.add(m);
                }
            }
        }

        return methods;
    }

    public static Set<Class<?>> getClassesInPackages(final Iterable<String> basePackages, final Environment env) {
        Set<Class<?>> classes = new HashSet<>();
        for (String basePackage : basePackages) {
            classes.addAll(getClassesInPackage(basePackage, env));
        }

        return classes;
    }

    private static Set<Class<?>> getClassesInPackage(final String basePackage, final Environment env) {
        Set<Class<?>> classes = new HashSet<>();

        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false, env);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        final Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(basePackage);

        for (BeanDefinition bean : beanDefinitions) {
            Class<?> clazz;
            try {
                clazz = Class.forName(bean.getBeanClassName());
                classes.add(clazz);
            } catch (ClassNotFoundException e) {
                logger.log(Level.WARNING, "Could not find class for bean definition", e);
            }
        }

        return classes;
    }

}
