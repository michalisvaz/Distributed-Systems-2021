# Project in Distributed Systems

In this project for the course "Distributed Systems" of AUEB's Informatics Department we created a distributed system where users could upload videos and watch other users' videos through a mobile (Android) application. Users are able to search videos by hashtag or by channel name of the uploader.

The project consisted of two phases:

* In the first phase we had to implement a framework which we would use in the second phase to send and receive videos. This was done by creating some brokers which communicated with the the clients and between themselves and which stored the users' videos. We also created the framework for the client nodes of our system to send and search videos. This was done with socket communication between clients and brokers.
* In the second phase we created an android application where the user could login and upload their videos or search other users' videos and watch them. To do so we used the framework created in the first part of the project.
