package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link PropertySourcesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesUtils
 * @since 2017.01.13
 */
public class PropertySourcesUtilsTest {

    @Test
    public void testGetSubProperties() {

        MutablePropertySources propertySources = new MutablePropertySources();

        Map<String, Object> source = new HashMap<String, Object>();
        Map<String, Object> source2 = new HashMap<String, Object>();

        MapPropertySource propertySource = new MapPropertySource("propertySource", source);
        MapPropertySource propertySource2 = new MapPropertySource("propertySource2", source2);

        propertySources.addLast(propertySource);
        propertySources.addLast(propertySource2);

        Map<String, Object> result = PropertySourcesUtils.getSubProperties(propertySources, "user");

        Assert.assertEquals(Collections.emptyMap(), result);

        source.put("age", "31");
        source.put("user.name", "Mercy");
        source.put("user.age", "${age}");

        source2.put("user.name", "mercyblitz");
        source2.put("user.age", "32");

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", "Mercy");
        expected.put("age", "31");

        result = PropertySourcesUtils.getSubProperties(propertySources, "user");

        Assert.assertEquals(expected, result);

        result = PropertySourcesUtils.getSubProperties(propertySources, "");

        Assert.assertEquals(Collections.emptyMap(), result);

        result = PropertySourcesUtils.getSubProperties(propertySources, "no-exists");

        Assert.assertEquals(Collections.emptyMap(), result);

    }

}
