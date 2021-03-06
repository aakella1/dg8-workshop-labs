# Preface

What happens when you run your applications distributed, scaled and in the cloud …​
You are a developer or an architect creating world-class applications and solutions. Well, all applications don’t necessarily live in just one box, and for the recent past, Cloud is not just one more computer but a lot more. All applications use a network to speak to each other, whether they speak to a database, a web service or storage, or other services. That’s the world we all live in!

Creating a distributed application means you care about latency, performance, and, most importantly, the responsiveness to your end-users. End users could be systems or humans; whats important is that you have a timely and consistent response. If that’s the challenging world we all live in, don’t worry, there are a lot many things one can do to optimize the stack. Most importantly, this workshop focuses on one thing i.e. Red Hat Data Grid.

So what is Red Hat Data Grid? Red Hat Data Grid 8.0 provides a distributed in-memory, NoSQL datastore solution. Your applications can access, process, and analyze data at in-memory speed to deliver a superior user experience. Whether you are using legacy applications or a new breed of microservices and functions, Red Hat Data Grid 8.0 will enable your applications to perform better with its in-memory solution.

This lab offers attendees an intro-level, hands-on session with Red Hat Data Grid, which uses the infinispan components under the hood. You will see these names interchangeably used and they both mean the same, since Inifinispan is an integral component in the Red Hat Data Grid. From the first line of code, to making services, to consuming them and finally to assembling everything together and deploying it on Openshift. It illustrates what Cache is, how to program with a distributed cache, and how to build applications and best practises on designing applications with Cache.

Deploying and maintaining microservices is hard. Much harder than maintaining a monolith. The are many moving pieces that can change / evolve / crash at anytime. To help us deploying our microservices, we are going to use Kubernetes. Kubernetes (commonly referred to as "K8s") is an open-source system for automating deployment, scaling and management of containerized applications that was originally designed by Google and donated to the Cloud Native Computing Foundation. It aims to provide a "platform for automating deployment, scaling, and operations of application containers across clusters of hosts". In this lab, we are using a specific distribution of K8S named OpenShift that provide a few set of features very useful to maintain our microservices. It’s NOT required to deploy Vert.x applications on top of Kubernetes. Bare metal is generally fine. We use Kubernetes in this lab because of the complexity involved when dealing with multiple microservices, their updates, downtimes and so on.

This is a BYOL (Bring Your Own Laptop) session. You will be provided with a CodeReady Workspace, an Openshift environment all pre-provisioned. If you prefer to run locally on your own laptop, you can do development on your own laptop, but you will need to use the provided Openshift environment as Operators installed for you are already setup there. So bring your Windows, OSX, or Linux laptop. You need JDK 8+ on your machine, and Apache Maven (3.5+).

What you are going to learn:

- What is Red Hat Data Grid 8.0

- What is a Cache, and how to start with the common usecases

- What is an Embedded Cache

- What is Clustering in a Cache scenario and how it works

- What is a Remote Cache and how to take benefit of Red Hat Data Grid

- You will build applications with known frameworks like Quarkus and Spring

- You will learn hot to use Red Hat Data Grid server REST API.

- You will learn how to externalize sessions

- And many more…​