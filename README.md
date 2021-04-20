# A Microservices-based System for Crowd Computing

**Overview:**

This project provides a distributed computing framework that enables the crowd computing paradigm on the smart devices (e.g., mobile phones, computers) by utilizing the volunteered idle CPU cycles of smart devices owned by the crowd. Crowd computing is a way of solving computationally intensive task or problem in a distributed manner. The large computationally intensive task is divided into multiple subtasks that are distributed over multiple computing devices (mobile phones, computers) for processing.
This framework has three main components. 1) The dashboard, 2) The middleware broker, and 3) The workers. All three components would communicate with each other through a 4th component called MQTT broker.
--------------------------------------------------------------------------------------------------------------------------
1. **Dashboard:** It is a React, NodeJS based web application that allows the user to define a task with a set of properties and a file. This also displays the computed task results to the user.

	 **Modules:** 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**MQTT connection:** This module establishes a connection with the MQTT broker through an MQTT client library to publish and receive the messages (tasks or results) from the middleware broker.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Define Task:** This allows the user to define a task through a set of properties such as a short name for the task, a brief description about the task, due date to run the task on the worker device, the actual size of the task, the author who initiated the task, and the reward to the worker for executing the task.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Task Result:** This module displays the computed result of the task that sent by the middleware broker.

	 **Used Technologies:** React, NodeJS, Express, Mosca MQTT client, Bootstrap, Material-UI, and Socket.io for the communication between React and Node server.
  
	 **Installation: **

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Download and install Node.js version v14.16.0 or higher and NPM through a Node version manager or the installer from https://nodejs.org/en/download/. For installation, you can refer https://docs.npmjs.com/downloading-and-installing-node-js-and-npm.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Through the terminal, download and install React using the command `npm install react`.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- Clone both repositories (`/module1-dashboard/mcc-dashboard-nodejs/`, `/module1-dashboard/mcc-dashboard-react/`) into your local machine.

	 **Run:** Note: You need to run the NodeJS app before running the React app.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- For the NodeJS app, open a terminal, navigate to the project folder and run the command `npm install` to install all project dependencies into the node_modules folder. Then start the application by running the command “npm start”. Alternatively, you can import the project into IDE (e.g., Visual studio code, WebStorm) and run the application.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;;&nbsp;&nbsp;- For the React app, open a new terminal, navigate to the project folder and run the command `npm install` to install all project dependencies into the node_modules folder. Then start the application by running the command “npm start”. Alternatively, you can import the project into IDE (e.g., Visual studio code, WebStorm) and run the application.
--------------------------------------------------------------------------------------------------------------------------

2)	**The Middleware Broker:** A Spring Boot microservices server application that acts as a middleman between user dashboard application and worker application. It has 4 main modules or microservices. All four microservices register with Eureka service discovery and uses RabbitMQ messaging queue for their inter-service communication.

                                **Modules:** 

                                             i.   **MQ Microservice:** This module establishes a connection with the MQTT broker through an MQTT client library to publish and receive the messages. It acts as a gateway between the MQTT broker and the other microservices.
                                                                      It is responsible for registering subscribed workers, receiving the task details from the user dashboard app, sending task description to the workers, receiving the acceptance response by the workers who agreed to run the task on their device, sending subtasks to workers, collecting subtask results that send by the workers, and sending accumulated single result back to the user dashboard application.

                                             ii.  **DB Microservice:** It Provides data persistence by storing data in the database. It is responsible for applying CURD operations on received tasks, worker responses, subtasks, subtask results, and accumulated results.

                                             iii. **Task Engine Microservice:** It takes a task and subscribed worker details from the DB service to select an appropriate worker for that task. This service divides the task into multiple subtasks based on the number of workers who agreed to run the task on their device and selects the appropriate worker per sub-task based on the worker device OS.

                                             iv.  **Result Engine Microservice:** This module accumulates the subtask results that brought in by each worker into a single result which will be send to display on the user's web dashboard.

                                **Used Technologies:** Java 8, Java Spring Boot (v.2.3.5) Microservices, Netflix Eureka Server, Async Eclipse Paho Java Client (v1.1.0), Apache Cassandra Database, Spring Data Cassandra, RESTAPI, Spring Cloud Stream, RabbitMQ, Maven Build Tool. 

                                **Installation:**
                                                  - Download and install JDK 8 from https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html. For installation, you can refer https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html.

                                                  - Clone repository /module2-middleware-broker/ into your local machine.

                                                  - Download and install Apache Cassandra from https://cassandra.apache.org/doc/latest/getting_started/installing.html

                                **Run:** To run the Spring Boot Microservices application follow below steps

                                          - Navigate to the bin folder located within the Apache Cassandra folder, open a terminal and run the command `Cassandra` to start the Cassandra database instance.

                                          - Open a new terminal and execute the command `cqlsh` to start the CQL shell. In the shell, run the CQL commands or schemas from the file (/module2-middleware-broker/Cassandra-sql-queries.txt) to create keyspaces and the tables in the database.

                                          - In path /module2-middleware-broker/microservices-app/ navigate to each microservice application folder (e.g., /microservices-app/eurekaserver) and open a separate terminal (or) git bash for each microservice and run the command `./mvnw spring-boot: run`. The `eurekaserver` needs to be run before running any other microservices. Alternatively, you can import the project into IDE (e.g., IntelliJ, Eclipse) and run the application.


3)	**The Workers:** The workers could be android devices or desktop devices. It has two sub components. 1) The mobile client, and 2) The desktop client.

                      **The Mobile Client:** It is an android application that runs on any android-based mobile device to receive tasks in the form of .apk from the middleware broker, accepting or rejecting the tasks, running .apk subtasks in the background, and broadcasting the computed result back to the main application. The main application sends the computed result back to the middleware broker through MQTT.

                      **The Desktop Client:** It is a JavaFX application that runs on any desktop machine to receive tasks in the form of .jar from the middleware broker, accepting or rejecting the tasks, running .jar subtasks in the background and, sends the computed result back to the middleware broker through MQTT.

                      **Used Technologies:** Java 8, Android, Async Eclipse Paho Android Client, Broadcast Receiver, Java 11 for JavaFX, Async Eclipse Paho Java Client, and Gradle Build tool.

                      **Installation:**
                                        - Download and install JDK 8 and JDK11 from https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html. For installation, you can refer https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html.

                                        - Clone both repositories (`/module3-workers/MqttClient/, /module3-workers/cc_desktop_client/`) into your local machine.

                      **Run: **
                                - For the mobile client, import the project into android studio IDE and select `app` from the run/debug configurations drop-down menu in the toolbar. Enable developer options in your android mobile device and connect it to your computer. In the toolbar, select your connected mobile device to run the app from the target device drop-down menu. Alternatively, you can run the application on the emulator as well.

                                - Import the project into an IDE (IntelliJ, Eclipse). To run the JavaFx application for the first time from the IDE follow the following steps:
                                  Go to Gradle -> cc_desktop_client -> Tasks -> application -> run.

**MQTT Broker:** Download and install Mosquitto broker on your machine from https://mosquitto.org/download/


**Steps to run complete system** (For running each component, you can refer to the `Run` section under each component)
	
                                 - Open a terminal and start the Mosquitto broker using the command `net start mosquitto`.

                                 - Open a new terminal and start all the microservices instances of the middleware broker using command `./mvnw spring-boot: run`.

                                 - Now, go to your browser and type the Eureka server URL `localhost:8761`, you should be able to see all four running microservices instances in eureka server page.

                                 - To start the dashboard app, open one terminal for each React and nodeJS application then run the command `npm start` for both.

                                 - To open the dashboard, go to your browser and type the URL `localhost:3000`, you should be able to see the login page of the dashboard.

                                 - Install and run the Android or the desktop application on your device as mentioned under the workers component (see the `run` section).

**To initiate a task:**
                       - Open the worker application (Android or Desktop) on your device, go to subscribe tab, and click on subscribe button to receive the tasks.

                       - Login into the dashboard with credentials User Name: guest and Password: guest.

                       - Click on “define a task” tab and enter task short name, description, due date, size, author, rewards, and upload a file (the actual computational task) from /TestFiles/ either .apk or .jar or .txt.

                       - Under “Received Tasks” tab you will be receiving details about the task. For the received task, click accept button if you are willing to run the task on your device.

                       - After the task due date expiry, you will be receiving the executable file under the “Received Tasks”. Click on the executable file to run the task on your device.

                       - Now, at the dashboard side, under the tab “Result” you can see the computed or accumulated results sent by the middleware broker.
					   
					   
