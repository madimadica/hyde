package com.madimadica.hyde.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternMatcherTest {

    void assertEmail(String email) {
        assertEquals(email, PatternMatcher.findAutolinkEmail(email, 0));
    }

    void assertNotEmail(String email) {
        assertNull(PatternMatcher.findAutolinkEmail(email, 0));
    }

    @Test
    void foo() {
        HtmlEntities.entityMap.forEach((k, v) -> {
            System.out.println(v + " (" + k + ")");
        });
    }

    @Test
    void findAutolinkEmail() {
        assertEmail("<test@example.com>");
        assertEmail("<test@example.com>");
        assertEmail("<test@example.com>");
        assertEmail("<user.name@subdomain.example.com>");
        assertEmail("<first.last+alias@example.co.uk>");
        assertEmail("<user@domain-example.com>");
        assertEmail("<person@sub.domain.example.org>");
        assertEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb123>");
        assertEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb123.aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb123>");

        assertNotEmail("test@example.com");
        assertNotEmail("<test@example.com");
        assertNotEmail("<testexample.com>");
        assertNotEmail("<test@ex#ample.com>");
        assertNotEmail("<test@>");
        assertNotEmail("<user@domain..com>");
        assertNotEmail("<user@domain!example.com>");
        assertNotEmail("<first\ndot@example.com>");
        assertNotEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb1234>");
        assertNotEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb123.aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb1234>");
        assertNotEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb1234.aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb123>");
        assertNotEmail("<test@aaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbbaaaaabbbbb12345.com>");
        assertNotEmail("<@example.com>");
        assertNotEmail("<john(doe)example@example.com>");
    }
}