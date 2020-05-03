# Handwashing reminder

An app that reminds you smartly when you have to wash your hands 💦👏

## Motivation

Due to the
[pandemic COVID-19](https://www.undp.org/content/undp/en/home/coronavirus.html)
I decided, motivated by Elena, to create an application that can help
people with a very simple technique for avoiding diseases: **washing our
hands**.

Some days ago, my grandfather José Fernando died due to this disease.
That was an extra motivation for finally completing this application and
help as many people as it can - I do not want any more dies because of
this virus.

Finally, I took advantage of this situation to learn a new programming
language, Kotlin, the one in which this app is built. I found this such
a great experience and I learned lots of things: coroutines, lambdas,
and those things that makes Kotlin such an awesome programming language.

I hope you enjoy this project and it's useful for you and people nearby.

## Features

Currently, the application supports:

+ Send notifications at specific time. This feature was developed using
  the Android's
  [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
  but because the notifications were not working as expected (they were almost
  always delayed) the app now uses AlarmManager for waking the device at
  specific time, even if it is in Doze mode.
+ Detect user activity and send a notification when ends an specific
  one. For example, if he gets out of a vehicle, or has just finished
  running, etc. This was developed using Google's [Activity Recognition
  API](https://developers.google.com/location-context/activity-recognition).

Obviously there are more features but the "important" ones (the ones
that reminds you to wash your hands) are those.

In addition, using the [WHO](https://who.int) as source, there are a
"diseases information" view in which some information about dangerous
diseases is displayed. This feature uses Firebase Remote Config, so the
list can always be updated and changed without updating the entire
application.

Another section is focused on **how to wash your hands**. This task,
that may seem simple, is crucial for having a good health and avoid some
practices that can compromise it. This section contains informative
videos and images for guiding you through the entire process.

Finally, there is a **news** section which is still under development.
In a future I expect having there recent news about any important
disease.

### Dynamic modules
By using
[dynamic delivery](https://developer.android.com/guide/app-bundle/dynamic-delivery)
this application downloads and installs modules on-demand. That is, it
downloads and installs new features keeping the app size as low as
possible.

## Play Store
The application is currently available for downloading only at Google
Play Store. This is because it's using dynamic features. In a while I
will try to implement classic APK version.

[Download from Play Store](https://play.google.com/store/apps/details?id=com.javinator9889.handwashingreminder)

<a href='https://play.google.com/store/apps/details?id=com.javinator9889.handwashingreminder&pcampaignid=pcampaignidMKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
<img alt='Get it on Google Play' src='https://play.google.com/intl/en_gb/badges/static/images/badges/en_badge_web_generic.png'/></a>

## Contributing
If you want to contribute:

+ Rate, giving me a star 🌟.
+ See my other projects in which I spent so much time working on them 💻.
+ Donate me and I will work even harder :D
  ([donate here](https://paypal.me/javinator9889)).
+ And watch if you would like to receive new updates 👁.
