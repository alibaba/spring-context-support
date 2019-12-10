package com.alibaba.spring.util;

import org.junit.Test;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.spring.util.PropertySourcesUtils.getSubProperties;
import static org.junit.Assert.assertEquals;

/**
 * {@link PropertySourcesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesUtils
 * @since 2017.01.13
 */
@SuppressWarnings("unchecked")
public class PropertySourcesUtilsTest {

    @Test
    public void testGetSubProperties() {

        ConfigurableEnvironment environment = new AbstractEnvironment() {
        };

        MutablePropertySources propertySources = environment.getPropertySources();

        Map<String, Object> source = new HashMap<String, Object>();
        Map<String, Object> source2 = new HashMap<String, Object>();

        MapPropertySource propertySource = new MapPropertySource("propertySource", source);
        MapPropertySource propertySource2 = new MapPropertySource("propertySource2", source2);

        propertySources.addLast(propertySource);
        propertySources.addLast(propertySource2);

        Map<String, Object> result = getSubProperties(propertySources, "user");

        assertEquals(Collections.emptyMap(), result);

        source.put("age", "31");
        source.put("user.name", "Mercy");
        source.put("user.age", "${age}");

        source2.put("user.name", "mercyblitz");
        source2.put("user.age", "32");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", "Mercy");
        expected.put("age", "31");

        assertEquals(expected, getSubProperties((Iterable) propertySources, "user"));

        assertEquals(expected, getSubProperties(environment, "user"));

        assertEquals(expected, getSubProperties(propertySources, "user"));

        assertEquals(expected, getSubProperties(propertySources, environment, "user"));

        result = getSubProperties(propertySources, "");

        assertEquals(Collections.emptyMap(), result);

        result = getSubProperties(propertySources, "no-exists");

        assertEquals(Collections.emptyMap(), result);

    }

}
