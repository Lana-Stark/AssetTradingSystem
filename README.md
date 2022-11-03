# CAB302 Assignment
### Asset Trading System
____
Alex Williams, Bev Gorry, Jack French, Lana Stark
____

To initialise an admin user of username Admin, password 1234 run the following SQL query:

`INSERT INTO users (username, firstName, lastName, password, unitId, accountType) VALUES ("Admin","Admin","User","03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4", 1, "Admin")`

To initialise an Organisational Unit "ICT Department" with 50 credits, run the following command

`INSERT INTO organisationalunits (unitName, numCredits) VALUES ("ICT Department", 50)`

----

The network protocol for this application is written in a very similar way to the HTTP protocol, where a status code is returned for each request from the server.

Server response status codes are:

- 200 - Successful
- 400 - Processing or value error occurred
- 401 - Unauthorised
- 404 - Invalid Request Type

This data is transmitted back and forward via Object Streams sending HashMaps.