# HKSI Hong Kong School Information Android App
Native Android application for Hong Kong school information inquiry, integrated with the official open API of the Hong Kong Education Bureau. It delivers core capabilities including school search, multi-dimensional filtering, detail viewing, and map visualization, with full implementation of core Android development functionalities such as list rendering, network requests, JSON parsing, and UI interaction.

## Core Features
1. Real-time school name search + multi-dimensional filtering (school type, student gender, finance type)
2. School favorite function with list swipe shortcut operations
3. Google Maps integration with bidirectional linkage between the school list and map markers
4. Bilingual internationalization support (Traditional Chinese (Hong Kong) / English)
5. Built-in WebView for seamless loading of school official websites

## Requirements
- Minimum supported Android version: Android 10 (minSdk 29)
- Development tool: Android Studio Hedgehog or higher
- Runtime dependencies: Internet connection, Google Play Services (for map functionality)

## Quick Start
### Direct Installation
1. Navigate to the Releases page of this repository
2. Download the latest `app-debug.apk` installation package
3. Install it on an Android 10+ device to launch the app

### Build from Source
1. Clone the repository to your local machine and open it with Android Studio
2. Replace the Google Maps API Key in `AndroidManifest.xml` with your personal exclusive key
3. Sync Gradle dependencies, then compile and run the application

## Disclaimer
This project is for technical practice only and has no commercial purpose. All school data used in the application is from the official open API of the Hong Kong Education Bureau, and the copyright of the data belongs to the original publisher.
