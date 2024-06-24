package com.atguigu.tingshu.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;

@Configuration
public class LoginJwtPrivateKeyConfig {

    @Value("${encrypt.location}")
    private String location;
    @Value("${encrypt.secret}")
    private String secret;
    @Value("${encrypt.alias}")
    private String alias;
    @Value("${encrypt.password}")
    private String password;

    @Bean
    public RSAPrivateKey rsaPrivateKey() {
        // 密钥文件的工厂对象初始化
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(new ClassPathResource(location), secret.toCharArray());
        // 访问这个文件中的一对钥匙
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, password.toCharArray());
        // 获取私钥
        PrivateKey aPrivate = keyPair.getPrivate();
        // 返回注入容器
        return (RSAPrivateKey) aPrivate;
    }
}
