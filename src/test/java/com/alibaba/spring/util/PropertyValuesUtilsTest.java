package com.alibaba.spring.util;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.PropertyValues;
import org.springframework.mock.env.MockEnvironment;

/**
 * {@link PropertyValuesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyValuesUtils
 * @since 2017.01.13
 */
public class PropertyValuesUtilsTest {

    @Test
    public void testGetSubPropertyValues() {

        MockEnvironment environment = new MockEnvironment();

        PropertyValues propertyValues = PropertyValuesUtils.getSubPropertyValues(environment, "user");

        Assert.assertNotNull(propertyValues);

        Assert.assertFalse(propertyValues.contains("name"));
        Assert.assertFalse(propertyValues.contains("age"));

        environment.setProperty("user.name", "Mercy");
        environment.setProperty("user.age", "30");

        propertyValues = PropertyValuesUtils.getSubPropertyValues(environment, "user");

        Assert.assertEquals("Mercy", propertyValues.getPropertyValue("name").getValue());
        Assert.assertEquals("30", propertyValues.getPropertyValue("age").getValue());

    }

}
