# RandIMI
RandIMI allows for participants of studies to be randomized.
It implements a number of randomization algorithms with different guarantees.

![RandIMI Logo](./src/main/webapp/WEB-INF/resources/images/randimi_dices.png)

## Documentation

More detailed documentation of RandIMI and its features can be found in the [GitHub Wiki](https://github.com/imi-ms/RandIMI/wiki).
A Swagger UI specifying the API endpoints can be found at [https://imi-ms.github.io/RandIMI/](https://imi-ms.github.io/RandIMI/)

## Randomization

RandIMI currently supports the different randomization algorithms; blocked randomization, minimization, and randomization by tossing a coin.

Coin toss is the simplest for of randomization.
A subject will simply be assigned to a random study arm.

Blocked randomization is more complicated, as it divides the randomization list into blocks to which the subjects will be assigned evenly, yet random.
This guarantees that if a subset of subjects is selected, those are evenly distributed across the studies' strata.

Minimization is a more sophisticated randomization method that calculates an imbalance for all possible study arm assignments.
By assigning the subject to the study arm that minimizes the imbalance, RandIMI ensures that the study arms are balanced in terms of subject characteristics, reducing potential biases.

## REDCap Integration

RandIMI can be integrated into REDCap projects using the [REDCap RandIMI Integration](https://imigitlab.uni-muenster.de/published/redcaprandimiintegration).

## Installation

### Using Docker

RandIMI can be installed using Docker.
A docker-compose file is provided in the project's root directory.

1. Create a directory and copy the `docker-compose.yml` into it.
2. Create a new `.env` file in the same directory and paste the content of the provided `.env.example` file into it.
The only variable that has to be set is the database password `PG_PASSWORD`.
3. Start the containers:
```shell
docker-compose up -d
```

### Manual Installation

Before deploying RandIMI to a server, you need to install Java 17, Tomcat 10, and a PostgreSQL 16 database.
Database name, user, and password can be chosen freely.
A script for the setup can be found at [src/main/resources/db/db_create.sql](src/main/resources/db/db_create.sql)
In order for RandIMI to be able to connect to the database, the created database has to be confiugured in a properties file placed in `/etc/randimi/db.properties`.
An example of the file structure can be found in [src/main/resources/db.properties](src/main/resources/db.properties).
RandIMI will create the required database schema automatically on startup.

Next, download the war file from the [Release page](https://github.com/imi-ms/RandIMI/releases) or build the project yourself as described below.
Rename the war file to `ROOT.war` and place it into `/val/lib/tomcat10/webapps/`.
Finally, start the Tomcat.

### First Setup
The credentials for the admin account are: Username: `admin`, Password: `changeme`.
Before RandIMI can be used productively, you have to make some changes to the settings:
1. Change the password of the admin account.
2. Configure the mail server.
3. Set the content of the Imprint, Data Privacy, and Support popups.

### Updating RandIMI

As long as it is not stated otherwise in the Release notes, you can update RandIMI by simply switching to the latest docker image or replacing the war file.
RandIMI uses flyway to automatically migrate the database without the need of manual intervention.

## Development

### Build

If you want to build RandIMI yourself, you first need to have a Java 17 JDK installed and cloned the repository.
Then built the project using `mvn package` from the project's root directory.
This will create a war file inside the `./target` directory.

To build the Docker images, the war has to be built first.
Then run `docker compose -f docker-compose-build.yml build`.

### Installation of the development environment

1. Install Java 17
2. Install Tomcat 10
    1. Download Tomcat 10 Core from here: https://tomcat.apache.org/download-10
    2. Unzip the file and move the folder to your preferred destination
3. Checkout RandIMI
4. Install PostgreSQL (tested versions are: 15, 16)
   - Option A) Install Docker and use the `docker-compose-dev.yml` by running the following command:
   ```shell
   docker compose -f docker-compose-dev.yml up -d
   ```
   - Option B) Install PostgreSQL directly on the machine and execute the script `src/main/resources/db/db_create.sql` as user postgres
5. Add run configuration to your IDE
   1. IntelliJ (paid version)
      1. Create a new configuration for a local Tomcat server
      2. Select the previous installed Tomcat 10 server as the `Application server`.
         You might have to add the server by clicking the `Configure...` button.
         `Tomcat Home` and `Tomcat base directory` should point to the extracted directory.
      3. (Optional) Add `-Dspring.profiles.active=dev` to your `VM options`.
         This enables hot reloading for frontend resources.
      4. Select your `JRE`
      5. Under the `Deployment` Tab add the artifact `RandIMI.war exploded` and set the `Application context` to your preferred value
   2. IntelliJ (free version)
      1. Install the plugin `Smart Tomcat`
      2. Select the previous installed Tomcat 10 server as the `Tomcat server`.
         You might have to add the server by clicking the `Configure...` button.
      3. (Optional) Add `-Dspring.profiles.active=dev` to your `VM options`.
         This enables hot reloading for frontend resources.
6. (Optional) Install Docker for Selenium tests


### Thymeleaf Templates
The user interface is being generated using thymeleaf, so the source code of all pages can be found in `src/main/webapp/WEB-INF/views/`.
Check out the thymeleaf documentation for more information.

### Database Schema
During development, you may make changes to the database schema.
To ensure upgradability, RandIMI uses flyway for database migrations.
Instead of applying your changes to the database scheme directly, you should create a migration script in `src/main/resources/db/migration/`.
Those scripts should be numbered and should not be modified once released.

## License

RandIMI is licensed under the [Apache-2.0 license](LICENSE).

## Third party libraries

RandIMI uses the following open-source libraries:

- [Spring Framework](https://github.com/spring-projects/spring-framework)
- [Jackson](https://github.com/FasterXML/jackson-dataformat-xml)
- [Jakarta](https://github.com/jakartaee)
- [Apache](https://github.com/apache)
- [Project Lombok](https://github.com/projectlombok/lombok)
- [Flyway](https://github.com/flyway/flyway)
- [Ehcache](https://github.com/ehcache/ehcache3)
- [Thymeleaf](https://github.com/thymeleaf/thymeleaf)
- [WebJars](https://github.com/webjars)
- [Bootstrap](https://github.com/twbs/bootstrap)
- [Font Awesome](https://github.com/FortAwesome/Font-Awesome)
- [JQuery](https://github.com/jquery/jquery)
- [DataTables](https://github.com/DataTables/DataTables)

## Credits
RandIMI was developed by the Medical Data Integration Center (MeDIC), Institute of Medical Informatics, University of Münster

Medical Data Integration Center (MeDIC)<br>
Institute of Medical Informatics<br>
University of Münster<br>
Albert-Schweitzer-Campus 1, Gebäude A11<br>
48149 Münster<br>
[medic@uni-muenster.de](mailto:medic@uni-muenster.de)<br>
0251 / 83 – 5 57 22
