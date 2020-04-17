Conclusion
You made it ! Or you jumped to this section. Anyway, congratulations. We hope you enjoy this lab and learn some stuff. There is many other things about Red Hat Data Grid and Inifinispan that you can do and that was not illustrated here.
Don’t forget Red Hat Data Grid can be used in many usecases:
Red Hat Data Grid is a Cloud native caching system that you can embed in your application or use it as a remote server You can use it with the some of the well known frameworks and runtimes like Java, Node, C, C# etc.
And it intergrates very well with Kubernetes/Openshift e.g. Operators, Observability etc.
As soon as you jump into the microservice, functions or cloud native applications, you will need a better application environment. Kubernetes/OpenShift are perfect weapon to build, deploy, bind, and manage your microservices.
If you want, and we hope so, to go further here are some references:
Traditional zip deployments are available on the Customer Portal (https://access.redhat.com).
The container distribution and operator are available in the Red Hat Container Catalog (https://catalog.redhat.com/software/containers/explore) Product documentation is available here (https://docs.redhat.com)
Getting Started Guide that will get you running with RHDG 8 in 5 minutes.
Migration Guide (https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.0/html/data_grid_migration_guide/index)
Starter Tutorials (https://github.com/redhat-developer/redhat-datagrid-tutorials)
Supported Components (https://access.redhat.com/articles/4933371)
Supported Configurations (https://access.redhat.com/articles/4933551)


Java Development Kit
We need a JDK 8+ installed on our machine. Latest JDK can downloaded from:
Oracle JDK 8 (http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) OpenJDK (http://openjdk.java.net/install/)
You can use either Oracle JDK or OpenJDK.
Apache Maven
You need Apache Maven 3.5+. If you don’t have it already:
Download Apache Maven from https://maven.apache.org/download.cgi. Unzip to a directory of your choice and add it to the PATH .
IDE
We recommend you use an IDE. You can use Eclipse, IntelliJ, VS Code or Netbeans.
NoIDE?
If you don’t have an IDE, here are the steps to get started with Eclipse.
1. First download Eclipse from the download page (http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/oxygen1).
2. In the Download Links section, be sure to select the right version for your operating system. Once selected it brings you to a download page with a Download button. 3. Once downloaded, unzip it.
4. In the destination directory, you should find an Eclipse binary that you can execute.
5. Eclipse asks you to create a workspace.
6. Once launched, click on the Workbench arrow (top right corner).
Getting the code
References References
   
 git clone https://github.com/jbossdemocentral/sso-kubernetes-workshop.git
  