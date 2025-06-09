# Stats
![Build](https://img.shields.io/github/actions/workflow/status/Egg-03/License3j-WebAPI/.github%2Fworkflows%2Fmaven_build.yml?style=for-the-badge)
![Tests](https://img.shields.io/github/actions/workflow/status/Egg-03/License3j-WebAPI/.github%2Fworkflows%2Fmaven_tests.yml?style=for-the-badge&label=Tests)
[![Endpoint Badge](https://img.shields.io/endpoint?url=https%3A%2F%2Flicense3j-api.onrender.com%2Fapi%2Fshieldio&style=for-the-badge)](https://yfdkkl4n.status.cron-job.org/)
![Codecov](https://img.shields.io/codecov/c/gh/Egg-03/License3j-WebAPI?token=170O0RU7FD&style=for-the-badge)
[![Documentation](https://img.shields.io/badge/Documentation-Apidog-pink?style=for-the-badge)](https://license3j.apidog.io/)



# About

This project provides a RESTful API built with Spring Boot that exposes the functionalities of the [License3j](https://github.com/verhas/License3j) library over the web.

# Functionalities

The API currently supports the following operations:

1.  **Create a fresh license in memory**
2.  **Show the license loaded in memory**
3.  **Upload an existing license to memory from file**
4.  **Add features to new or existing licenses**
5.  **Generate private and public keys for signing and verifying new and existing licenses**
6.  **Sign and verify licenses**
7.  **Download and upload generated keys**
8.  **Digest public key into a java code ready byte array**
9.  **Redownload signed licenses**

# Use Cases

This API can be used in various scenarios, including:

* **Web-based License Management Portal:** A front-end application can be built on top of this API, providing a user-friendly web interface for software vendors to generate, manage, and distribute licenses.
* **Automated License Generation:** Software build processes or deployment pipelines can integrate with this API to automatically generate licenses as part of their workflow.
* **Centralized License Server:** This API can serve as a central point for managing licenses across different applications and deployments within an organization.

# Security Considerations

**Important:** Currently, this API **does not have any built-in authentication or authorization mechanisms.** 

# Getting Started 

This project assumes that the reader is familiar with how License3j works and can create and apply licenses to their applications using either the [License3j REPL application](https://github.com/verhas/License3jRepl) or the [License3j GUI Application](https://github.com/Egg-03/License3j-GUI). If you are not familiar with how license3j works, the official repository [README](https://github.com/verhas/License3j) has a comprehensive guide on how to use the library. You can also head over to the [License3j REPL Application Readme](https://github.com/verhas/License3jRepl/blob/master/README.md) to acquire more details. If you are already familiar with these, you can skip to the numbered steps.

## Using the API Locally or Self-Hosting it

1.  **Cloning the repository**

    Clone the repo locally using Github CLI
    ```
    gh repo clone Egg-03/License3j-WebAPI
    ```
    
    or via HTTPS

    ```
    git clone https://github.com/Egg-03/License3j-WebAPI.git
    ```
    
2.  **Building the project.**

     This project uses Maven as it's build tool.
    
     If you don't have Maven installed on your local system or if you don't want to use Maven, there is a wrapper available in the repository itself. Just open your terminal in the 
     project location and type the following:

     ```
     ./mvnw clean package
     ```
     This will build the project binaries in the `target` folder of the repository.

     If you already have Maven installed, just run the following in the terminal opened in the project location
    
      ```
      mvn clean package
      ```
      This will build the project binaries in the `target` folder of the repository as well.
 
4.  **Running the API locally**

      Open your terminal in the `target` folder and type the following:

      ```
      java -jar license3j-api-x.x.x.jar
      ```

Once the application is running, you can interact with the API endpoints using tools like `curl`, Postman, Apidog, or a web browser (only for GET methods). Check the documentation for the endpoints and the requests that can be made.
NOTE: locally running the server requires PORT 8080 to be open and available. You can change the default behavior in `application.properties` available in `src/main/resources`

There is also a Dockerfile available which you can configure yourself to run it as a containerised application.

## Using a pre-configured endpoint

If you don't want to run it locally, a deployed instance is also available live on Render with the following enpoint configured: https://license3j-api.onrender.com

The documentation will use this as its endpoint.

# Documentation

You can view the documentation in [Apidog](https://license3j.apidog.io/)

# Contribution

*Working on it*

# License

Licensed under the Apache 2.0 license
