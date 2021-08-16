# eslclient-springboot-starter

#### Description
FreeSWITCH ESL client for springboot starter

#### Software Architecture
Just built based on FreeSWITCH Event Socket Library. it implements springboot's starter and auto configure.


#### Installation

1.  Add dependency to pom.xml
```
<dependency>
    <groupId>top.wdcc</groupId>
    <artifactId>eslclient-spring-boot-starter</artifactId>
    <version>1.0-BETA</version>
</dependency>
```

#### Usage

- Configure FreeSWITCH ESL connection info
```
freeswitch:
  eslclient:
    host: freeswitch     # event socket host
    port: 8021           # event socket port
    password: ClueCon    # event socket password
    timeout-sec: 30000   # event socket timeout
    pool-size: 1         # event socket client pool size
```

- Add BACKGROUND_JOB event listener
```
/**
 * Listenning <code>BACKGROUND_JOB</code> event (<code>eventName</code> is optional in this case，just extends <code>EslBackgroundJobListener</code> and add annotation <code>EslListener</code>)
 */
@EslListener
public class DemoBackgroundJobListener implements EslBackgroundJobListener {
    private static final Logger logger = LoggerFactory.getLogger(DemoBackgroundJobListener.class);

    @Override
    public void onBackgroundResult(String jobUuid, boolean hasBody, List<String> bodyLines) {
        logger.info("job uuid: {}, has body: {}", jobUuid , hasBody);
        logger.info("received BGAPI result: \n{}", bodyLines);
    }
}
```

- Add normal event listener, event like API
```
/**
 * Listenning an event
 * <code>eventName, subClassName(optional) </code>
 */
@EslListener(eventName = "API")
public class DemoApiListener implements EslEventListener {
    private static final Logger logger = LoggerFactory.getLogger(DemoApiListener.class);
    @Override
    public void onEslEvent(String eventName, Map<String, String> eventMap) {
        logger.info("execute an API:{}, args:{}", eventMap.get("API-Command"), eventMap.get("API-Command-Argument"));
    }
}
```

- use EslClient and send command to FreeSWITCH
```
@Autowired
private EslClient eslClient;

public void test(){
    eslClient.api("sofia", "status");
}
```

#### Contribution

1.  Fork the repository
2.  Create dev branch
3.  Commit your code
4.  Create Pull Request
