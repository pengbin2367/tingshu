package com.atguigu.tingshu;

//静态导入: 编译时引入

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/***
 * 专辑微服务的启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ServiceAlbumApplication {

    /**
     * ssm-->springMvc + spring(复习一下bean的声明周期--循环依赖问题?   )+ mybatis
     *  问题:
     *      1.pom.xml文件: 大量的jar包 和大量的版本号--->冲突问题
     *      2.配置文件: web.xml spring.xml springmvc.xml spring-mybatis.xml---><bean name="" class=""></bean>
     * springboot的原理
     *  起步依赖(编译时):
     *      pom.xml文件优化:
     *          引入父工程模块让开发人员忽略版本号问题,同时解决了冲突问题
     *          进行了jar包整合: spring-boot-starter-web
     *  自动装配:
     *      配置文件优化,删除全部配置文件--->
     *          优化为注解形式配置类+bean注解
     * @param args: 启动参数
     */
    public static void main(String[] args) {
        /**
         * 1.构建一个容器,但是容器初始状态是空的
         * 2.把启动类装进去,解析加载启动类的字节码文件
         * 3.识别到SpringBootApplication注解(生效于运行状态)
         *       SpringBootConfiguration:  标识启动类为一个配置类
         *       EnableAutoConfiguration: 将可能使用到的所有的bean全部完成初始化放入容器
         *          项目启动的时候,加载spring官方统计的全部可能常用的bean文件,一个个尝试实例化(反射机制),实例化成功的写入容器,失败的跳过下一个
         *       ComponentScan:包扫描-->启动类所在的包和子包下全部类的全部注解-->自定义的bean(controller  service mapper)写入到容器中
         */
        SpringApplication.run(ServiceAlbumApplication.class, args);
    }

}
