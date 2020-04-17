Embeded Cache with a cluster
In this lab we are going to cluster the embedded cache. Inifinispan does this very nicely. We do not need to change a lot of configuration to achieve this. After configuration, we will deploy our application to Openshift and see how the cluster will work in a cloud environment. Following diagram illustrates the topology we want to achieve. Although clustering cache nodes can be in 100s, we will start with a couple of them and see how we can get the feature set we need for a cache.

![Diagram](docs/images/clusteredembeddedcache.png)

Clustering
Open the ScoreService.java again in our project dg8-embedded-quarkus

We are going to add the following lines of code to our onStart method

        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
        global.transport().clusterName("ScoreCard");
        cacheManager = new DefaultCacheManager(global.build());
Copy the and replace the onStart method in our ScoreService.java

    void onStart(@Observes @Priority(value = 1) StartupEvent ev){
        GlobalConfigurationBuilder global = GlobalConfigurationBuilder.defaultClusteredBuilder();
        global.transport().clusterName("ScoreCard");
        cacheManager = new DefaultCacheManager(global.build());

        ConfigurationBuilder config = new ConfigurationBuilder();

        config.expiration().lifespan(5, TimeUnit.MINUTES)
                .clustering().cacheMode(CacheMode.DIST_SYNC);

        cacheManager.defineConfiguration("scoreboard", config.build());
        scoreCache = cacheManager.getCache("scoreboard");
        scoreCache.addListener(new CacheListener());


        log.info("Cache initialized");

    }
We define a global configuration, since we are going to be in a clustered mode, hence everytime a new instance of our app will be created it will have the global configuration parameters to talk to the other nodes if they are present.
Infinispan nodes rely on a transport layer to join and leave clusters as well as to replicate data across the network. Infinispan uses JGroups technology to handle cluster transport. You configure cluster transport with JGroups stacks, which define properties for either UDP or TCP protocols. Here we have defined a unique clusterName for our app’s embedded cache
We setup a distributed cache, which means that not all our nodes will have all the keys.
We will take a look into replicaiton and distribution furhter in this lab.

Also open the CacheListener and make sure that the following line is changed to @Listener(clustered = true)

@Listener
By doing this we are making sure we have a clustered listener. And that we can listen to the cross cluster events. so we know exactly in case of key distribution.

This all looks in order. lets compile our application.

It is suggested that at this moment you close all terminal windows that you might have opened in the previous labs. to keep a clear view of our lab

A quick look at our side bar menu on the right called MyWorkspace

cdw
We will use this menu through out the labs. There is a bunch of commands created specifically for this workshop.

First lets login to Openshift. You will find the button in the right corner in MyWorkspace menu. Click Login to Openshift

Lets run our project click on the Command Emebdded - Start Live Coding This will enable Live coding, it will open up a small terminal to build your artifact and then open up a browser view

Make sure you click on the Openlink

cdw
You can also click on the link icon in the browser view, which will open a browser tab.

cdw
If all of this is working lets make sure we can deploy this applicaiton.

First run the following command to add the Openshift extension for Quarkus The Openshift extension makes it easy to deploy your application to openshift, rather then taking all the different steps from an oc command line, you can do that through your maven build.

run the following in your terminal, you should see a build successfull message when done.

mvn quarkus:add-extension -Dextensions="openshift"
Now open the application.properties file in src/main/resources/application.properites

Add the following properties to it

quarkus.http.cors=true
quarkus.openshift.expose=true 

# if you dont set this and dont have a valid cert the deployment wont happen
quarkus.kubernetes-client.trust-certs=true 
The first property makes sure that once our application is deployed it will expose a route
The second property makes sure that incase you dont have valid certificates the build wont stop. in our case that can likely be the case since its not a production environment rather a demo one.
Now go to your MyWorkspace menu and Login to Openshift.

Perfect everything is inorder. Make sure you are logged into openshift. If you are not sure. You can run the following command in your terminal.

oc whoami
The command should return your user name: user3 , is you are logged in.

Lets first create an image namespace for our application

mvn clean package -Dquarkus.container-image.build=true
You should see a build successful message at the end. That mean everything worked out.

Now lets deploy our application to Openshift

mvn clean package -Dquarkus.kubernetes.deploy=true
Also remmember next time we need to deploy we just need to run the above deploy command again. thats all!

Lets wait for this build to be successfull!

Openshift Console
First, open a new browser with the OpenShift web console

![Diagram](docs/images/openshift_login.png)

Login using:

Username: user3

Password: openshift!

When you access the OpenShift web console or other URLs via HTTPS protocol, you might see browser warnings like Your Connection is not secure since this workshop uses self-signed certificates (which you should not do in production!). For example, if you’re using Chrome, to accept the warning, Click on Advanced then Proceed to…​ to access the page.

Other modern browsers most likely have similar procedures to accept the security exception.

You should see something as follows

![Diagram](docs/images/openshiftprojectview.png)

Click on the project name and you should see something similar

![Diagram](docs/images/lab2ocpoverview)

Click on resources, And at the bottom you will see the route to your application. You can also click at the route and it will take you to the application page, same as we have done in the previous lab. if append /api to the url you will be on the api endpoint.

Now go back to the overview page for the applicaiton and Click on the pod scaler and scale to 2 pods.

![Diagram](docs/images/lab2podscaler)

Now open another terminal in CodeReady workspaces and change to the scripts directory

cd dg8-embedded-quarkus/scripts
in this directory we have a load.sh file. Open this file in CodeReadyWorkspace and change the variable EP to the applicaiton route from the browser and run load.sh

./load.sh
Go back to the resrouce view of your application and then click view logs, you should see something as follows.

Node1:

![Diagram](docs/images/distsyncnode1)
Node2:

![Diagram](docs/images/distsyncnode2)

Notice in the above screenshots how the keys are distributed between the two nodes.

This wont be the case if we were replicating these entries. So lets go ahead and setup a replicated cache instead. Open the score service and change the Cache config as shown below. Remember the onStart method has the configuration.

        config.expiration().lifespan(5, TimeUnit.MINUTES)
                .clustering().cacheMode(CacheMode.REPL_SYNC)
Before we deploy we also need to make sure that the Listener is no longer listening to events clusterwide. we want to listen to events only on the node they are happening on. For this lets make a small config change to our Listener annotation in our class CacheListener

@Listener(primaryOnly = false)
It is possible in a non transactional cache to receive duplicate events. This is possible when the primary owner of a key goes down while trying to perform a write operation such as a put. Infinispan internally will rectify the put operation by sending it to the new primary owner for the given key automatically, however there are no guarantees in regards to if the write was first replicated to backups. Thus more than 1 of the following write events (CacheEntryCreatedEvent, CacheEntryModifiedEvent & CacheEntryRemovedEvent) may be sent on a single operation.

Perfect, now we are all set to deploy again.

Now lets deploy our application to Openshift

mvn clean package -Dquarkus.kubernetes.deploy=true
Scale the pods
run the load.sh script once the pods are running
Check the log files and you should see a similar output as below
![Diagram](docs/images/replicatedsyncnode)
Feel free to change the listner annotation in different modes and try out to see how the events are recieved.

Design Considerations
Firstly, p2p deployments are simpler than client-server ones because in p2p, all peers are equals to each other and this simplifies deployment. If this is the first time you are using Infinispan, p2p is likely to be easier for you to get going compared to client-server.

Client-server Infinispan requests are likely to take longer compared to p2p requests, due to the serialization and network cost in remote calls. So, this is an important factor to take in account when designing your application. For example, with replicated Infinispan caches, it might be more performant to have lightweight HTTP clients connecting to a server side application that accesses Infinispan in p2p mode, rather than having more heavyweight client side apps talking to Infinispan in client-server mode, particularly if data size handled is rather large. With distributed caches, the difference might not be so big because even in p2p deployments, you’re not guaranteed to have all data available locally.

Environments where application tier elasticity is not important, or where server side applications access state-transfer-disabled, replicated Infinispan cache instances are amongst scenarios where Infinispan p2p deployments can be more suited than client-server ones.

Congratulations we are at the end of this lab!

Recap
You created our own Cache and learnt how to us EmbeddedCacheManager
You learnt how to use ConfigurationBuilder and Configuration objects to define our Configurations for the Cache and CacheManager
You learnt about how to create and Embedded Cluster
You learnt how to deploy a Quarkus application with emebedded cache and scale it.
You learnt the difference between Replicated and Distributed Cache and how clustering and listeners works.
*Congratulations!! you have completed the second lab of this workshop. Lets move to the next lab and learn how we can create a remote cache and how it can benefit our applications.