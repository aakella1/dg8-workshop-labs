Externalizing Sessions
In this lab, we’ll develop how to externalize HTTP Sessions from Spring Boot to Data Grid. It allows Data Grid Spring Session support is built on SpringRemoteCacheManager and SpringEmbeddedCacheManager which means developers don’t need to store HTTP session data in Data Grid manually for clustering the session data across multiple Spring Boot applications. Behind the scene, Data Grid will autowire Spring Boot Session to distributed caches in Data Grid.

1. Developing EmbeddedCache Service
The embeddedCache service allows Spring Boot application to emded the HttpSession data at in-memory data storage when users invoke RESTful endpoints in the frontend web page. Lets’s go through quickly how the embeddedCache service gets REST services to address Spring Session with In-Memory Cache with Red Hat Data Grid 8. Go to Explorer: /projects in CodeReady Workspaces Web IDE and expand dg8-spring-session directory.

embeddedCache
There’re a few interesting things what we need to take a look at this Spring Boot application before we will develop it in CodeReady Workspaces.

This embeddedCache service is not using the default BOM (Bill of material) that Spring Boot projects typically use. Instead, we are using an Infinispan BOM provided by Red Hat that provides a high-level API to ensure compatibility between major versions of Data Grid. You can also enforce a specific version of Data Grid with the infinispan-bom module. Let’s take a look at infinispan-bom to your pom.xml file as follows:

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.infinispan</groupId>
            <artifactId>infinispan-bom</artifactId>
            <version>${version.infinispan}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>javax.transaction</groupId>
            <artifactId>transaction-api</artifactId>
            <version>1.1</version>
        </dependency>
        <dependency>
            <groupId>javax.cache</groupId>
            <artifactId>cache-api</artifactId>
            <version>1.1.0.redhat-1</version>
        </dependency>
    </dependencies>
</dependencyManagement>
embeddedCache
In order to use Embedded Mode in Spring Boot, infinispan-spring-boot-starter-embedded dependency is already pulled in your pom.xml file. This starter produces a SpringEmbeddedCacheManager bean by default:

<dependency>
    <groupId>org.infinispan</groupId>
    <artifactId>infinispan-spring-boot-starter-embedded</artifactId>
    <version>2.2.3.Final-redhat-00001</version>
</dependency>
embeddedCache
Create an InfinispanCacheConfigurer bean to customize the cache manager. Open a Java class called EmbeddedCacheService.java in com.redhat.com.rhdg.service and copy below the // TODO: Add cacheConfigurer method here marker:

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
Copy below the // TODO: Add globalCustomizer method here marker to customize InfinispanGlobalConfigurer bean:

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
Next, Autowire cacheManager to SpringEmbeddedCacheManager then implment REST APIs to createSession, deleteSession. Copy below the // TODO: Add SessionController class here marker:

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
Add the @EnableInfinispanEmbeddedHttpSession annotation to EmbeddedCacheService to enable Spring Cache support. When this starter detects the EmbeddedCacheManager bean, it instantiates a new SpringEmbeddedCacheManager, which provides an implementation of Spring Cache.

Copy below the // TODO: Add an Infinispan annotation here marker:

@EnableInfinispanEmbeddedHttpSession
Perfect! Now we have all the building blocks ready to use the cache. Let’s start using our cache.

2. Deploying EmbeddedCache Service
Now we will build and deploy the project using the following command, which will use the maven plugin to deploy via CodeReady Workspaces Terminal:

mvn clean package spring-boot:repackage -f $CHE_PROJECTS_ROOT/cdg8-workshop/dg8-spring-session
Create a build configuration for your application using OpenJDK base container image in OpenShift:

oc new-build registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.5 --binary --name=cacheapp -l app=cacheapp
Start and watch the build, which will take about minutes to complete:

oc start-build cacheapp --from-file=target/rhdg-0.0.1-SNAPSHOT.jar --follow
Deploy it as an OpenShift application after the build is done:

oc new-app cacheapp && oc expose svc/cacheapp && \
oc label dc/cacheapp app.kubernetes.io/part-of=catalog app.openshift.io/runtime=spring --overwrite
Finally, make sure it’s actually done rolling out. Visit the Topology View for the cache service, and ensure you get the blue circles!

![Diagram](docs/images/embeddedCache-topology)

3. Testing EmbeddedCache Service
Let’s scale up the cache service to make sure if the clustered Spring applications refer to Spring Session in Data Grid. Click on Up Arrow once in Overview page:

![Diagram](docs/images/embeddedCache-scaleup-start)

Then you will see how the pod is scailing up:

![Diagram](docs/images/embeddedCache-scaleup-end)

Let’s go externalizing Spring Session to Data Grid! Access the Cache Service UI!

![Diagram](docs/images/embeddedCache-ui)

Click on Invoke the service then the created Spring Session ID is already stored at in-memory datagrid as active data in the Result box:

![Diagram](docs/images/embeddedCache-invoke1)
Open a new web browser window then access the the Cache Service UI.

Click on Invoke the service once again then you will see the exact same Spring Session ID and active data but the count is increased to 2. So two applications are clustered and refer to the *embedded Infinispan cache:

![Diagram](docs/images/embeddedCache-invoke2)
Go back to the first web browser then click on Clear the cache. Move to the second web browser then click on Invoke the service. You will see new Session ID, active data and the count is reset to 1 again:

![Diagram](docs/images/embeddedCache-invoke3)
Let’s double-check if the Spring Session is clustered in the all running pods. Go back to the Topology View and click on 'View logs' in the pods:

![Diagram](docs/images/embeddedCache-invoke4)
Now that we know how to react on changes in the cluster topology, we can also react to changes to the data within the cluster. The CacheListener separates the roles of our two pods such as putting data in the cache(-- Entry for CACHE_ENTRY_MODIFIED created) and showing the cache modifications(-- Entry for CACHE_ENTRY_MODIFIED modified):

![Diagram](docs/images/embeddedCache-logs)
We now have implemented Spring Session with embedded in-memory datagrid for clustering HTTP sessions across Spring Boot microservices. Congratulations!