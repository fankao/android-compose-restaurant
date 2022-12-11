### 1. Using Retrofit

> Retrofit also allows you to easily add custom headers and request types, file
> uploads, mocking responses, and more.

* An interface that defines the HTTP operations that need to be performed. Such an
  interface can specify request types such as GET, PUT, POST, DELETE, and so on.
* A **Retrofit.Builder** instance that creates a concrete implementation of the
  interface we defined previously. The Builder API allows us to define networking
  parameters such as the HTTP client type, the URL endpoint for the HTTP
  operations, the converter to deserialize the JSON responses, and so on.
* Model classes that allow Retrofit to know how to map the deserialized JSON objects
  to regular data classes.

> We mentioned that we can use our Firebase Realtime Database URL as a
> REST API. To access a specific node, such as the restaurants node of our
> database, we also appended the .json format to make sure that the Firebase
> database will behave like a REST API and return a JSON response

### 2. Further reading

https://square.github.io/retrofit/
