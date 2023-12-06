# Secure Messaging app

## Features
* Exchange of keys between users through QRCode
* Keys are stored exlusivly on the device in [Android Keystore](https://developer.android.com/privacy-and-security/keystore)
* Messages are encrypted on disk and on the server and can be read only after authorization through fingerprint
* No personal credentials needed for authorization only login and password
* Offline login to read messages 

## Stack
* [Jetpack Compose](https://www.jetpackcompose.net/) for UI
* [Firebase](https://firebase.google.com) - backend

## Backend 
Encrypted messages are stored in FireStore and send to the device through Firebase Messaging [source code](https://github.com/Koshelenkoa/secureMessagingFunctions)

## Screenshots
![chat screen](/screenshots/chat_screen.png?raw=true)
![create chat](/screenshots/create_chat_scren.png?raw=true)
![login screen](/screenshots/login_screen.png?raw=true)
![home screen](/screenshots/home_screen.png?raw=true)