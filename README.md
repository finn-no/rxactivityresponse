[![Build Status](https://travis-ci.org/finn-no/rxactivityresponse.svg?branch=master)](https://travis-ci.org/finn-no/rxactivityresponse)
# RxActivityResponse

## About
A library for using rx + onActivityResponse/onRequestPermissionsResult on android. For example for using rx and permissions/gplay settings/Google login etc. It was written to deal with Google Play LocationSettings (giving response in onActivityResult), and Google Login (also giving response in OnActivityResult).

### The problem

Lets do an example without this library's helpers.

```java
public class ExampleView extends View {
    void getLocation() {
        Observable.create(getPermissionStatusObservable())
           .map(if (needsPermission) getPermissionObservable())
           .map(if (locationServicesDisabled) enableLocationServicesObservable())
           .map(getLocation())
           .subscribe(location -> useLocation(location))
       }

       void useLocation(location) {
           ...
       }
   }
}
```

###### The main problems with this approach:
1. Permission and locationsettings requests respond in `onActivityResult` / `onRequestPermissionsResult` in the activity.
2. `onSaveInstanceState` might be triggered if the user rotates during the request, or if device is memory pressured during `onActivityResult`. This means the view we're subscribing from no longer exists.

### Our solution
Install RxActivityResponse in your activity. This is a simple deal:
```java
@Override
protected void attachBaseContext(Context newBase) {
    super.attachBaseContext(RxActivityResponse.install(this, newBase));
}
```

Then in you request object do something like this:

```java
public class ExampleView extends View implements RxStateRestart {
    private static final int GET_LOCATION_ACTIVITY_REQUESTCODE = 42;
    private RxState rxState;

    public ExampleView() {
        // This has to be done construction time. This allows the library to restart the request if neccesary.
        rxState = RxState.get(GET_LOCATION_ACTIVITY_REQUESTCODE, this);
    }

    void getLocation() {
        // Starting a new call is exactly the same as restarting one during onActivityResponse/onRequestPermissionsResult
        rxAction(GET_LOCATION_ACTIVITY_REQUESTCODE);
    }

    @Override
    public void rxAction(int requestCode) {
        if (requestCode == GET_LOCATION_ACTIVITY_REQUESTCODE) {
            // This restarts our Rx call if we've gotten a response in onRequestPermissionsResult/onActivityResult.
            RxPlayServices.getLocation(locationRequest, rxState)
                .subscribe(location -> useLocation(location));
        }
    }

    void useLocation(location) {
        ...
    }
}
```

That's it! This will work even if the app goes through onSaveInstanceState during the rx call.

For a complete example see [`GpsLocationButton.java`](https://github.com/finn-no/rxactivityresponse/blob/master/sample/src/main/java/no/finn/rxactivityresponse/sample/GpsLocationButton.java)

## Gradle.properties changes

```groovy
repositories {
    jcenter()
}

dependencies {
    compile('no.finn.rxactivityresponse:library:0.5.0')
    compile('no.finn.rxactivityresponse:playservices:0.5.0')
}

```
## License

    Copyright (C) 2015-2017 FINN.no.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
