# RxAndroid 2 & Retrofit 2

This short guide explains how you setup and use Retrofit 2 with RxAndroid 2.

## Project setup

We need two dependencies for this project. Add the lines below to your build.gradle in your app project under **dependencies**.

For RxAndroid:
````gradle
compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
compile 'io.reactivex.rxjava2:rxjava:2.1.0'
````
and for Retrofit 2:
````gradle
compile 'com.squareup.retrofit2:retrofit:2.3.0'
````

And add the adapter for retrofit2 to work with RxJava 2.

````gradle
compile 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'
````

**OPTIONAL**

If you want to add support for GSON or another body parser, you can also add the following dependencies.

````gradle
compile 'com.google.code.gson:gson:2.7'
compile 'com.squareup.retrofit2:converter-gson:2.3.0'
````
