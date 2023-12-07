2.0.0
-----

**Breaking:** Batch Piano Analytics dispatcher now uses Kotlin and requires Android `minSdk` version 21.

* Bumped `targetSdk` version to 34.
* Added compatibility with Piano Analytics SDK 3.3.0 and newer.
* Piano Analytics SDK is no longer included in the plugin as transitive dependency. You have to explicitly add the dependency in your build.gradle.

1.0.1
-----

* Added verification to ensure the dispatcher is running on the right Piano SDK version (3.2.1 or older) while waiting for us to support the new version (3.3.0+).

1.0.0
-----

* Added Android's manifest configuration meta-data to enable/disable on-site ads event sending. See documentation for more info.

1.0.0-rc.1
-----

 * Initial dispatcher release candidate.