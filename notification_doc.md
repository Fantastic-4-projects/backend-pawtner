# Notification Setup for Expo Mobile App

This document outlines the steps to integrate push notifications from the backend into your Expo mobile application.

## 1. Get the Expo Push Token

In your Expo app, you need to get the unique push token for the device. You can do this using the `expo-notifications` library.

```javascript
import * as Notifications from 'expo-notifications';

async function registerForPushNotificationsAsync() {
  let token;
  const { status: existingStatus } = await Notifications.getPermissionsAsync();
  let finalStatus = existingStatus;
  if (existingStatus !== 'granted') {
    const { status } = await Notifications.requestPermissionsAsync();
    finalStatus = status;
  }
  if (finalStatus !== 'granted') {
    alert('Failed to get push token for push notification!');
    return;
  }
  token = (await Notifications.getExpoPushTokenAsync()).data;
  console.log(token);
  return token;
}
```

## 2. Send the Token to the Backend

When the user logs in, send the retrieved Expo Push Token to the backend along with the user's credentials. The backend expects the token in the `fcmToken` field of the login request body.

**Endpoint:** `/auth/login`
**Method:** `POST`
**Body:**

```json
{
  "email": "user@example.com",
  "password": "yourpassword",
  "fcmToken": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]"
}
```

## 3. Handling Notifications

The backend will now send notifications to the registered devices when:

*   A new order is created.
*   The status of an existing order is updated.

You can listen for incoming notifications in your Expo app and handle them accordingly.

```javascript
import * as Notifications from 'expo-notifications';

// ...

useEffect(() => {
  const subscription = Notifications.addNotificationReceivedListener(notification => {
    console.log(notification);
    // Handle the notification, e.g., show an in-app banner
  });

  return () => subscription.remove();
}, []);
```

## 4. Handling Invalid Tokens

The backend will automatically detect and remove invalid or expired tokens. If a token is found to be invalid when sending a notification, it will be deleted from the database. This means you don't need to manually manage token expiration in your app.
