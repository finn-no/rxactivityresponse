# RxActivityResponse

## About
A library for using rx + onActivityResponse/onRequestPermissionsResult on android. For example for using rx and permissions/gplay settings/google login etc. It was written to deal with google play LocationSettings (giving response in onActivityResult), and Google Login (also giving response in OnActivityResult).

### The problem

Lets do an example without this library's helpers.

```

ExampleView:
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

```

###### The large problems with this approach:
1. permission/locationsettings requests respond in onActivityResult/onRequestPermissionsResult in the activity.
2. onSaveInstanceState might be triggered if the user rotates during the request, or if device is memory pressured during onActivityResult. This means the view we're subscribing in no longer exists.

### Our solution

First you need to add the library to your build.gradle file. 

Then you need to implement all activity delegate calls. This should be a small one time job, see https://github.com/finn-no/rxactivityresponse/blob/master/sample/src/main/java/no/finn/rxactivityresponse/sample/SerializedRxSampleActivity.java .

```

ExampleView implements RxStateRestart:
  static GET_LOCATION_ACTIVITY_REQUESTCODE = 42;
  RxState rxState;

  ExampleView() {
    // This has to be done construction time. This allows the library to restart the request if neccesary.
    rxState = RxState.get(GET_LOCATION_ACTIVITY_REQUESTCODE, this);
  }

  void getLocation() {
     // Starting a new call is exactly the same as restarting one during onActivityResponse/onRequestPermissionsResult
     rxAction(GET_LOCATION_ACTIVITY_REQUESTCODE)
  }
  
  @Override
  rxAction(requestCode) {
    if (requestCode == GET_LOCATION_ACTIVITY_REQUESTCODE) {
        // This restarts our Rx call if we've gotten a response in onRequestPermissionsResult/onActivityResult.
        RxPlayServices.getLocation(locationRequest, rxState)
            .subscribe(location -> useLocation(location));
    }
  }
  
  void useLocation(location) {
    ...
  }
  
```

That's it! This will work even if the app goes through onSaveInstanceState during the rx call.

For a complete example see https://github.com/finn-no/rxactivityresponse/blob/master/sample/src/main/java/no/finn/rxactivityresponse/sample/GpsLocationButton.java

## Gradle.properties changes

Add jcenter() to your build.gradle repositories block and rxactivity response to your dependencies: 

```

compile('no.finn.rxactivityresponse:library:0.4')
compile('no.finn.rxactivityresponse:playservices:0.4')

```

## License

    Copyright (C) 2015 FINN.no.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
