package com.ims.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ims.security.jwt")
public class JwtProperties {

    /** Base64-encoded HMAC-SHA256 secret (>= 32 bytes after decoding). */
    private String secret;

    /** Token validity in milliseconds. */
    private long expirationMs = 3_600_000L;

    /** JWT issuer claim. */
    private String issuer = "ims-backend";
}
