package com.alibaba.spring.util;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Test Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
@Component("testBean")
@Order(1)
public class TestBean implements Bean {
}
