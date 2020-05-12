# Ionic Implementation

## Overview

Here you can find instructions on how to integrate and use FriendlyScore Connect for Flutter.

To get started quickly with FriendlyScore Connect for Ionic, clone the [GitHub repository](https://github.com/FriendlyScore/FriendlyScore-Connect-Ionic-Native-Example) and run the example. You need to [sign-up](https://friendlyscore.com/getting-started) for the free API keys through our Developer Console.

## Requirements
- [FriendlyScore API keys](https://friendlyscore.com/company/keys)
### Flutter
-1.19.0
### Dart
-2.9.0
### Android
- Install or update Android Studio version 3.2 or greater
- Android 5.0 and greater


## Quickstart Demo App

Clone and run the demo project from our [GitHub repository](https://github.com/FriendlyScore/FriendlyScore-Connect-Flutter-Native-Example).

## Flutter Android Implementation

We will override the `configureFlutterEngine` of the `MainActivity.kt` class within the `android` section of the App that will enable interaction between the UI component and Native Android code.

### Installation

Please follow the instructions below to install FriendlyScore Connect Android Native library, provide the necessary configuration and understand the flow.


#### Add the following values to your project-level build.gradle file

In your project-level Gradle file (you can find an example in the demo [build.gradle](https://github.com/FriendlyScore/FriendlyScore-Connect-Ionic-Native-Example/blob/master/android/build.gradle)), add rules to include the Android Gradle plugin. The version should be equal to or greater than `3.2.1`.

```groovy
buildscript {
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
    }
}
```

#### Add the following values to your project-level build.gradle file

In your project-level Gradle file (you can find an example in the demo, [build.gradle](https://github.com/FriendlyScore/FriendlyScore-Connect-Ionic-Native-Example/blob/master/android/build.gradle)), add the Jitpack Maven repository:

```groovy
allprojects {
  repositories {
    maven { url 'https://jitpack.io' } // Include to import FriendlyScore Connect dependencies
  }
}
```
#### Add configuration to your app

Go to the [Redirects](https://friendlyscore.com/company/redirects) section of the FriendlyScore developer console and provide your `App Package Id` and `App Redirect Scheme`.

You will also need your [Client Id](https://friendlyscore.com/company/keys) for the specific environment (SANDBOX, DEVELOPMENT, PRODUCTION).

In the project-level properties file (you can find an example in the demo [gradle.properties](https://github.com/FriendlyScore/FriendlyScore-Connect-Flutter-Native-Example/blob/master/android/gradle.properties)), please add the following configuration values:

```bash
# Client Id value is specified in the keys section of the developer console.
# Use the Client Id for the correct ENVIRONMENT.

CLIENT_ID=client_id

# App Redirect Scheme value is in the Redirects section of the developer console.
# You must specify the value the SDK will use for android:scheme to redirect back to your app. https://developer.android.com/training/app-links/deep-linking

APP_REDIRECT_SCHEME=app_redirect_scheme
```

#### Add the following values to your App Level build.gradle file(In the demo, [app/build.gradle](https://github.com/FriendlyScore/FriendlyScore-Connect-Ionic-Native-Example/blob/master/android/app/build.gradle))

Now we must read the configuration to create the string resources that will be used by the FriendlyScore Connect Android library. Also we will include the FriendlyScore Connect Library.

```groovy
android {
  compileOptions {
  sourceCompatibility 1.8
  targetCompatibility 1.8
  }

  defaultConfig {
    resValue "string", "fs_client_id", (project.findProperty("CLIENT_ID") ?: "NO_CLIENT_ID")
    resValue "string", "fs_app_redirect_scheme", (project.findProperty("APP_REDIRECT_SCHEME") ?: "NO_APP_REDIRECT_SCHEME_PROVIDED")
  }
}

dependencies {
   api 'com.github.friendlyscore.fs-android-sdk:friendlyscore-connect:latest.release'
}
```

You can select the environment you want to use:

| Environment  |   Description   |
| :----       | :--             |
| Environments.SANDBOX     | Use this environment to test your integration with Unlimited API Calls |
| Environments.DEVELOPMENT | Use this your environment to test your integration with live but limited Production API Calls |
| Environments.PRODUCTION  | Production API environment |

###  Android Implementation

The [FriendlyScoreConnectPlugin.java](https://github.com/FriendlyScore/FriendlyScore-Connect-Flutter-Native-Example/blob/master/android/app/src/main/java/com/demo/friendlyscore/connect/FriendlyScoreConnectPlugin.java) provides function that can be called from the Ionic components.

You must pass `userReference` that identifies the user to the Plugin.

```kotlin
 /**
     * In order to initialize FriendlyScore for your user you must have the `userReference` for that user.
     * The `userReference` uniquely identifies the user in your systems.
     * This `userReference` can then be used to access information from the FriendlyScore [api](https://friendlyscore.com/developers/api).
     */
    var userReference = "your_user_reference"

    /**
     * In order to listen when the user returns from the FriendlyScoreView in your `onActivityResult`, you must provide the `requestcode` that you will be using.
     */
    val REQUEST_CODE_FRIENDLY_SCORE = 11

```
Inside the `MainActivity` class create a `CHANNEL` for communication between the flutter ui components & native code

```kotlin
private val CHANNEL = "friendlyscore/connect"

Override the `configureFlutterEngine`. Create the `MedthodChannel.setMethodCallHandler` function which is called from the Flutter UI. In this call
from the UI the call method is provided along with the `userReference` parameter. We must store the result variable for future use to pass back events from the native component to the Flutter UI

```kotlin

 var finalResult: MethodChannel.Result?=null
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            finalResult = result
            userReference = call.argument<String>("user-reference")!!
            if(call.method == "startFriendlyScoreConnect"){
                startFriendlyScoreConnect(call, result)
            }

        }
    }
```

Define the function `startFriendlyScoreConnect` th

```kotlin
  fun startFriendlyScoreConnect(call:MethodCall,result: MethodChannel.Result){
        Handler(Looper.getMainLooper()).post {
            // Call the desired channel message here.
            FriendlyScoreView.Companion.startFriendlyScoreView(this, getString(R.string.fs_client_id), userReference, REQUEST_CODE_FRIENDLY_SCORE, Environments.PRODUCTION)

        }
    }
```
Handle the result from FriendlyScore Connect SDK, to pass back to the Flutter UI. We will use the `finalResult` saved earlier.
```kotlin

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_FRIENDLY_SCORE) {
            if (data != null) {

                //Present if there was error in creating an access token for the supplied userReference.
                if (data.hasExtra("userReferenceAuthError")) {
                    finalResult?.error("userReferenceAuthError", "", "")
                    //Do Something
                }

                //Present if there was service denied.
                if (data.hasExtra("serviceDenied")) {

                    if (data.hasExtra("serviceDeniedMessage")) {
                        val serviceDeniedMessage = data.getStringExtra("serviceDeniedMessage")
                        if (serviceDeniedMessage != null) {
                            finalResult?.error("serviceDenied", serviceDeniedMessage, "")

                        }
                    }
                }
                //Present if the configuration on the server is incomplete.
                if (data != null && data.hasExtra("incompleteConfiguration")) {
                    if (data.hasExtra("incompleteConfigurationMessage")) {
                        val errorDescription = data.getStringExtra("incompleteConfigurationMessage")
                        if (errorDescription != null) finalResult?.error("incompleteConfiguration", errorDescription, "")

                    }
                }
                //Present if there was error in obtaining configuration from server
                if (data.hasExtra("serverError")) {
                    finalResult?.error("serverError", "", "")
                }
                //Present if the user closed the flow
                if (data.hasExtra("userClosedView")) {
                    finalResult?.success("userClosedView")
                }
            }
        }
    }

}
```

## Flutter UI Component

### Add a Button

This includes the ui elements such as button that a user will click to trigger the FriendlyScore Connect Flow.
In your main.dart file (in the demo [app.component.html](https://github.com/FriendlyScore/FriendlyScore-Connect-Flutter-Native-Example/blob/master/lib/main.dart)) add a button that triggers the FriendlyScore Connect flow.
Create a `MethodChannel` to communicate with the native component. This must match the `Channel` created earlier in the `MainActivity`

```dart
    static const platform = const MethodChannel('friendlyscore/connect');
```

Define the function that is triggered when the user taps on the button. We need to pass the  `userReference` to the native component. It is expecting it to be available in the arguments.

Surround the call to the `startFriendlyScoreConnect` in `try` `catch` block.
```dart
    void _startFriendlyScore() async  {

        setState(() async {
          try{
            final result = await platform.invokeMethod('startFriendlyScoreConnect', {'user-reference':'your_user_reference'});
           //userClosedView
           print(result)
          }catch(e){
            //userReferenceAuthError
            //serviceDenied
            //incompleteConfiguration
            //serverError
            print(e);
          }
        });
      }
```


```dart
    @override
      Widget build(BuildContext context) {
        // This method is rerun every time setState is called,
        //
        // The Flutter framework has been optimized to make rerunning build methods
        // fast, so that you can just rebuild anything that needs updating rather
        // than having to individually change instances of widgets.
        return Scaffold(
          appBar: AppBar(
            // Here we take the value from the MyHomePage object that was created by
            // the App.build method, and use it to set our appbar title.
            title: Text(widget.title),
          ),
          body: Center(
            // Center is a layout widget. It takes a single child and positions it
            // in the middle of the parent.
            child: Column(
              // Column is also a layout widget. It takes a list of children and
              // arranges them vertically. By default, it sizes itself to fit its
              // children horizontally, and tries to be as tall as its parent.
              //
              // Invoke "debug painting" (press "p" in the console, choose the
              // "Toggle Debug Paint" action from the Flutter Inspector in Android
              // Studio, or the "Toggle Debug Paint" command in Visual Studio Code)
              // to see the wireframe for each widget.
              //
              // Column has various properties to control how it sizes itself and
              // how it positions its children. Here we use mainAxisAlignment to
              // center the children vertically; the main axis here is the vertical
              // axis because Columns are vertical (the cross axis would be
              // horizontal).
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                  RaisedButton(child: Text("Start FriendlyScore Connect"),
                  onPressed: _startFriendlyScore,
                  color: Colors.white,
                  textColor: Colors.blue,
                  padding: EdgeInsets.fromLTRB(10, 10, 10, 10),
                  splashColor: Colors.grey,
                  )

            ],
            ),
          ),
        );
      }

```

## Error Definition
| Error                     | Definitions  |
| -------------             | -------------|
| userReferenceAuthError   | Present if there was an authentication error for the supplied `userReference`.
| serviceDenied             | Present if service was denied. Please check the description for more information.
| incompleteConfiguration   | Present if the configuration on the server is incomplete. Please check the description for more information.
| serverError               | Present if there was a critical error on the server.

## Response State Definition
| State                    | Definitions  |
| -------------             | -------------|
| userClosedView            | Present if the user closed the FriendlyScore flow.

## Next Steps

### Access to Production Environment

You can continue to integrate FriendlyScore Connect in your app in our sandbox and development environments. Once you have completed testing, you can request access to the production environment in the developer console or speak directly to your account manager.

### Support

Find commonly asked questions and answers in our [F.A.Q](https://friendlyscore.com/developers/faq). You can also contact us via email at [developers@friendlyscore.com](mailto:developers@friendlyscore.com) or speak directly with us on LiveChat.

You can find all the code for FriendlyScore Connect for Web component, iOS and Android on our [GitHub](https://github.com/FriendlyScore).
