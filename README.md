# Welcome to Football Balancer application!

## Stage: WORK-IN-PROGRESS

## Description:
The application is created to meet the basic functional requirements of everyone interested in managing football teams, as well as balancing them (via the sorting algorithm). The idea was born thanks to my many years of experience in taking part in amateur football games on all kinds of pitches (usually on the so-called Orlik football fields). The main problem I faced over the years was the lack of a centralized, dedicated platform for managing people in terms of creating two football teams, setting the date and location of the next match and balancing the squads so that the gaming experience was as pleasant as possible for each of us. Usually, the contact and information channels for those interested in the game were facebook, whats-app, etc., which often caused unnecessary confusion in the flow of information. It also carried the responsibility for appointing the teams that would competed against each other on the field on the organizers of the games, which was usually maximized the human factor in terms of balancing these teams. My application tries to meet the above problems so that each game is set in one transparent place and so that an algorithm that avoids simple human errors is responsible for creating competing teams.
<br>
<br>

## How-to-run:
The application is prepared on two levels: back-end written in java and front-end written in ReactJS. It is strongly recommended to use all the microservices included in the whole, so that the application works in accordance with its basic assumptions.
Below is a list of links to the required microservices:

- https://github.com/DudekJakub/Football-Balancer_1.0_GATEWAY-SERVICE : Spring Gateway Microservice responsible for single point entrance to the application (along with load-balancer in case of running more then one instance of given microservice).

- https://github.com/DudekJakub/Football-Balancer_1.0_AUTH-SERVICE : Authentication Microservice responsible for verification "who the user is" (creating user account & log-in into the system).

- https://github.com/DudekJakub/Football-Balancer_1.0_EUREKA-SERVICE : Eureka Discovery Microservice responsible for registration each microservice and making them visible for Gateway routing system.

- https://github.com/DudekJakub/Football-Balancer_1.0_NOTIFICATION-CHAT-SERVICE : Notification/Chat Microservice responsible for managing real-time notifications along with chat feature (which is still under the development)

When all of the above microservives are built and ready to run, first pleace run below command to pre-mature preparing required elements such as MySQL database, Mongo database and RabbitMQ queue system:

using docker-compose:
<br>
`docker-compose -f docker-compose.yaml up -d`

And to stop:
<br>
`docker-compose down`

Then run all microservices along with monlith and you are ready to go!

To test application I recommend using Swagger UI available (after fulfilling all above steps) under following link:
- http://localhost:8083/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config

To use front-end layer instead of Swagger UI/Postman please download front-end repository available under following link: 
- https://github.com/DudekJakub/Football-Balancer_1.0_FRONT-END

Front-end can be start via following command: 
<br>
`npm run dev`
<br>
<br>

## Main functional requirements:
| DESCRIPTION | STATUS |
| ---------------| :---------------:|
| Creating user account | ready |
| Logging into the application | ready |
| Browsing the list of rooms | ready |
| Searching for specific room/user via search bar | ready |
| Creating room | ready |
| Joining the room via password | ready |
| Sending request to become a member of the room | ready |
| Setting room's next match date | ready |
| Setting room's next match localization along with google-maps pin | ready |
| Accepting/Rejecting new member request as room's adminstrator | ready |
| Editing room's next match date as room's administrator | ready |
| Accepting/Rejecting new member request as room's adminstrator | ready |
| Editing room's description as room's administrator | ready |
| Editing room's localization as room's administrator | ready |
| Editing room's access visibility as room's administrator - private/public room | ready |
| Creating player along with linking him/her to specific room's member | ready |
| Creating skill for room (along with assigning it to all room's players) | work-in-progress |
| Creating formations in which two teams will face each other and based on which balancing algorithm will work on | work-in-progress |
| Balancing feature | work-in-progress |
| Joining to the queue to next match meeting | work-in-progress |
<br>

## Technology stack:


