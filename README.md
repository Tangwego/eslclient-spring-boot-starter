# eslclient-springboot-starter

#### 介绍
FreeSWITCH ESL springboot starter

#### 软件架构
基于FreeSWITCH事件库Event Socket Library的一个springboot starter实现


#### 安装教程

1.  pom.xml中添加依赖
```
<!-- https://mvnrepository.com/artifact/top.wdcc/eslclient-spring-boot-starter -->
<dependency>
    <groupId>top.wdcc</groupId>
    <artifactId>eslclient-spring-boot-starter</artifactId>
    <version>1.0.1-RELEASE</version>
</dependency>

```

#### 使用说明

- 配置FreeSWITCH ESL连接相关信息
```
freeswitch:
  eslclient:
    host: freeswitch     # event socket 主机地址
    port: 8021           # event socket 主机端口
    password: ClueCon    # event socket 主机认证密码
    timeout-sec: 30000   # event socket 连接超时
    pool-size: 1         # event socket 连接池大小
```

- 添加异步调用结果监听器
```
/**
 * 监听BACKGROUND_JOB事件 (eventName可选，只要继承了EslBackgroundJobListener并添加了EslListener注解即可)
 */
@EslListener
public class DemoBackgroundJobListener implements EslBackgroundJobListener {
    private static final Logger logger = LoggerFactory.getLogger(DemoBackgroundJobListener.class);

    @Override
    public void onBackgroundResult(String jobUuid, boolean hasBody, List<String> bodyLines) {
        logger.info("job uuid: {}, 是否有收到BGAPI结果: {}", jobUuid , hasBody);
        logger.info("收到的BGAPI结果: \n{}", bodyLines);
    }
}
```

- 添加事件监听器
```
/**
 * 监听事件
 * eventName填写需要关心的事件, subClassName填写事件子类(可选)
 */
@EslListener(eventName = "API")
public class DemoApiListener implements EslEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DemoApiListener.class);
    @Override
    public void onEslEvent(String eventName, Map<String, String> eventMap) {
        logger.info("执行了一个API:{}, 参数:{}", eventMap.get("API-Command"), eventMap.get("API-Command-Argument"));
    }
}
```

- 使用EslClient发送命令
```
@Autowired
private EslClient eslClient;

public void test(){
    eslClient.api("sofia", "status");
}
```

#### 参与贡献

1.  Fork 本仓库
2.  新建 dev 分支
3.  提交代码
4.  新建 Pull Request
