# RxActivityResponse

## About
A library for using rx + onActivityResponse/onRequestPermissionsResult on android. For example for using rx and permissions/gplay settings/google login etc. It was written to deal with google play LocationSettings (giving response in onActivityResult), and Google Login (also giving response in OnActivityResult).

## The problem
If you do use rx on android. And something during that the rx call might serialize your entire app. (For example leaving the app, and getting the response in onActivityResult). This breaks rx quite badly.

The library allows creating an object to deal with this problem. This object will be serialized, and can be in the same location as the subscription was done, to keep that part of the code in the same place. Rather than having to deal with every single onActivityResponse in your main activity.

## How does this work:
Lets image we have a View that requires permission to access location.

NB : This is a simple example. In this case you could do .subscribe() and only care about onRequestPermissionsResult. However, you might want to do rx.getLocation(). That first gets the permission, then enables gps through LocationSettings api, then gets the location. In this case the example makes more sense :)

```
ExampleView:
   void getLocation() {
     RxPermission.getPermission(getContext(), <Our ResponseHandler>, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        .subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean permission) {
                if (permission) {
                    // we have the permissions
                }
            }
        });
   }
```

<Our ResponseHandler> is a class you can serialize. (In the sample it's a parcelable, but you have controll over the serialization, we use jackson..). It will be serialized if the activity needs to. When it's deserialized it's onRequestPermissionsResult/onActivityResult will run, and it can handle whatever it needs to. Refinding the view if neccesary:

```
ResponseHandler:
    public void onRequestPermissionsResult(activity, permissions, grantResults) {
        if (permissionsGranted) {
            // The RxPermissions call will now have the permission, and continue on to give a response.
            activity.findViewById(ExampleViewId).getLocation();
        } else {
            // Optionally handle permission denied cases differently.
        }
    }
}
```

For a complete example see https://github.com/finn-no/rxactivityresponse/blob/master/sample/src/main/java/no/finntech/rxactivityresponse/sample/RxButtonExample.java

## Using it

Add jcenter() to your build.gradle repositories block and rxactivity response to your dependencies: 

```

compile('no.finntech.rxactivityresponse:rxactivityresponse:0.1')

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
