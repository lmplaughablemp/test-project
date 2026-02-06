package com.example.bankcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Encryption encryption = new Encryption();
    private Security security = new Security();

    public Encryption getEncryption() {
        return encryption;
    }

    public void setEncryption(Encryption encryption) {
        this.encryption = encryption;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Encryption {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public static class Security {
        private Jwt jwt = new Jwt();
        private Cors cors = new Cors();

        public Jwt getJwt() {
            return jwt;
        }

        public void setJwt(Jwt jwt) {
            this.jwt = jwt;
        }

        public Cors getCors() {
            return cors;
        }

        public void setCors(Cors cors) {
            this.cors = cors;
        }

        public static class Jwt {
            private String secret;
            private long expiration;
            private long refreshExpiration;

            public String getSecret() {
                return secret;
            }

            public void setSecret(String secret) {
                this.secret = secret;
            }

            public long getExpiration() {
                return expiration;
            }

            public void setExpiration(long expiration) {
                this.expiration = expiration;
            }

            public long getRefreshExpiration() {
                return refreshExpiration;
            }

            public void setRefreshExpiration(long refreshExpiration) {
                this.refreshExpiration = refreshExpiration;
            }
        }

        public static class Cors {
            private String allowedOrigins;

            public String getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(String allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }
        }
    }
}