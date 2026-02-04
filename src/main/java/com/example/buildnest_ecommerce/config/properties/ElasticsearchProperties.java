package com.example.buildnest_ecommerce.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {
    private boolean enabled = true;
    private String host;
    private int port;
    private String username;
    private String password;
    private final Ssl ssl = new Ssl();
    private final Alert alert = new Alert();
    private final Metrics metrics = new Metrics();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Ssl getSsl() {
        return ssl;
    }

    public Alert getAlert() {
        return alert;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public static class Ssl {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Alert {
        private boolean enabled = true;
        private int cpuThreshold;
        private int memoryThreshold;
        private int errorRateThreshold;
        private String webhookUrl;
        private final Email email = new Email();
        private final Slack slack = new Slack();
        private final Smtp smtp = new Smtp();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCpuThreshold() {
            return cpuThreshold;
        }

        public void setCpuThreshold(int cpuThreshold) {
            this.cpuThreshold = cpuThreshold;
        }

        public int getMemoryThreshold() {
            return memoryThreshold;
        }

        public void setMemoryThreshold(int memoryThreshold) {
            this.memoryThreshold = memoryThreshold;
        }

        public int getErrorRateThreshold() {
            return errorRateThreshold;
        }

        public void setErrorRateThreshold(int errorRateThreshold) {
            this.errorRateThreshold = errorRateThreshold;
        }

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public Email getEmail() {
            return email;
        }

        public Slack getSlack() {
            return slack;
        }

        public Smtp getSmtp() {
            return smtp;
        }

        public static class Email {
            private boolean enabled;
            private String to;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getTo() {
                return to;
            }

            public void setTo(String to) {
                this.to = to;
            }
        }

        public static class Slack {
            private boolean enabled;
            private String webhook;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public String getWebhook() {
                return webhook;
            }

            public void setWebhook(String webhook) {
                this.webhook = webhook;
            }
        }

        public static class Smtp {
            private String host;
            private int port;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }
    }

    public static class Metrics {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
