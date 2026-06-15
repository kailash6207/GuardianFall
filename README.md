# GuardianFall 🛡️

GuardianFall is an Android application designed to improve the safety of elderly individuals living independently. The app continuously monitors device sensors to detect potential falls and automatically alerts family members or caregivers during emergencies.

## Features

* 🚨 Real-time fall detection using smartphone sensors
* ⏳ Emergency countdown before sending alerts
* 📍 Location tracking for emergency situations
* 👨‍👩‍👧‍👦 Emergency contact management
* 🔔 Instant notifications and alerts
* ☁️ Firebase integration for cloud-based data storage
* ⚙️ Customizable safety settings

## How It Works

1. The app monitors accelerometer and motion sensor data.
2. When a potential fall is detected, an emergency countdown begins.
3. The user can cancel the alert if they are safe.
4. If no response is received, the app retrieves the user's location.
5. Emergency contacts are notified with location details and alert information.

## Technology Stack

* Kotlin
* Android SDK
* Jetpack Compose
* Firebase Firestore
* Google Location Services
* Room Database

## Project Structure

```text
data/
firebase/
location/
sensor/
service/
ui/
utils/
```

## Future Enhancements

* Smartwatch integration
* Heart rate monitoring
* SMS emergency alerts
* Voice-activated emergency assistance
* AI-powered fall verification
* Family dashboard application

## Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/kailash6207/GuardianFall.git
   ```

2. Open the project in Android Studio.

3. Configure Firebase and add `google-services.json`.

4. Build and run the application on an Android device.

## Vision

GuardianFall aims to provide peace of mind to families by ensuring that elderly individuals receive timely assistance during emergencies, helping them live more safely and independently.
