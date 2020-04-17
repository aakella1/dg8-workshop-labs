Caching 101, Lets get started with your first app
What could be cases where you want to use Cache; Lets take a moment and think about it. Where do you think you could use cache? Well there could be limitless answers to this question; Some common usecases are listed below

Lookup Data; if you have an app, that needs some user profile data etc, thats unecessary to pole everytime, and it doesnt change much

Latency or bulk; you might have a service or database that takes alot of time to load some of the data.

Traffic; you might have loads of users and trends are spiking

Session Storage; Storing your webapp sessions, this could be carts etc, that you can use to scale your application

Global Counters; You might want to create distributed keys accross a distributed dataset. Use this to update and fetch data.

![Diagram](docs/images/embeddedcache.png)

The above figure depicts a common scenario when you dont want to use a networked dataset that could be slow and with high latency, but rather use an inmemory cache. In this case the in-memory cache becomes the front for the data.

This lab is about an embedded or local cache, also a common usecase for applications. What is an Embedded Cache in Red Hat Data Grid?

Embedded Cache, Some basics
The CacheManager is Red Hat Data Grid’s main entry point. You can use a CacheManager to

configure and obtain caches

manage and monitor your nodes

execute code across a cluster

Depending on whether you are embedding Red Hat Data Grid in your application or you are using it remotely, you will be dealing with either an EmbeddedCacheManager or a RemoteCacheManager. While they share some methods and properties, be aware that there are semantic differences between them.

CacheManagers are heavyweight objects, and recommended use would be one CacheManager used per JVM (unless specific setups require more than one; but either way, this should be a minimal and finite number of instances).

The simplest way to create a CacheManager is:

EmbeddedCacheManager manager = new DefaultCacheManager();
which starts the most basic, local mode, non-clustered cache manager with no caches. CacheManagers have a lifecycle and the default constructors also call start(). Overloaded versions of the constructors are available, that do not start the CacheManager, although keep in mind that CacheManagers need to be started before they can be used to create Cache instances.

Once constructed, CacheManagers should be made available to any component that require to interact with it via some form of application-wide scope such as JNDI, a ServletContext or via some other mechanism such as an IoC container.

When you are done with a CacheManager, you must stop it so that it can release its resources:

manager.stop();
This will ensure all caches within its scope are properly stopped, thread pools are shutdown. If the CacheManager was clustered it will also leave the cluster gracefully.

Your first service with Caching
One of the common usecases for a Cache is to keep most used data in memory. Example having a Scoreboard in the cache. Lets assume theres a webpage that keeps the Score card for a round played by players on different tours. Now since this website expects people coming to check the top scores for example, or maybe based on a country etc. The best approach would be store this information in a cache rather then polling that information from different webservices or different databases as an example.

In our first service we will do exactly that. We will store this data in our Embedded Cache and understand not only how this works but the different ways of handling cache, getting events from it etc.

Project details
You can choose multiple runtimes to impelement this service, in our case today the example we have taken is with Quarkus. Quarkus is a Kubernetes Native Java stack tailored for OpenJDK HotSpot and GraalVM, crafted from the best of breed Java libraries and standards. Quarkus is also known for some of its cool features e.g Live Coding which we will also use in our labs. It makes it easier to code and see our changes as we do that.

Navigate to the project dg8-embedded-quarkus This is a template project, and you will be writing code into this project. As you can see there is already some files inplace. Lets take a look into what these files are and do.

The Maven dependencies
Open the pom.xml file in the project.

We will be using the following dependencies to create our service

    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy</artifactId> 
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-resteasy-jsonb</artifactId> 
    </dependency>
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-infinispan-embedded</artifactId> 
    </dependency>
    <dependency>
Quarkus-resteasy; for our REST endpoint
Quarkus-resteasy-jsonb; we will use this for Json serialization for our REST endpoint
Quarkus-infinispan-embedded; This extension will enable us to embed our cache in our service.
The Score Entity
We have also created a POJO called Score, which will serve as our datastructure for the ScoreCard. If you have played golf, you might wonder this is a very basic data structure and that’s entirely true, we could have gone in more details but we have kept this short to cover all the features. And you are welcome to extending this datastructure after successfully finishing these labs.

If you open Score.java you will see the following first few lines

    // The number of holes played per round
    public static final int HOLES = 18;

    // The players is on this hole
    private int currentHole = 0;

    // Name of the player
    private String playerName;

    // players unique Id
    private String playerId;

    // The actual scoreCard
    private int[] card = new int[HOLES];

    // The course player is playing on.
    private String course = "St.Andrews Links";

    // the courseCard; the expected handicap
    private int[] courseCard = {4,4,4,4,5,4,4,3,4,4,3,4,4,5,4,4,4,4};
The rest of the methods are accessors for these fields. Important to mention we do have three constructors

    // Used in Json serialization
    public Score()

    // Creating a new player with course and the courses score card
    public Score(String playerName, String playerId, String course, int[] courseCard)

    // Creating a new player with defaults
    public Score(String playerName, String playerId)
Take a look at some of the other methods in the Score class and make yourself familiar with it. Do not change the class at this time.

Creating a service for caching
So now that you are familiar with the project template, lets start by creating a service. Todo this open ScoreService.java

Define the following three class level variables

    Cache<Object, Score> scoreCache; 

    Logger log = LoggerFactory.getLogger(ScoreService.class); 

    @Inject
    EmbeddedCacheManager cacheManager; 
the scoreCache is an instance of Cache, which will be our point to store and retrieve values. Cache expects <K,V> types, in our case our key is an Object and our actual entry is a Score. Yes the same Score POJO we say earlier. The Cache is also the central interface of Red Hat Data Grid. A Cache provides a highly concurrent, optionally distributed data structure with additional features such as; JTA transaction compatibility, Eviction support for evicting entries from memory to prevent OutOfMemoryErrors, Persisting entries to a CacheLoader, either when they are evicted as an overflow, or all the time, to maintain persistent copies that would withstand server failure or restarts. For convenience, Cache extends ConcurrentMap and implements all methods accordingly. Methods like keySet(), values() and entrySet() produce backing collections in that updates done to them also update the original Cache instance. Certain methods on these maps can be expensive however (prohibitively so when using a distributed cache). The size() and Map.containsValue(Object) methods upon invocation can also be expensive just as well. The reason these methods are expensive are that they take into account entries stored in a configured CacheLoader and remote entries when using a distributed cache.
the log; straight forward logger incase we want to log something.
cacheManager; which is an instance of EmbeddedCacheManager, we inject this into our code using the dependency injection and this is possible due to the extension we added in our maven dependencies.
Next let’s create some accessor methods for our service.

    public List<Score> getAll() { 
        return new ArrayList<>(scoreCache.values());
    }

    public void save(Score entry) { 
        scoreCache.put(getKey(entry), entry);
    }

    public void delete(Score entry) { 
        scoreCache.remove(getKey(entry));
    }

    public void getEntry(Score entry){ 
        scoreCache.get(getKey(entry));
    }
We get all values from the cache and return them as a List of Scores
We are saving the entire entry, which we expect as a Score object.
We are deleting an entry from our cache
Finally we want to get 1 entry from our cache.
These are simple accessor methods, one thing you might have noticed is the use of the method getKey. This method described as follows has one simple task i.e. to get us the key, which in our case we use as a concatenated string of playerId+course. Since entry always has both of these values we concatenate them here.

Add the following methods to your class as well.

    public static String getKey(Score entry){
        return entry.getPlayerId()+","+entry.getCourse();
    }

    public Score findById(String key) {
        return scoreCache.get(key);
    }
to get the key, so we have the right combination when we get an entry request to our cache
find the entry in our cache incase we get a getOne request from the resource
Perfect! Almost to our final step for this service. What we are missing is initialization of our CacheManager and then we need to ask the CacheManager to give us a new cache.

The CacheManager has many purposes: - acts as a container for caches and controls their lifecycle - manages global configuration and common data structures and resources (e.g. thread pools) - manages clustering

A CacheManager is a fairly heavy-weight component, and you will probably want to initialize it early on in your application lifecycle. For that reason we use the onStart method in this Service to ensure that the CacheManager and Cache are both created at startup. This also benefits us when we change this to clustering mode, more on that in our next lab.

    void onStart(@Observes @Priority(value = 1) StartupEvent ev){
        cacheManager = new DefaultCacheManager(); 
        ConfigurationBuilder config = new ConfigurationBuilder(); 

        cacheManager.defineConfiguration("scoreboard", config.build()); 
        scoreCache = cacheManager.getCache("scoreboard"); 

        log.info("Cache initialized");

    }
Constructing a CacheManager is done via one of its constructors, which optionally take in a Configuration or a path or URL to a configuration XML file. In our current config we do not need to add much, but use the defaults
We use defaults for the Configuration builder. its a very handy Object that enables us to define different cache configurations which we will notice further on in this lab.
We are passing our configuration to the CacheManager.
You obtain Cache instances from the CacheManager by using one of the overloaded getCache(), methods. Note that with getCache(), there is no guarantee that the instance you get is brand-new and empty, since caches are named and shared. Because of this, the CacheManager also acts as a repository of Caches, and is an effective mechanism of looking up or creating Caches on demand. In our case we expect this to be the first Cache and local embedded one. This is also not clustered.
You might have noticed, that a CacheManager can have multiple Caches; which is great, since in any application you could store multiple unrelated data in different caches, not just that you might even want to have different behaviour with different Caches, e.g. Eviction or Expiration could differ etc. This gives us a lot more to work with then we would in a ConcurrentHashMap as an example.

Creating a REST Resource for our app
Lets create our REST resource. This should be simple. Open the ScoreResource.java file. Since we already implemented most of our code in the service, we need to make sure we can respond on the correct REST calls.

First lets inject our ScoreService so we can use all the caching functions we need.

    @Inject
    ScoreService scoreService;
Lets implement the create end point, here we are simply calling the save function on the scoreService.

    @POST
    @Transactional
    public Response create(@Valid Score item) {
        scoreService.save(item);
        return Response.status(Status.CREATED).entity(item).build();
    }
And we also want to be able to get one entry from our cache. following method will do that by calling the scoreService.findById

    @GET
    @Path("/{id}")
    public Object getOne(@PathParam("id") String id) {
        Object entity = scoreService.findById(id);
        if (entity == null) {
            throw new WebApplicationException("ScoreCard with id of " + id + " does not exist.", Status.NOT_FOUND);
        }
        return entity;
    }

    @GET
    public List<Score> getAll() {
        return scoreService.getAll();
    }
And incase we wanted to update an entry. that would normally the case when we the player is playing the round. so the score will be updated.

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@Valid Score card, @PathParam("id") Long id) {
        scoreService.save(card);
        return Response.status(Status.CREATED).entity(card).build();

    }
Take a look into some of the other methods in the ScoreResource to make your self familiar with the code there.

If you might have noticed at the class declaration we are using the following annotations

@Produces(MediaType.APPLICATION_JSON) 
@Consumes(MediaType.APPLICATION_JSON) 
@Path("/api") 
This means we are producing JSON from our responses
This means we only listen to JSON, this helps us to consume the JSON directly and serialize it into our Score POJO as an example.
and api is the path to our resource. e.g. localhost:8080/api
It is suggested that at this moment you close all terminal windows that you might have opened in the previous labs. to keep a clear view of our lab

Run the Service
A quick look at our side bar menu on the right called MyWorkspace

Embedded > "Start Live Coding" in tasks.json
mvn clean compile quarkus:dev -f ${CHE_PROJECTS_ROOT}/dg8-workshop/dg8-embedded-quarkus

We will use this menu through out the labs. There is a bunch of commands created specifically for this workshop.

First lets login to Openshift. You will find the button in the right corner in MyWorkspace menu. Click Login to Openshift

Lets run our project click on the Command Emebdded - Start Live Coding This will enable Live coding, it will open up a small terminal to build your artifact and then open up a browser view

open localhost:8080

You can also click on the link icon in the browser view, which will open a browser tab.

cdw
Now open another terminal and change to the scripts directory

cd dg8-embedded-quarkus/scripts
now open a terminal form the MyWorkspace menu move to our project directory cd dg8-embedded-quarkus which should be in project root/dg8-workshop/

cd dg8-embedded-quarkus
in the scripts directory we have a load.sh file. Open this file in CodeReadyWorkspace and change the variable EP to the applicaiton route from the browser, you can copy it from the browser view in CodeReady or the tab that was openend when you ran the Live Coding. (assuming live coding is still on) and run load.sh , you can run this file

./load.sh
Hit the URL again on the browser veiw and you will see some scores updated. Now these scores were posted directly <1> via our ScoreResource <2> into our ScoreService <3> and passed into the cache

We just created a bunch of POST requests, to create a bunch of scores. The way the algorithm is working is that , we assume the score card is updated after every hole. or at the end. so if you place the data

card:5,4,4,4,3,5,0,0,0,0,0,0,0,0,0,0,0,0
course:Bethapage
currentHole:6
playerId:2
playerName:James
In this case, we are still doing a POST request like before, however the cache is checking whats coming in the put request, it will create the combination key like we have in our Score service i.e. PlayerId+Course and put the new data in it. This means that again its just one call to make this update, no lookups were needed specifically to perform before updating.

Try this data entry again, and this time change the course to Firestone, and you will notice that there will be a new entry for James. So now James will have two rounds on the scoreboard.

Its important to know what our key is and its important to find the right combination of what kind of key our data should posses when it comes to a Cache.

Lets do that, enter this data in the form in your browser view and press save, it will updated James’s round score.

If you goto your endpoint/api which should be route of your app/api in the browser you will also see the same JSON data there as well.

So what we have successfully done so far. Read, Write and update our Cache.

Lets move on to the next step and do some more interesing additions to our project.

Since we are using the Live Coding mode here, at any time if you terminate or restart the session it will clear the cache.

Expiration of Entries
Lets assume you are pulling this data off from a database. You might want that it should be removed from the cache after a certain time period. You can do this by defining this either on the a single entry or the entire cache. By default entries created are immortal and do not have a lifespan or maximum idle time. Using the cache API, mortal entries can be created with lifespans and/or maximum idle times

Expiration is a top-level construct, represented in the configuration as well as in the cache API. - While eviction is local to each cache instance , expiration is cluster-wide . Expiration lifespan and maxIdle values are replicated along with the cache entry. - Maximum idle times for cache entries require additional network messages in clustered environments. For this reason, setting maxIdle in clustered caches can result in slower operation times. - Expiration lifespan and maxIdle are also persisted in CacheStores, so this information survives eviction/passivation.

Lets start with doing this for one entry.

In Infinispan entry expiration can happen in two ways:

a certain time after the data was inserted into the cache (i.e. lifespan)

a certain time after the data was last accessed (i.e. maximum idle time)

The Cache interface offers overloaded versions of the put() method that allow specifying either or both expiration properties. The following example shows how to insert an entry which will expire after 5 seconds

Open the ScoreService and change the save method to the following.

    public void save(Score entry) {
        scoreCache.put(getKey(entry), entry, 5, TimeUnit.SECONDS);
    }
In the above code, we have used TimeUnit and we specify 5 as the unit which is seconds. Following are the units you can use in the TimeUnit

    NANOSECONDS,
    MICROSECONDS,
    MILLISECONDS,
    SECONDS,
    MINUTES,
    HOURS,
    DAYS;
Okay now its time to test this change. Go back into the terminal and run load.sh

./load.sh
Refresh your browser view right away. and you will see the entries again. Now wait for 5 seconds and refresh again. You will see the entries have expired. This is becuase we set the timespan to 5 seconds.

In the previous step we used the overloaded put() method to store mortal entries. But since we want all of our entries to expire with the same lifespan, we can configure the cache to have default expiration values. To do this we will construct the DefaultCacheManager by passing in a org.infinispan.configuration.cache.Configuration object. A configuration in Infinispan is mostly immutable, aside from some runtime-tunable parameters, and is constructed by means of a ConfigurationBuilder. Using the above use-case, let’s create a cache configuration where we want to set the default expiration of entries to 5 seconds.

Add the following line to your ScoreService onStart method; right under the ConfigurationBuilder instantiation

    config.expiration().lifespan(5, TimeUnit.SECONDS);
Also change the save implementation back to the following

    public void save(Score entry) {
        scoreCache.put(getKey(entry), entry);
    }
and run load.sh

./load.sh
Refresh your browser view right away. and you will see the entries again. Now wait for 5 seconds and refresh again. You will see the entries have expired. This is becuase we set the timespan to 5 seconds for the CacheManager.

Now this is a configuration change for the cache and this will expire all entries after 5 seconds.

Challenge yourself: Next task for you is to change the lifespan to 5 minutes and see if that works for you.

When an entry expires it resides in the data container or cache store until it is accessed again by a user request. An expiration reaper is also available to check for expired entries and remove them at a configurable interval of milliseconds. More information can be found in the Product documentation

Eviction
Red Hat Data Grid supports eviction of entries, such that you do not run out of memory. Eviction is typically used in conjunction with a cache store, so that entries are not permanently lost when evicted, since eviction only removes entries from memory and not from cache stores or the rest of the cluster. Red Hat Data Grid supports storing data in a few different formats. Data can be stored as the object iself, binary as a byte[], and off-heap which stores the byte[] in native memory.

Eviction occurs on a local basis, and is not cluster-wide. Each node runs an eviction thread to analyse the contents of its in-memory container and decide what to evict. Eviction does not take into account the amount of free memory in the JVM as threshold to starts evicting entries. You have to set size attribute of the eviction element to be greater than zero in order for eviction to be turned on. If size is too large you can run out of memory. The size attribute will probably take some tuning in each use case.

Difference between Eviction and Expiration
Both Eviction and Expiration are means of cleaning the cache of unused entries and thus guarding the heap against OutOfMemory exceptions, so now a brief explanation of the difference.

With eviction you set maximal number of entries you want to keep in the cache and if this limit is exceeded, some candidates are found to be removed according to a choosen eviction strategy (LRU, LIRS, etc…​). Eviction can be setup to work with passivation, which is eviction to a cache store.

With expiration you set time criteria for entries to specify how long you want to keep them in the cache.

lifespan Specifies how long entries can remain in the cache before they expire. The default value is -1, which is unlimited time.

maximum idle time Specifies how long entries can remain idle before they expire. An entry in the cache is idle when no operation is performed with the key. The default value is -1, which is unlimited time.

Perfect now we know what eviction and expiration API we have at our disposal and how we can use them in our app.

Add the following line to your ScoreService onStart method; right under the ConfigurationBuilder instantiation; in our example below we are going to limit our Cache to only 2 entries, anything above that will not be added to the Cache.

    config.memory().size(2).build();
and run load.sh

./load.sh
Refresh your browser view right away. and you will see the entries again. But this time note that there are only two entries. And thats what we had specified in our Cache configuration.

Listeners
Red Hat Data Grid offers a listener API, where clients can register for and get notified when events take place. This annotation-driven API applies to 2 different levels: cache level events and cache manager level events.

Events trigger a notification which is dispatched to listeners. Listeners are simple POJO s annotated with @Listener and registered using the methods defined in the Listenable interface.

Both Cache and CacheManager implement Listenable, which means you can attach listeners to either a cache or a cache manager, to receive either cache-level or cache manager-level notifications.

Implement a new class CacheListener

1 Create a new Java file, by right clicking on your project’s package name i.e. acme, also shown in the screenshot below

cdw
2 Next specifiy the name of the file CacheListener.java , also in the screenshot below

cdw
package org.acme;

import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

@Listener
public class CacheListener {

    @CacheEntryCreated
    public void entryCreated(CacheEntryCreatedEvent<String, Score> event) {
        System.out.printf("-- Entry for %s created \n", event.getType());
    }

    @CacheEntryModified
    public void entryUpdated(CacheEntryModifiedEvent<String, Score> event){
        System.out.printf("-- Entry for %s modified\n", event.getType());
    }
}
Also important is to add this listener to our Cache configuration. Add the following line to the config

    scoreCache.addListener(new CacheListener());
Now if we update the entries in our cache or create new ones; we will see a notification on the our console. Lets test this out.

Run load.sh

./load.sh
Now check the terminals tab where it says Embedded - Live Coding you should messages like follows

2020-04-16 09:29:38,664 INFO  [org.acm.ScoreService] (vert.x-worker-thread-3) Cache initialized
2020-04-16 09:29:38,665 INFO  [io.quarkus] (vert.x-worker-thread-3) jcache-quarkus 1.0.0-SNAPSHOT (powered by Quarkus 1.3.2.Final) started in 0.074s. Listening on: http://0.0.0.0:8080
2020-04-16 09:29:38,665 INFO  [io.quarkus] (vert.x-worker-thread-3) Profile dev activated. Live Coding activated.
2020-04-16 09:29:38,665 INFO  [io.quarkus] (vert.x-worker-thread-3) Installed features: [cdi, infinispan-embedded, kubernetes, resteasy, resteasy-jsonb, smallrye-metrics]
2020-04-16 09:29:38,666 INFO  [io.qua.dev] (vert.x-worker-thread-3) Hot replace total time: 0.371s
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_CREATED created
-- Entry for CACHE_ENTRY_MODIFIED modified
if you start to re run the load.sh a couple of times, you will start to see the modified messages more frequently. Assuming that the lifespan of the cache is more then 5 seconds.

Congratulations we are at the end of this lab!

Recap
You created our own Cache and learnt how to us EmbeddedCacheManager
You learnt how to use ConfigurationBuilder and Configuration objects to define our Configurations for the Cache and CacheManager
You learnt about Expiration and Eviction
And lastly you implemented your own Listener.
*Congratulations!! you have completed the first lab of this workshop. Lets move to the next lab and learn how we can cluster this Cache and also deploy this on a cloud environment like Openshift.