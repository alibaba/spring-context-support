package com.alibaba.spring.util;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

import static com.alibaba.spring.util.PropertySourcesUtils.getSubProperties;

/**
 * {@link PropertyValues} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyValues
 * @since 2017.01.19
 */
public abstract class PropertyValuesUtils {

    /**
     * Get Sub {@link PropertyValues} from {@link ConfigurableEnvironment}
     *
     * @param environment {@link ConfigurableEnvironment}
     * @param prefix      the prefix of property name
     * @return {@link PropertyValues}
     */
    public static PropertyValues getSubPropertyValues(ConfigurableEnvironment environment, String prefix) {

        Map<String, Object> subProperties = getSubProperties(environment.getPropertySources(), environment, prefix);

        PropertyValues subPropertyValues = new MutablePropertyValues(subProperties);

        return subPropertyValues;

    }
}
