# License3j Web API

This project provides a RESTful API built with Spring Boot that exposes the functionalities of the [License3j](https://github.com/verhas/License3j) library over the web. This allows users to perform various license management tasks using standard HTTP requests, making it accessible through web browsers or other HTTP clients.

## Functionalities

The API currently supports the following operations:

1.  **Create a fresh license in memory:**
2.  **Show the license loaded in memory:**
3.  **Upload an existing license to memory from file:**
4.  **Add features to new or existing licenses:**
5.  **Generate private and public keys for signing and verifying new and existing licenses:**
6.  **Sign and verify licenses:**
7.  **Download and upload generated keys:**
8.  **Digest public key into a java code ready byte array:**
9. **Redownload signed licenses**

## Use Cases

This API can be used in various scenarios, including:

* **Web-based License Management Portal:** A front-end application can be built on top of this API, providing a user-friendly web interface for software vendors to generate, manage, and distribute licenses.
* **Automated License Generation:** Software build processes or deployment pipelines can integrate with this API to automatically generate licenses as part of their workflow.
* **Centralized License Server:** This API can serve as a central point for managing licenses across different applications and deployments within an organization.

## Security Considerations

**Important:** Currently, this API **does not have any built-in authentication or authorization mechanisms.** 

## Getting Started 

1.  **Cloning the repository**

    Clone the repo locally using Github CLI
    ```
    gh repo clone Egg-03/License3j-WebAPI
    ```
    
    or via HTTPS

    ```
    git clone https://github.com/Egg-03/License3j-WebAPI.git
    ```
    
2.  **The project uses Maven as it's build tool.**

3.  **Building the project**

      Using Maven:

      ```
      mvn clean package
      ```
 
4.  **Running the API locally**

      ```
      java -jar license3j-api-x.x.x.jar
      ```

Once the application is running, you can interact with the API endpoints using tools like `curl`, Postman, or a web browser. Check the documentation for the endpoints and the requests that can be made.
NOTE: locally running the server requires PORT 8080 to be open and available. You can change the default behavior in `application.properties` available in `src/main/resources`

There is also a Dockerfile available which you can configure yourself to run it as a containerised application.

If you don't want to run it locally, a pre-built demo is also available live on Render with the following enpoint configured: https://license3j-api.onrender.com

The documentation will use this as its endpoint.

## Documentation
*Working on it*
## Contribution
*Working on it*

## License

Licensed under the Apache 2.0 license
