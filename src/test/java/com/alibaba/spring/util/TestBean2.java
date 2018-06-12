package com.alibaba.spring.util;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * Test Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
@Component("testBean2")
public class TestBean2 implements Bean, Ordered {

    @Override
    public int getOrder() {
        return 2;
    }
}
