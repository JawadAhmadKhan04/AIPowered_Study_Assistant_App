<?php
// Prevent any output before JSON response
error_reporting(0);
ini_set('display_errors', 0);

header('Content-Type: application/json');

// Get POST data
$recipientUserId = $_POST['recipientUserId'];
$title = $_POST['title'];
$body = $_POST['body'];
$sender_id = $_POST['sender_id'] ?? null;
$type = $_POST['type'] ?? 'chat'; // Default to chat if not specified

// OneSignal credentials
$app_id = "5b2a135d-53c4-4047-bea1-1b038ef697f5";
$rest_api_key = "os_v2_app_lmvbgxktyraeppvbdmby55ux6x6zz4y3bvdehj4adz7karuwe4jrt3bdop7omseb5d4r6lsekz66knhricboaz7oypey52u4lxykoja";

// Prepare notification payload
$fields = array(
    'app_id' => $app_id,
    'include_external_user_ids' => array($recipientUserId),
    'headings' => array("en" => $title),
    'contents' => array("en" => $body)
);

// Add data
$data = array();

// Add sender_id if provided
if ($sender_id) {
    $data['sender_id'] = $sender_id;
}

// Add notification type
$data['type'] = $type;

// Add data to payload
$fields['data'] = $data;

$fields = json_encode($fields);

// Set up cURL
$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, "https://onesignal.com/api/v1/notifications");
curl_setopt($ch, CURLOPT_HTTPHEADER, array(
    'Content-Type: application/json; charset=utf-8',
    'Authorization: Basic ' . $rest_api_key
));
curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
curl_setopt($ch, CURLOPT_HEADER, FALSE);
curl_setopt($ch, CURLOPT_POST, TRUE);
curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, FALSE);

// Execute cURL
$response = curl_exec($ch);
$httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

// Output response
echo $response;

curl_close($ch);
?>
