const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
admin.initializeApp();

// Cloud Function to send notifications
exports.sendNotification = functions.https.onRequest((req, res) => {
  // Extract data from the request
  const {to, title, body} = req.body;

  // Create the notification payload
  const payload = {
    notification: {
      title,
      body,
    },
    data: {
      title,
      body,
    },
    token: to, // Recipient's FCM token
  };

  // Send the notification
  admin.messaging().send(payload)
      .then((response) => {
        console.log("Notification sent successfully:", response);
        res.status(200).json({success: true});
      })
      .catch((error) => {
        console.error("Error sending notification:", error);
        res.status(500).json({success: false, error: error.message});
      });
});
