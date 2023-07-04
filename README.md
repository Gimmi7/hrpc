[TOC]

hrpc(http-rpc),基于http的java rpc框架。

### Usecase
- gracefully export servive to others
- eliminate template code for built rpc service


## 1.Guideline

### 1.1 Prerequisite
Maven dependency
```
        <dependency>
            <groupId>com.shareit.live</groupId>
            <artifactId>spring-boot-starter-hrpc</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```
### 1.2 Creating rpc service interface
```
@HrpcService(name = "example")
public interface ExampleService {
    String sayHi(String userName);
}
```
### 1.3 Implements hrpc service interface at server side
```
@Service
public class ExampleServiceImpl implements ExampleService {
    @Override
    public String sayHi(String userName) {
        return "Hi " + userName;
    }
}
```
enable hrpc server, by config application.yml
```
hrpc:
  enable: true
```
### 1.4 Config client yml & scan hrpc service at client side
application.yml
```
hrpc:
  remote-servers:
    - name: example
      servers: http://127.0.0.1:8080
```
scan hrpc service
```
@HrpcServiceScan(basePackages = {"com.shareit.live.example"})
@SpringBootApplication
public class WsClientDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsClientDemoApplication.class, args);
	}

}
```
### 1.5 Client invoke service
```
@Service
public class TestService {
    @Autowired
    private ExampleService exampleService;

    public void test() {
        String userName = "test_user";
        String greeting = exampleService.sayHi(userName);
        System.out.println(greeting);
    }
}
```

## 2.序列化框架对比

不同类型的请求参数，从client端经框架序列化后传输到server再由框架反序列；不同框架的结果对比
### 2.1 对比fastjson和protostuff
请求参数类型 | fastjson | protostuff |
:-:         | :-:      | :-:       |
java.lang.String | java.lang.String | java.lang.String 
int | java.lang.Integer | java.lang.Integer 
byte | java.lang.Byte | java.lang.Byte 
byte[] | byte[] | byte[] 
int[][] | com.alibaba.fastjson.JSONArray(error) | int[][] 
com.shareit.live.proto.ReqMsg | com.alibaba.fastjson.JSONArray(error) | com.shareit.live.proto.ReqMsg 
com.shareit.live.fecade.domain.UserModel[] | com.alibaba.fastjson.JSONArray(error) | com.shareit.live.fecade.domain.UserModel[] 
int[] | com.alibaba.fastjson.JSONArray(error) | int[] 
java.lang.Integer[] | com.alibaba.fastjson.JSONArray(error)| java.lang.Integer[] 
java.util.List | com.alibaba.fastjson.JSONArray | java.util.ArrayList 
java.util.Set | java.util.HashSet | java.util.HashSet



```
public class HrpcRequest {

    /**
     * implemented interface
     */
    private String clazz;

    /**
     * method name
     */
    private String method;

    /**
     * args of method
     */
    private Object[] args;

    /**
     * arg types
     */
    private Class<?>[] argTypes;

}
上面所有的请求参数都是赋值给Object[]后，再交给序列化框架处理；
fastjson在处理Object是需要使用autoType类型；
上面的测试都是开启了autoType的，所以fastjson能够处理java.util.Set；
不过fastjson将除byte[]以外的所有数组类型处理为jsonArray;
导致反射调用时抛出异常java.lang.IllegalArgumentException:
argument type mismatch] with root cause ；
java.util.List没有抛异常，完全是因为jsonArray implemts List<Object>的原因，
其他数组全都参数不匹配；
很明显,fastjson将所有数组都用jsonArray来处理，然后硬编码了byte[];


另外fastjson不能处理protobuf生成的java类: com.alibaba.fastjson.JSONException:
default constructor not found.
class com.google.protobuf.Descriptors$FieldDescriptor] with root cause;
fastjson反序列化依赖无参构造函数进行类的初始化，protobuf不走这一套；
```
### 2.2 排除Hessian的原因
```
Hessian 要求序列化的类必须实现 java.io.Serializable接口，这个要求对入参，返回值，嵌套类型的限制太大；
```

### 2.3 why protostuff
- 通过了所有的参数序列化反序列测试；
- 支持protobuf;

### 2.4 protostuff对model不一致的处理情况
* protostuff 当前版本支持forward-compatible,不支持backward-compatible ;
* pojo 的field数量可以不一致，多的fileds不处理，缺少的fields使用缺省值；
* pojo 的同名field的类型必须一致，不然无法进行反序列化
* pojo fields的顺序不一致会导致seq为负数，无法解析
* 不能处理repeated protobuf: 如果设置 enable morph capability globally,会导致和protobuf不兼容；https://github.com/protostuff/protostuff/issues/240

## 3.Performance test

### 3.1 Test execution environment:
```
CPU: Intel(R) Core(TM) i7-8565U CPU @ 1.80GHz 1.99 GHz
MEM: 8.00GB
JDK Version: Java HotSpot(TM) 64-Bit Server VM "1.8.0_211"

JVM Args:
-Xms128m
-Xmx750m
-XX:ReservedCodeCacheSize=240m
-XX:+UseConcMarkSweepGC
-XX:SoftRefLRUPolicyMSPerMB=50
-ea
-XX:CICompilerCount=2
-Dsun.io.useCanonPrefixCache=false
-Djdk.http.auth.tunneling.disabledSchemes=""
-XX:+HeapDumpOnOutOfMemoryError
-XX:-OmitStackTraceInFastThrow
-Djdk.attach.allowAttachSelf=true
-Dkotlinx.coroutines.debug=off
-Djdk.module.illegalAccess.silent=true
```
### 3.2 Invocation performance test reuslt ####Java 8

Spring AOP uses either JDK dynamic proxies or CGLIB to create the proxy for a given target object.
[Spring proxy doc](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html#:~:text=JDK%20dynamic%20proxies%20are%20built,the%20target%20type%20are%20proxied.)

```
-------------------------
| Invoke number: 100000  |
-------------------------
```
args | framework | time cost |
:-:  |  :-:      |  :-:      |
void | hrpc-jdk-dynamic-proxy  | 1.79548 ms
void | hrpc-cglib-proxy  | 1.88901 ms
void | plain-http   |  0.21996 ms
pojo | hrpc-jdk-dynamic-proxy | 1.84397 ms
pojo | hrpc-cglib-proxy  | 1.84491 ms
pojo | plain-http | 1.68574 ms


## 4.Load Balance
**hrpc support a default simple random load balance strategy;**
**At the same time, you can implement custom load balance strategy**
**follow steps below to implement custom load balance strategy:**
```
1. config serverFetchBeanName with @HrpcServece, e.g. @HrpcService(name = "fecade", serverFetchBeanName = "serverFetch")
2. support a spring bean which implements interface ServerFetch and with beanName configed in step 1, e.g. 
@Service(value = "serverFetch")
public class HrpcServerFetch implements ServerFetch {
    @Override
    public String fetch(String name) {
        int random = RandomUtils.nextInt(0, 10);
        System.out.println("random=" + random);
        if (random > 5) {
            return "http://127.0.0.1:2048";
        } else {
            return "http://localhost:45677";
        }
    }
}
```

## 5.TODO LIST
* 当前hrpc的客户端默认重试配置为 maxAttempts=2, backoff=200ms，后续支持可配置重试参数
