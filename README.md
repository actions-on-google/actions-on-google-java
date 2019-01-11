# Actions on Google Java/Kotlin Client Library
The Java/Kotlin library makes it easy to create Actions for the Google Assistant and
supports Dialogflow and Actions SDK fulfillment. It does so by handling most of the
request processing logic letting you focus on your Action’s business logic.

* [GitHub repo](https://github.com/actions-on-google/actions-on-google-java)
* [Reference docs](https://actions-on-google.github.io/actions-on-google-java/)
* [Actions on Google docs](https://developers.google.com/actions/)
* [Actions on Google samples](https://developers.google.com/actions/samples/)

## Requirements

* __IDE__: You can create a project using any IDE of your choice. However we recommend IntelliJ IDEA by JetBrains.
* __Java__: The Actions on Google Java fulfillment library requires __Java 8__ or higher.
You can download the JDK from [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* __Kotlin__: This library is written in Kotlin. Kotlin is bundled with IntelliJ IDEA version 15+.
* __Google Cloud SDK__ (if deploying to Google Cloud App Engine): 
You can initialize and deploy your project to App Engine using the Google Cloud SDK (provides the gcloud CLI).
You can download the Google Cloud SDK [here](https://cloud.google.com/sdk/docs/).

## Setup Instructions
The Actions on Google Java/Kotlin library is hosted on Maven central.
To use the library in your project, add the following to the dependencies section of your
project’s build.gradle.

```
repositories {
   mavenCentral()
}

dependencies {
   compile group: 'com.google.actions', name: 'actions-on-google', version: '1.0.0’
}
```


If using maven, add the following to your pom.xml file.

```xml
<dependency>
	<groupId>com.google.actions</groupId>
	<artifactId>actions-on-google</artifactId>
	<version>1.0.0</version>
	<type>pom</type>
</dependency>
```

### Boilerplate

We recommend that you start your project from the library [boilerplate](https://github.com/actions-on-google/boilerplate-java). The boilerplate is a self-contained project that sets up all the build dependencies using Gradle and allows you to import it easily into IntelliJ or other IDE of choice. It also includes artifacts that make it easy to deploy your project on Google Cloud Platform or AWS.

```
git clone https://github.com/actions-on-google/boilerplate-java.git
```

### Instructions for IntelliJ

* Start IntelliJ IDEA
* Click Import Project
* Select the build.gradle file located in the root directory of the project to import the gradle project.
* On the Import Project from Gradle dialog box, select Use gradle ‘wrapper’ task configuration, and click OK.

The project should now be setup.

## Use the Java/Kotlin library

The Java/Kotlin library makes it easy to implement a fulfillment webhook for your Action. You implement your Action’s logic in intent handlers as explained below.

### Intent handler 
An intent is a goal or action that the user wants to do, such as listening to a song or ordering coffee. Actions on Google represents the intent as a unique identifier. Your Action webhook provides handlers for intents it wants to handle dynamically. In the Java library, this is implemented as a Java class that extends either ```DialogflowApp``` or ```ActionsSdkApp```.

Intent handlers are public methods in this class that are marked with a special annotation - ```@ForIntent``` as shown below. Intent handlers accept an ```ActionRequest``` as a parameter and return an ```ActionResponse```. 

```java
public class MyActionsApp extends DialogflowApp {

 @ForIntent("Default Welcome Intent")
 public ActionResponse welcome(ActionRequest request) {
   // Intent handler implementation.
 }
}
```

### Assemble a response
Intent handlers return a relevant fulfillment response that is sent back to the Google Assistant, which ultimately conveys it to the user as voice and/or visual response. In the simplest form, a response is text spoken back to the user. Actions on Google also supports many other response formats, which include immersive cards with images, carousels, lists, media, and SSML.

Your Action may also respond with one of the helper intents supported by the Assistant. Examples include requesting confirmation from the user or getting permission from the user to get their location.

The ```ResponseBuilder``` class provides a variety of helper methods to assemble a response. In the simplest case, your Action responds back with text:

```java
@ForIntent("welcome")
 public ActionResponse welcome(ActionRequest request) {
   ResponseBuilder responseBuilder = getResponseBuilder(request);
   responseBuilder.add("Welcome to my app");
   Return responseBuilder.build();
 }
 ```

Here is a response that uses ```BasicCard``` to render a visual response:

```java
responseBuilder
           .add("This is the first simple response for a basic card.")
           .add(new BasicCard()
                   .setTitle("Title: This is a title")
                   .setSubtitle("This is a subtitle")
                   .setFormattedText(text)
                   .setImage(new Image()
                           .setUrl(IMG_URL)
                           .setAccessibilityText("Image alt text"))
                   .setButtons(buttons))
           .addSuggestions(SUGGESTIONS);
```

The library generates a JSON response from the ```ActionResponse``` returned from the intent handler (see below). The JSON is eventually handled by the Google Assistant to render an audio/visual response to the end user.

The following response uses a helper intent to request the Assistant to get the relevant information from the user:

```java
@ForIntent("askForPermissions")
 Public ActionResponse askForPermission(ActionRequest request) {
   ResponseBuilder responseBuilder = getResponseBuilder(request);
   responseBuilder
           .add("Placeholder for permissions text")
           .add(new Permission()
                   .setPermissions(new String[]{
                           ConstantsKt.PERMISSION_NAME,
                           ConstantsKt.PERMISSION_DEVICE_PRECISE_LOCATION
                   })
                   .setContext("To provide a better experience"));

   return responseBuilder.build());
 }
 ```

You can explore more helper methods in ```ResponseBuilder```.

As you can see from the above examples, the Java library provides an intuitive API to assemble responses from your Action. It provides an idiomatic abstraction over the JSON protocol to make it very easy to assemble all supported responses from your Action.

### Core API classes
The Java/Kotlin library includes the following core API classes:

* __```App```__: Top level interface to handle the JSON request and return a JSON response. It defines a single method —  handleRequest() —  to do this.
* __```DefaultApp```__: Subclass of App, implements the request processing logic.
* __```DialogflowApp```__ and __```ActionsSdkApp```__: These sub-classes of ```DefaultApp``` provide specific implementations to handle requests from Dialogflow or directly from Google Assistant (Actions SDK).
* __```ActionRequest```__: ```ActionRequest``` parses and encapsulates the JSON request body.
* __```ActionResponse```__: Encapsulates the webhook response.
* __```ResponseBuilder```__: Provides many helper methods to assemble a response. 

You can read more about the classes in the [library reference](https://actions-on-google.github.io/actions-on-google-java/).

## Test/Debug
This section describes how to test and debug your Java/Kotlin webhook during active development and implement end-end/integration tests.

### Unit tests
The boilerplate provides a starting point to unit test your intent handler. Depending on your use case, you can choose one of the following approaches:
* Create an ```ActionRequest``` by reading JSON from a file.
* Use ```MockRequestBuilder``` to build an ```ActionRequest``` instance to test your intent handler.

### Local debugging
During active development, you may start a local server to run your Action as a RESTful web service. You can send valid POST requests to this server (using tools such as Postman) to debug your intent handlers. Both Dialogflow and the Actions Simulator provide tools to visualize and copy JSON requests between Google Assistant and your webhook.

### Integration Tests
You can use the [Actions on Google testing library](https://github.com/actions-on-google/actions-on-google-testing-nodejs) to test your Action end to end. Note that the testing library requires tests to be written in Node.js. The webhook fulfillment can be in Node.js/Java/Kotlin or any other language.

## References and How to report bugs
* Actions on Google documentation: [https://developers.google.com/actions/](https://developers.google.com/actions/).
* If you find any issues, please open a bug on [GitHub](https://github.com/actions-on-google/actions-on-google-java).
* Questions are answered on [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google).
* [Reference docs](https://actions-on-google.github.io/actions-on-google-java/)
* [Boilerplate](https://github.com/actions-on-google/boilerplate-java)

Here are some samples to get you started.

* [Conversation components](https://github.com/actions-on-google/dialogflow-conversation-components-java)
* [Helper intents](https://github.com/actions-on-google/dialogflow-helper-intents-java)
* [Updates sample](https://github.com/actions-on-google/dialogflow-updates-java)
* [Transactions](https://github.com/actions-on-google/dialogflow-transactions-java)
* [Sign in](https://github.com/actions-on-google/dialogflow-google-sign-in-java)

## How to make contributions?
Please read and follow the steps in the CONTRIBUTING.md.

## License
See LICENSE.md.

## Terms
Your use of this library is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).