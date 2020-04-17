package com.redhat.com.rhdg.service;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.redhat.com.rhdg.CacheListener;

import org.infinispan.commons.marshall.JavaSerializationMarshaller;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.infinispan.spring.embedded.session.configuration.EnableInfinispanEmbeddedHttpSession;
import org.infinispan.spring.starter.embedded.InfinispanCacheConfigurer;
import org.infinispan.spring.starter.embedded.InfinispanGlobalConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Add an Infinispan annotation here
@EnableInfinispanEmbeddedHttpSession
@Service
public class EmbeddedCacheService {

   // TODO: Add cacheConfigurer method here
   @Bean
   public InfinispanCacheConfigurer cacheConfigurer() {
      return manager -> {
         final Configuration ispnConfig = new ConfigurationBuilder()
               .clustering()
               .cacheMode(CacheMode.REPL_SYNC)
               .build();

         manager.defineConfiguration("sessions", ispnConfig);
         manager.getCache("sessions").addListener(new CacheListener());

      };
   }
   // TODO: Add globalCustomizer method here
   @Bean
   public InfinispanGlobalConfigurer globalCustomizer() {
      return () -> {
         GlobalConfigurationBuilder builder = GlobalConfigurationBuilder.defaultClusteredBuilder();
         builder.serialization().marshaller(new JavaSerializationMarshaller());
         builder.transport().clusterName("rhdg");
         builder.serialization().whiteList().addClass("org.springframework.session.MapSession");
         builder.serialization().whiteList().addRegexp("java.util.*");
         return builder.build();
      };
   }
   // TODO: Add SessionController class here
   @RestController
   static class SessionController {

 private int count = 0;

           @Autowired
           SpringEmbeddedCacheManager cacheManager;

           @RequestMapping("/session")
           public Map<String, String> createSession(HttpServletRequest request) {

    Map<String, String> result = new HashMap<>();

                   String sessionId = request.getSession().getId();
    result.put("created:", sessionId);
    result.put("active:", cacheManager.getCache("sessions").getNativeCache().keySet().toString());
    result.put("count:", String.valueOf(count));
    count++;

    return result;

   }

 @RequestMapping("/delete")
           public void deleteSession(HttpServletRequest request) {

    request.getSession().invalidate();
    count = 0;

           }
   }
}