package top.wdcc.eslclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * Esl客户端配置参数
 */
@Configuration
@ConfigurationProperties(prefix = EslProperties.PREFIX)
public class EslProperties implements Serializable {

    private static final long serialVersionUID = -4599944442168056484L;

    public static final String PREFIX = "freeswitch.eslclient";
    /**
     * ESL主机地址
     */
    private String host;

    /**
     * ESL主机端口
     */
    private int port;

    /**
     * ESL密码
     */
    private String password;

    /**
     * ESL连接超时
     */
    private int timeoutSec;

    /**
     * ESL连接池大小
     */
    private int poolSize;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeoutSec() {
        return timeoutSec;
    }

    public void setTimeoutSec(int timeoutSec) {
        this.timeoutSec = timeoutSec;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    @Override
    public String toString() {
        return "EslAutoConfigurationProperties{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", password='" + password + '\'' +
                ", timeoutSec=" + timeoutSec +
                ", poolSize=" + poolSize +
                '}';
    }
}
