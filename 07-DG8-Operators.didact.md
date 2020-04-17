Deployment: What’s an Operator and how does it help us?
An Operator is a method of packaging, deploying and managing a Kubernetes-native application. A Kubernetes-native application is an application that is both deployed on Kubernetes and managed using the Kubernetes APIs and kubectl tooling. An Operator is essentially a custom controller. A controller is a core concept in Kubernetes and is implemented as a software loop that runs continuously on the Kubernetes master nodes comparing, and if necessary, reconciling the expressed desired state and the current state of an object. Objects are well known resources like Pods, Services, ConfigMaps, or PersistentVolumes. Operators apply this model at the level of entire applications and are, in effect, application-specific controllers.

The Operator is a piece of software running in a Pod on the cluster, interacting with the Kubernetes API server. It introduces new object types through Custom Resource Definitions, an extension mechanism in Kubernetes. These custom objects are the primary interface for a user; consistent with the resource-based interaction model on the Kubernetes cluster.

An Operator watches for these custom resource types and is notified about their presence or modification. When the Operator receives this notification it will start running a loop to ensure that all the required connections for the application service represented by these objects are actually available and configured in the way the user expressed in the object’s specification.

The Operator Lifecycle Manager (OLM) is the backplane that facilitates management of operators on a Kubernetes cluster. Operators that provide popular applications as a service are going to be long-lived workloads with, potentially, lots of permissions on the cluster.

With OLM, administrators can control which Operators are available in what namespaces and who can interact with running Operators. The permissions of an Operator are accurately configured automatically to follow a least-privilege approach. OLM manages the overall lifecycle of Operators and their resources, by doing things like resolving dependencies on other Operators, triggering updates to both an Operator and the application it manages, or granting a team access to an Operator for their slice of the cluster.

Red Hat Data Grid 8.0 comes with an Operator. The administrators of the cluster have already installed the Data Grid Operator, what we need to do as a user is define a Custom Resource as to how and what configuration we want for our Red Hat Data Grid instances.

Installing
Assuming you have already logged in to openshift from the CodeReady terminal, if not you can do it now. Click on the Login to Openshift menu in the right menu called 'My Workspace'.

First, open a new browser with the OpenShift web console

openshift_login
Login using:

Username: user3

Password: openshift!

Once Logged in, goto your project user3-cache Click the link Installed Operator on the left, as shown in the picture below.

![Diagram](docs/images/dg_operatorinstalled)

Notice that the DataGrid operator is already installed in your namespace.

![Diagram](docs/images/dg_operatoroverview)

You can see there are no clusters installed in our namespace. Lets go ahead and do that.

You can press the Create Infinispan and create a CR (Custom Resource) from the console as well.

Or you can also follow the instructions to deploy a CR below.

Lets start by installing a basic Red Hat Data Grid Cluster.

    apiVersion: infinispan.org/v1
    kind: Infinispan 
    metadata:
    name: datagrid-service 
    spec:
    replicas: 2 
    expose:
        type: LoadBalancer 
Create a file with name cr_minimal.yaml copy and paste the above defination and save it.

Before applying this defination, lets take a look how its constructed.

tells Kubernetes/Openshift that the Custom resource type is Infinispan
we specify the name of our cluster as datagrid-service
we specify the replicas we want for our service.
And finally we want this instance to be accessible from outside openshift. e.g. the console;
Also notice that we are calling our service datagrid-service, we will use this name in the following labs to access our cluster.

Now from the terminal use the oc command line to apply it.

oc apply -f cr_minimal.yaml
You can watch the Red Hat Data Grid Operator creating the instances by running the following command.

oc get pods -w
Above command should render a similar output as below

[jboss@workspacel7b3gw19zpoclvcu dg8-operator]$ oc get pods
NAME                                   READY   STATUS      RESTARTS   AGE
datagrid-service-0                     0/1     Running     0          43s
infinispan-operator-5bb557597f-vl88n   1/1     Running     0          24h
oc get services
The above command should render a similar output as shown in the example below. Showing all the services

![Diagram](docs/images/dg_operatorserviceview)

You can see that there are three datagrid-services,

1 for use within the cluster,

1 for ping service which ensures that the clusters are healthy and operational

and lastly the external service, which we will use to goto the Admin console.

Run the following command to get the loadbalancer address

oc get services | grep datagrid-service-external | awk '{ print $4 }'
As you can see we have a service with our LoadBalancer. Lets get that url and paste it in the browser as follows

The following is an example, your loadbalancer url might differ

http://ad6cd35d6e6aa46fcb96558204c35f08-872149037.us-east-1.elb.amazonaws.com:11222
If you try to access the url; you would need to provide credentials.

The datagrid operator creates the credentials during installation time and they should be stored in your namespace secrets. Lets get the secret with the following command.

oc get secret datagrid-service-generated-secret -o jsonpath="{.data.identities\.yaml}" | base64 --decode
And now the final test to check we have a running cluster; login with the username developer and the password from the above secret.

![Diagram](docs/images/dg_adminconsole)

Recap
You created your own CR
Deployed the CR to Openshift using the DataGrid operator
You installed your DataGrid instance
*Congratulations!! you have completed the first Datagrid installation of this workshop. Lets move to the next lab and learn how we can use this instance as a RemoteCache with a Quarkus Application.