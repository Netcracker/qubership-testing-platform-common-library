# Usage notes

## Logging

### 1. Add dependency to pom.xml
```xml
<dependencies>
    ...
    <dependency>
        <groupId>org.qubership.atp.common</groupId>
        <artifactId>qubership-atp-common-logging</artifactId>
        <version>0.0.42</version>
    </dependency>
    ...
</dependencies>
```

### 2. Add parameters to application.properties
```properties
atp.logging.resttemplate.headers=${ATP_HTTP_LOGGING_HEADERS:true}
atp.logging.resttemplate.headers.ignore=${ATP_HTTP_LOGGING_HEADERS_IGNORE:}
atp.logging.feignclient.headers=${ATP_HTTP_LOGGING_HEADERS:true}
atp.logging.feignclient.headers.ignore=${ATP_HTTP_LOGGING_HEADERS_IGNORE:}
```
* By default, _atp.logging.resttemplate.headers_ value is false.
* _atp.logging.resttemplate.headers_ is used to log headers of requests/responses for RelayRestTemplate and M2MRestTemplate.
* _atp.logging.resttemplate.headers.ignore_ is used to ignore specified headers while logging. Tokens should be separated with spaces.
* _atp.logging.feignclient.headers_ is used to log headers of requests/responses for FeignClient.
* _atp.logging.feignclient.headers.ignore_ is used to ignore specified headers while logging for FeignClient. Tokens should be separated with spaces.
* Parameters _atp.logging.resttemplate.headers.ignore_ and _atp.logging.feignclient.headers.ignore_ support regular expressions.

### 3. Add configuration into logback.xml
```xml
<if condition='${ATP_HTTP_LOGGING}'>
    <then>
        <logger name="org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor" level="DEBUG" additivity="false">
            <appender-ref ref="ASYNC_GELF"/>
        </logger>
        <logger name="org.qubership.atp.catalogue.service.client.feign.DatasetFeignClient" level="DEBUG" additivity="false">
            <appender-ref ref="ASYNC_GELF"/>
        </logger>
    </then>
</if>
```

To turn ON logging on local machine, one needs to add the following options to JVM:
```properties
-Dlogging.level.org.qubership.atp.common.logging.interceptor.RestTemplateLogInterceptor=debug
-Dlogging.level.org.qubership.atp.catalogue.service.client.feign.DatasetFeignClient=debug
```

## Lock Manager

### 1. Dependency
Add dependency in pom
```xml
        <dependency>
            <groupId>org.qubership.atp.common</groupId>
            <artifactId>qubership-atp-common-lock-manager</artifactId>
            <version>0.0.42</version>
        </dependency>
```

### 2. Enable 
In Main class or new Configuration class add annotation `@EnableAtpLockManager`

### 3. Specify a provider
By default, it uses the InMemory provider. That is mean the LockManager stores the records about locks in memory of application.
There is possible to change provider to keep this records in database for example.
In order that, create Configuration class and define the LockProvider bean.
Example for mongodb:

1\. Add dependency
```xml
        <dependency>
            <groupId>net.javacrumbs.shedlock</groupId>
            <artifactId>shedlock-provider-mongo</artifactId>
            <version>4.12.0</version>
        </dependency>
```
2\. Add Configuration class
```java
@Configuration
@EnableAtpLockManager
public class LockManagerConfig {

    // some code

    @Bean
    public LockProvider lockProvider(MongoClient mongoClient) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(getDatabaseName());
        return new MongoLockProvider(mongoDatabase);
    }
}
```

If your application uses jdbc, then use appropriate dependency and rule for setting up the configuration.
 ```xml
        <dependency>
            <groupId>net.javacrumbs.shedlock</groupId>
            <artifactId>shedlock-provider-jdbc-template</artifactId>
            <version>4.12.0</version>
        </dependency>
 ```

For all list of available implementation of provider see page https://github.com/lukas-krecan/ShedLock#configure-lockprovider
(The LockManager is based on the ShedLock library)

### 4. Use lock in your code
In order to do something and make lock so that another thread is waiting completeness first one
do the following :

1\. add LockManager in your service
```java
@Autowired
private LockManager lockManager;
```
2\. Call execute executeWithLock
```text
lockManager.executeWithLock(lockName, () -> {<do something>});
```

### 5. Application properties
```properties
##=============Lock Manager========================
atp.lock.default.duration.sec=${LOCK_DEFAULT_DURATION_SEC:60}
atp.lock.retry.timeout.sec=${LOCK_RETRY_TIMEOUT_SEC:10800}
atp.lock.retry.pace.sec=${LOCK_RETRY_PACE_SEC:3}
```

* _atp.lock.default.duration.sec_ - duration of retention an acquired lock in case it is not released.
* _atp.lock.retry.timeout.sec_ - duration for the Lock Manager during which it is trying to acquire a lock.
* _atp.lock.retry.pace.sec_ - pause between two retries of acquiring a lock.
