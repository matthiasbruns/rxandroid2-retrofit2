# RxAndroid 2 & Retrofit 2

This short guide explains how you setup and use Retrofit 2 with RxAndroid 2.
The example code I use can be found here: https://github.com/matthiasbruns/rxandroid2-retrofit2

## Project Setup
https://github.com/matthiasbruns/rxandroid2-retrofit2/commit/6682f70d70da9951e38bb61458faa630d4e742cf

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

## Demo Webservice

For this guide we will use an open websive called GeoNames. You can find its documentation here http://www.geonames.org/export/JSON-webservices.html

We will implement a simple example with the cities webservice.

````
Cities and Placenames

Webservice Type : REST
Url : api.geonames.org/citiesJSON?
Parameters :
north,south,east,west : coordinates of bounding box
callback : name of javascript function (optional parameter)
lang : language of placenames and wikipedia urls (default = en)
maxRows : maximal number of rows returned (default = 10)

Result : returns a list of cities and placenames in the bounding box, ordered by relevancy (capital/population). Placenames close together are filterered out and only the larger name is included in the resulting list.
````

An example call looks like this: http://api.geonames.org/citiesJSON?north=44.1&south=-9.9&east=-22.4&west=55.2&lang=de&username=demo

## Retrofit Service Implementation

The trimmed JSON output of the query above looks like this

````JSON
{
  "geonames": [
    {
      "lng": -99.12766456604,
      "geonameId": 3530597,
      "countrycode": "MX",
      "name": "Mexiko-Stadt",
      "fclName": "city, village,...",
      "toponymName": "Mexico City",
      "fcodeName": "capital of a political entity",
      "wikipedia": "en.wikipedia.org/wiki/Mexico_City",
      "lat": 19.428472427036,
      "fcl": "P",
      "population": 12294193,
      "fcode": "PPLC"
    },
    {
      "lng": 116.397228240967,
      "geonameId": 1816670,
      "countrycode": "CN",
      "name": "Peking",
      "fclName": "city, village,...",
      "toponymName": "Beijing",
      "fcodeName": "capital of a political entity",
      "wikipedia": "en.wikipedia.org/wiki/Beijing",
      "lat": 39.9074977414405,
      "fcl": "P",
      "population": 11716620,
      "fcode": "PPLC"
    }
  ]
}
````

To use GSON we need to define a network model class, which looks like this response of the api.

The "Response" object contains a list named "geonames".
````java
// com.matthiasbruns.rxretrofit.network.CityResponse

public class CityResponse {

    public List<Geoname> geonames;
}
````

The list contains of "Geoname" models.
````java
// com.matthiasbruns.rxretrofit.network.Geoname

public class Geoname {

    public double lat;
    public double lng;
    public long geonameId;
    public String countrycode;
    public String name;
    public String fclName;
    public String toponymName;
    public String fcodeName;
    public String wikipedia;
    public String fcl;
    public long population;
    public String fcode;
}
````

We have to add "username=demo" as a query parameter after every request.
There is a way to do this automatically - the OkHttp Interceptor.
https://github.com/matthiasbruns/rxandroid2-retrofit2/commit/9c42ed917d0d34b2e3f188e91d58d5083c2183d5

````JAVA
// com.matthiasbruns.rxretrofit.network.RetrofitHelper

/**
     * This custom client will append the "username=demo" query after every request.
     */
    private OkHttpClient createOkHttpClient() {
        final OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                final Request original = chain.request();
                final HttpUrl originalHttpUrl = original.url();

                final HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("username", "demo")
                        .build();

                // Request customization: add request headers
                final Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                final Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        return httpClient.build();
    }

    private Retrofit createRetrofit() {
        return new Retrofit.Builder()
                .baseUrl("http://api.geonames.org/")
                .client(createOkHttpClient()) // <- add this
                .build();
    }
````

 To enable GSON in retrofit, we also need to add ConverterFactory to Retrofit.
 ````JAVA
 // com.matthiasbruns.rxretrofit.network.RetrofitHelper

     private Retrofit createRetrofit() {
         return new Retrofit.Builder()
                 .baseUrl("http://api.geonames.org/")
                 .addConverterFactory(GsonConverterFactory.create()) // <- add this
                 .client(createOkHttpClient())
                 .build();
     }
 ````

The next step is the service itself. Retrofit does not need a real implementation of the service.
All you have to do is to provide an interface which can consume the real api endpoint.
For our use case the service may look like this:

````JAVA
// com.matthiasbruns.rxretrofit.network.CityService

@GET("citiesJSON")
Single<CityResponse> queryGeonames(@Query("north") double north, @Query("south") double south,
        @Query("east") double east, @Query("west") double west, @Query("lang") String lang);
````

As you can see, the method has all queries except the "username" parameter from the example query. Since the api listens to a GET request, we have to annotate this method with @GET("citiesJSON"). "citiesJSON" is the relative path to the root url of the api. All query parameters will be added to the whole request url.
The return type **Single<CityResponse>** is a RxJava typed CityResponse object. Single means, that if you subscribe to this method, it will only emit an item once or call onError. If you want to know more about RxJava 2 you should read this guide: https://github.com/balamaci/rxjava-walkthrough

The last step it the actual creation of the service.

````JAVA
// com.matthiasbruns.rxretrofit.networkRetrofitHelper

public CityService getCityService() {
    final Retrofit retrofit = createRetrofit();
    return retrofit.create(CityService.class);
}
````

Before we can finally work on Android code, we have to enable RxJava in Retrofit.
https://github.com/matthiasbruns/rxandroid2-retrofit2/commit/2408d9142d1d83be691485527316f187ec7cacdc

````JAVA
// com.matthiasbruns.rxretrofit.network.RetrofitHelper

private Retrofit createRetrofit() {
    return new Retrofit.Builder()
            .baseUrl("http://api.geonames.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // <- add this
            .client(createOkHttpClient())
            .build();
}
````

## Using The Service
https://github.com/matthiasbruns/rxandroid2-retrofit2/commit/be9b654cbb2cb006dc114d1fb7bf9177345785ff

To use the service, you have to add the INTERNET permission first to the AndroidManifest

````xml
<uses-permission android:name="android.permission.INTERNET" />
````

In the MainActivity, we will add/replace the following code.

````JAVA
// com.matthiasbruns.rxretrofit.MainActivity

    /**
     * We will query geonames with this service
     */
    @NonNull
    private CityService mCityService;

    /**
     * Collects all subscriptions to unsubscribe later
     */
    @NonNull
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private TextView mOutputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOutputTextView = (TextView) findViewById(R.id.output);

        // Initialize the city endpoint
        mCityService = new RetrofitHelper().getCityService();

        // Trigger our request and display afterwards
        requestGeonames();
    }

    @Override
    protected void onDestroy() {
        // DO NOT CALL .dispose()
        mCompositeDisposable.clear();
        super.onDestroy();
    }

    private void displayGeonames(@NonNull final List<Geoname> geonames) {
        // Cheap way to display a list of Strings - I was too lazy to implement a RecyclerView
        final StringBuilder output = new StringBuilder();
        for (final Geoname geoname : geonames) {
            output.append(geoname.name).append("\n");
        }

        mOutputTextView.setText(output.toString());
    }

    private void requestGeonames() {
        mCompositeDisposable.add(mCityService.queryGeonames(44.1, -9.9, -22.4, 55.2, "de")
                .subscribeOn(Schedulers.io()) // "work" on io thread
                .observeOn(AndroidSchedulers.mainThread()) // "listen" on UIThread
                .map(new Function<CityResponse, List<Geoname>>() {
                    @Override
                    public List<Geoname> apply(
                            @io.reactivex.annotations.NonNull final CityResponse cityResponse)
                            throws Exception {
                        // we want to have the geonames and not the wrapper object
                        return cityResponse.geonames;
                    }
                })
                .subscribe(new Consumer<List<Geoname>>() {
                    @Override
                    public void accept(
                            @io.reactivex.annotations.NonNull final List<Geoname> geonames)
                            throws Exception {
                        displayGeonames(geonames);
                    }
                })
        );
    }
````

The method **DisplayGeonames** calls the service endpoint we created before. The result is being transformed into the geoname list.
In the subscribe call, we send the geonames to the display logic, which simply loops through the list and displays the names of the Geoname object in a TextView.

## Conclusion

I've shown you how you can easily combine the power of RxJava with Retrofit. We used a randomly picked JSON API (it was my first Google result) and
created a Retrofit endpoint for the names API. Retrofit has opt-in support for RxJava2, which we used as the return type of our endpoint.

In the activity we subscribe to this created endpoint and display the received information in a simple way.
I hope I could help you with this little guide to get you Retrofit2-RxJava2 setup working.
