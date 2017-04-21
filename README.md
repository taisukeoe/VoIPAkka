# Android Akka VoIP DEMO app

This repository for VoIP system DEMO implemented in Android Akka.
This DEMO is very primitive version VoIP system, but works.

This repository consists of three parts:

- client/ ... VoIP client Android Application
- server/ ... VoIP server
- akka-jar/ ... Sample project to check how Akka proguard configuration works.

# How to use of Echo-back Server
1. Install Android SDK into your machine.
2. Download Android-25(equivalent with Android 7.1.1) platform via `android` command in SDK.
3. Set `ANDROID_HOME` environmental variable for Android SDK home directory.
4. Launch sbt
5. Run `client/android-run` with connecting with your Android Device via USB.
    Please be noted this Android app doesn't work in emulator properly, since emulator cannot use microphone.
6. Run `server/run YOUR_IP_ADDRESS YOUR_PORT` and choose `UDPEchoServer`.
7. Put YOUR_IP_ADDRESS and YOUR_PORT into EditText views after this Android Application launched, and press `CONNECT` button.
8. Press `OFF` Toggle Button and speak something. Your voice must be echoed back!

If you want to use VoIP server, please choose `UDPVoIPServer` in `6.` above, launch and connect two Android Devices into your local server.

# What this demo misses.
Disclaimer: This app is just for demo and samples for Android Scala & Akka.
Please be noted it's not production level VoIP project at all.

To make a production-level VoIP system, you'll have to consider the following at least.
* Audio quality improvement
* Performance tuning (Encoder/Decoder, Dispatcher, Mailbox, Memory usage, etc.)
* Resiliency


# LICENSE

Copyright 2017 Taisuke Oe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.