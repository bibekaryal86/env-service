# env-service

### Overview:

This is a Spring Boot-based utility application that interacts with a MongoDB database and provides an interface
for saving, retrieving, and deleting documents in MongoDB.
The documents that are saved represent runtime variables that can be consumed by other applications as and when needed.
A new MongoDb collection is created for each application, and the application name should be sent with each request.

### Features:

* Create: Persist data into a MongoDB collection. A new collection is created for each new application
* Read: Fetch stored documents from the corresponding MongoDB collection
* Update: Update functionality is not provided. If a document needs to be updated, it has to be recreated after deleting first.
* Delete: Delete document from a MongoDB collection

### Technologies:

* Java
* Spring Boot
* Gradle
* MongoDB
* JUnit
* OpenApi

### Setup and Installation:
* git clone https://github.com/bibekaryal86/env-service.git
* Navigate to the project directory
  * `cd env-service`
* Build the project
  * `./gradlew clean build`
* Run the application
  * The application requires the following environment variables to be provided at runtime:
    * AUTH_USR: Username of Basic Authentication implemented in the application
    * AUTH_PWD: Password of Basic Authentication implemented in the application
    * MONGO_APP: MongoDB Application name, required/found in MongoDB connection string
    * MONGO_DB: MongoDB Database name, required/found in MongoDB connection string
    * MONGO_USR: MongoDB Database user name, required/found in MongoDB connection string
    * MONGO_PWD: MongoDB Database user password, required/found in MongoDB connection string
    * SPRING_PROFILES_ACTIVE: development or production or any, this is optional but best to set
      * Profile `springboottest` is used when running JUnit tests
      * There are matching `springProfile` configuration in `logback.xml`
  * Run command:
    * java -jar -DAUTH_USR=some_username -DAUTH_PWD=some_password -DMONGO_APP=some_app -DMONGO_DB=some_database -DMONGO_USR=another_user -DMONGO_PWD=another_password SPRING_PROFILES_ACTIVE=production app/build/libs/env-service.jar

### API Endpoints:
* GET /tests/ping
  * Returns `{"ping": "successful"}`, no other functionalities
* POST /api/v1/{appName}
  * Create a document in collection for `appName`
  * If a collection doesn't exist for the `appName`, collection is created and then the document in the collection
  * Constraints:
    * There can not be two documents with same `name` attribute in a collection
    * A document must have `name` and one of either `stringValue`, `listValue` or `mapValue` field populated
* GET /api/v1/{appName}
  * Retrieve all documents in collection for `appName`
* PUT /api/v1/{appName}/{id}
  * Update is not allowed, this returns `Method Not Allowed` response
* DELETE /api/v1/{appName}/{envDetailsName}
  * Delete a document in collection for `appName` where `envDetailsName` matches `name` attribute in the document

### Deployment
This is currently deployed to Google Cloud Platform App Engine's Free Tier:
* https://envsvc.appspot.com/envsvc/tests/ping
* Deployment instructions:
  * Build the project: `./gradlew clean build`
  * Copy the generated jar file to the `gcp` library: `cp app/build/libs/env-service.jar gcp`
  * Configure `app-credentials.yaml`
    * copy `app-credentials_DUMMY.yaml` and update with actual values
  * Deploy to app engine: `gcloud app deploy app.yaml`
    * Best to start the process from `gcloud init` to select correct gcp project to deploy to
