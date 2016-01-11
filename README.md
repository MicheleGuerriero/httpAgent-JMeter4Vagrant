# httpAgent-JMeter4Vagrant

This project aim at running a simple load injection test using [Apache JMeter](http://jmeter.apache.org) and 
a test web service called [HTTPAgent](https://github.com/deib-polimi/modaclouds-tests/tree/master/http-agent-helper), which basically
when called returns and return an encrypted web page. The project basically automatized the test in order to create an environment
for running a simple performance test over a web service.

It provides a Vagrant file to easily set up two virtual machines, one running the client (JMeter) and one for the server [HTTPAgent].

You can run the following command to perform the machines set up.

    git clone https://github.com/MicheleGuerriero/httpAgent-JMeter4Vagrant.git
cd httpAgent-JMeter4Vagrant
vagrant up    


In the following we are going to explain how to run the default JMeter test.

# Running the test

