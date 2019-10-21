## Grabph
Grabph is the photo SNS App and  also has a lot of functions such as, the photo feed view, viewing of photo's EXIF data, supporting map-view and road-view to see the place taken the photo, view the information of person who took the photo and so on. Current version is 1.0 Alpha 25 and I'm adding several awesome new features to Grabph. As advanced technologies, LiveData, ViewModel, Paging Library, Room, Dagger as Dependency Injection and Clean Architecture were applied into Grabph. I'm making/improving Grabph to help someone who are trying to get new advanced technologies or making photo SNS App on Android. I'd like to give them a hand to make great service to be based on Grabph.
Please see below link:

## Summary
Grabph is based on Android architectural components and follows MVVM pattern. Also Grabph Advanced App Architecture consist of Presentation layer, Domain layer and Repository layer. And new latest technologies, Clean Architecture + Dagger 2.19 + MVI design pattern + LiveData + ViewModel + PaingLibrary + ROOM Tech + Kotlin Coroutine, were applied into Grabph App as Advanced Android App Architecture. And [RecyclreFragment](https://github.com/goforers/Grabph/blob/master/app/src/main/java/com/goforer/base/presentation/view/fragment/RecyclerFragment.kt) Ver 2.8 has been updated. The many advanced fuctions already were applied into [RecyclreFragment](https://github.com/goforers/Grabph/blob/master/app/src/main/java/com/goforer/base/presentation/view/fragment/RecyclerFragment.kt). These stuff make Android Apps to be extended being more competitive power and help them to maintain consistency.
And I'm also applying Kotlin Language into all modules in Grabph and almost codes are written with Kotlin.
I'd like to help someone who are trying to learn Kotlin Language to apply Kotlin Language into their project.

I was confident that I could develop high performance apps using Android architecture components and Kotlin Coroutine without using RxJava about seven month ago. And I proved it.

Grabph shows that it runs with mock data, not real data came from the backend server. App developers could implement code with mock and real data at the same time and test UI modules with mock data. It means that App developer could develop all module and test it even if the code of the Back-end server side does not implement.
Applying this development-technology provides an efficient way to develop and test the App faster and it allow to develop apps and REST APIs simultaneously.

Please refer to below:
[ViewModels and LiveData: Patterns + AntiPatterns](https://medium.com/androiddevelopers/viewmodels-and-livedata-patterns-antipatterns-21efaef74a54)

## Grabph Demo Video
[![Grabph Demo Video](http://img.youtube.com/vi/CeH-gKZtZtc/0.jpg)](https://www.youtube.com/watch?v=0c5nJWvnrLc&feature=youtu.be "Grabph Demo Video")


## Advanced App Architecture
<img src="https://github.com/goforers/Grabph/blob/master/Android%20App%20Architecture.png?raw=true" alt="Architecture" width="880"/>

Advanced App-Architecture consists of 3 layer, #Presentation Layer & #Domain(Business Logic) Layer & #Data Laery.

1. The presentation layer
The presentation layer is the user layer, the graphical interface that captures the user’s events and shows the results. It also performs operations such as verifying that there are no formatting errors in the user’s data entry and formatting data to be displayed in a certain way.
In this demo App, these operations are shared between the UI layer and the ViewModel layer:
# The UI layer contains the activities and fragments, capturing user events and displaying data.
# The ViewModel layer formats the data so that the UI shows them in a certain way and verifies that the user’s entries have the correct format.

2. The business logic layer
In this layer all the rules that a business must comply with are business. For this, they receive the data provided by the user and perform the necessary operations. In our example, the ordering of beers from lowest to highest alcoholic strength are the business rules for what the UseCase class will do.
It is the most stable layer and the one that indicates what is happening in the software architecture developed.

3. The data layer
In this layer is where the data is and where they can be accessed.
These operations are divided between the Repository layer and Datasource:
# The Repository layer is the one that performs the logic of data access. Your responsibility is to obtain them and check where they are, deciding where to look at each moment. For example, you can first check the database and, if they are not, search them on the web, save them in the local database and now return the saved data. That is, it defines the flow of access to the data. In our example, it asks beers directly to the data layer that communicates with the API.
# The Datasource layer is what the implementation performs in order to access the data. In this demo App, it is the one that implements the logic to be able to access the API data of beers.

<img src="https://github.com/goforers/Grabph/blob/master/Data%20Request%20%26%20Response%20Diagram.svg" alt="Architecture" width="880"/>
<img src="https://github.com/goforers/Grabph/blob/master/Event%20Bus.svg" alt="Architecture" width="880" />

## Loading data with ViewModel
<img src="https://github.com/goforers/Grabph/blob/master/Loading%20data%20with%20ViewModel.png" alt="Architecture" width="880"/>

## Pageing Library

<img src="https://github.com/goforers/Grabph/blob/master/paging-threading.gif" alt="Architecture" width="880" />
Loading Data
There are two primary ways to page data with the Paging Library:

Network or Database
First, you can page from a single source - either local storage or network. In this case, use a LiveData<PagedList> to feed loaded data into the UI, such as in the above sample.

To specify your source of data, pass a DataSource.Factory to LivePagedListBuilder.

<img src="https://github.com/goforers/Grabph/blob/master/paging-network-or-database.png" alt="Architecture" width="720" />
Figure 2. Single source of data provides DataSource.Factory to load content.
When observing a database, the database will ‘push’ a new PagedList when content changes occur. In network paging cases (when the backend doesn’t send updates), a signal such as swipe-to-refresh can ‘pull’ a new PagedList by invalidating the current DataSource. This refreshes all of the data asynchronously.

The memory + network Repository implementations in the PagingWithNetworkSample show how to implement a network DataSource.Factory using Retrofit while handling swipe-to-refresh, network errors, and retry.

Network and Database
In the second case, you may page from local storage, which itself pages additional data from the network. This is often done to minimize network loads and provide a better low-connectivity experience - the database is used as a cache of data stored in the backend.

In this case, use a LiveData<PagedList> to page content from the database, and pass a BoundaryCallback to the LivePagedListBuilder to observe out-of-data signals.

<img src="https://github.com/goforers/Grabph/blob/master/paging-network-plus-database.png" alt="Architecture" width="720" />
Figure 3. Database is cache of network data - UI loads data from Database, and sends signals when out of data to load from network to database.
Then connect these callbacks to network requests, which will store the data directly in the database. The UI is subscribed to database updates, so new content flows automatically to any observing UI.

The database + network Repository in the PagingWithNetworkSample shows how to implement a network BoundaryCallback using Retrofit, while handling swipe-to-refresh, network errors, and retry.

## Components Demonstrated
- [Clean Architecture](https://8thlight.com/blog/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Dagger 2.19](https://google.github.io/dagger/)
- [Room](https://developer.android.com/topic/libraries/architecture/room.html)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata.html)
- [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel.html)
- [Paging Library](https://developer.android.com/topic/libraries/architecture/paging.html)
- [MVVM Design pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Kotlin Coroutine](https://kotlinlang.org/docs/reference/coroutines/coroutines-guide.html)

### Prerequisites

- Android Studio 3.3 Beta 2 or later
- Android Device with USB Debugging Enabled

## Programming Language
- [Kotlin](https://kotlinlang.org/)

# License
```
Copyright 2019 Lukoh Nam, goForer

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
