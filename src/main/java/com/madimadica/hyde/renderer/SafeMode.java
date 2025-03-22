package com.madimadica.hyde.renderer;

import java.util.List;

public class SafeMode {
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("png", "gif", "jpeg", "webp");
    private static final List<String> BANNED_PROTOCOLS = List.of("javascript:", "vbscript:", "file:");

    public static boolean isSafeHref(String href) {
        href = href.toLowerCase();
        if (href.startsWith("data:")) {
            // Only allow png, gif, jpeg, and webp
            if (href.startsWith("image/", 5)) {
                for (var target : ALLOWED_IMAGE_TYPES) {
                    if (href.startsWith(target, 11)) {
                        return true;
                    }
                }
            }
            return false;
        }
        for (var protocol : BANNED_PROTOCOLS) {
            if (href.startsWith(protocol)) {
                return false;
            }
        }
        return true;
    }
}
