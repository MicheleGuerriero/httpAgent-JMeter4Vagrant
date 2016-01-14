# httpAgent-JMeter4Vagrant

This project aim at running a simple load injection test using [Apache JMeter](http://jmeter.apache.org) and 
a test web service called [HTTPAgent](https://github.com/deib-polimi/modaclouds-tests/tree/master/http-agent-helper), which basically
when called returns and return an encrypted web page. The project basically automatized the test in order to create an environment
for running a simple performance test over a web service.

It provides a Vagrant file to easily set up two virtual machines, one running the client (JMeter) and one for the server [HTTPAgent].

Make sure you have Vagrant and VirtualBox installed (). After that you can run the following command to perform the machines set up.

    git clone https://github.com/MicheleGuerriero/httpAgent-JMeter4Vagrant.git
    cd httpAgent-JMeter4Vagrant
    vagrant up    


In the following we are going to explain how to run the default JMeter test.

# Running the test

The HTTPAgent is already up and running and you can contact it browsing to http://10.10.10.101:8080/http-agent-helper-0.1/getRandomPage.
From the project directory you can log in into the JMeter machine, configure the load to consider during the test and start the test. In order to login into the JMeter machine you can run the following command:

    vagrant ssh jmeter

Once you are logged in, configure the test.txt file in the home directory (/home/vagrant/) with the desired workload. The test.txt file takes is a space separated list of pairs, each pair representing the number of simultaneous users to simulate and the simulation period. An example of the test.txt content is the following:

    100 300
    200 300
    500 300

Once the test.txt file is configure you can run the jmetertemplatefiller-0.0.1-SNAPSHOT.jar java artifact you can find in the home directory in order to generare from the jmeterTestTemplate-HTTPAgent.jmx (always in the home directory) a real consumable .jmx file. This will be output in the test folder under the home directory. In oder to do so you cans simply run:

    java -jar jmetertemplatefiller-0.0.1-SNAPSHOT.jar

The last step is to run jmeter with the test generated at the previous step. In order to do so you can run the following:

cd ~/apache-jmeter-2.13/bin
./jmeter -n -t ~/test/jmeterTest-HTTPAgent.jmx


The graphes and tables output of the test are store in the /home/vagrant/test directory.


