const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
admin.initializeApp();

// Cloud Function to send notifications
exports.sendNotification = functions.https.onRequest((req, res) => {
  // Enable CORS
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "GET, POST");
  res.set("Access-Control-Allow-Headers", "Content-Type");
  
  // Handle preflight requests
  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return;
  }
  
  console.log("Received notification request:", JSON.stringify(req.body));
  
  try {
    // Extract data from the request
    const {to, data} = req.body;
    
    if (!to) {
      console.error("Missing required parameter: to");
      return res.status(400).json({
        success: false,
        error: "Missing required parameter: to",
      });
    }
    
    if (!data) {
      console.error("Missing required parameter: data");
      return res.status(400).json({
        success: false,
        error: "Missing required parameter: data",
      });
    }
    
    console.log(`Sending notification to token: ${to.substring(0, 20)}...`);
    console.log(`Data payload: ${JSON.stringify(data)}`);
    
    // Simplify the payload structure to reduce potential issues
    const payload = {
      data: {
        ...data, // Spread all data fields
      },
    };
    
    // Add notification payload only if title and body are present
    if (data.title && data.body) {
      payload.notification = {
        title: data.title,
        body: data.body,
      };
    }
    
    // Log the final payload
    console.log("Final payload:", JSON.stringify(payload));
    
    // Send the notification
    admin.messaging().sendToDevice(to, payload)
      .then((response) => {
        console.log("Notification sent successfully:", JSON.stringify(response));
        
        // Check for errors in the response
        const successCount = response.successCount || 0;
        const failureCount = response.failureCount || 0;
        
        if (failureCount > 0 && response.results && response.results.length > 0) {
          console.error("Some messages failed to send:", 
            JSON.stringify(response.results));
        }
        
        res.status(200).json({
          success: true,
          messageId: response.results && response.results.length > 0 ? 
            response.results[0].messageId : null,
          successCount,
          failureCount,
        });
      })
      .catch((error) => {
        console.error("Error sending notification:", error);
        console.error("Error code:", error.code);
        console.error("Error message:", error.message);
        
        res.status(500).json({
          success: false,
          error: error.message,
          errorCode: error.code,
          errorDetails: error.toString(),
        });
      });
  } catch (error) {
    console.error("Exception in sendNotification:", error);
    console.error("Stack trace:", error.stack);
    
    res.status(500).json({
      success: false,
      error: error.message,
      stack: error.stack,
    });
  }
});
