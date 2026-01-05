<?php

// search_users.php

include 'db.php'; // Include your database connection file

header('Content-Type: application/json');

if (!isset($_GET['query'])) {

    echo json_encode(["status" => "error", "message" => "Missing search query"]);

    exit;

}

// Base URL for image paths, matching the one used in ApiService.kt

$base_url = "http://192.168.100.139/assignment-03/";

$query = trim($_GET['query']);

// Cast to integer and default to 0 if not set or invalid

$current_user_id = isset($_GET['current_user_id']) ? intval($_GET['current_user_id']) : 0;

if (empty($query)) {

    // Return empty list if query is empty to prevent large unnecessary queries

    echo json_encode(["status" => "success", "users" => []]);

    exit;

}

$search_query = "%$query%";

// SQL query to search users by username, first name, or last name

// Only public fields are selected: id, username, first_name, last_name, and profile photo path.

$sql = "SELECT 

    u.id,

    u.username,

    u.first_name,

    u.last_name,

    up.profile_photo_path

FROM users u

LEFT JOIN user_profiles up ON u.id = up.user_id

WHERE u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ?

ORDER BY u.username ASC

LIMIT 50";

$stmt = $conn->prepare($sql);

$stmt->bind_param("sss", $search_query, $search_query, $search_query);

$stmt->execute();

$result = $stmt->get_result();

$users = [];

while ($row = $result->fetch_assoc()) {

    // Skip current user from search results

    if ($row['id'] == $current_user_id) {

        continue;

    }

    $profile_photo_url = null;

    if ($row['profile_photo_path']) {

        // Construct the full, accessible URL for the Android app

        $profile_photo_url = $base_url . $row['profile_photo_path'];

    }

    $users[] = [

        "id" => intval($row['id']),

        "username" => $row['username'],

        "first_name" => $row['first_name'],

        "last_name" => $row['last_name'],

        "profile_photo_url" => $profile_photo_url 

    ];

}

echo json_encode(["status" => "success", "users" => $users]);

$stmt->close();

$conn->close();

?>

