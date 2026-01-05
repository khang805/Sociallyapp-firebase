<?php
include 'db.php';
header('Content-Type: application/json');

// Base URL for image paths
$base_url = "http://192.168.100.139/assignment-03/";

// Optional: Get current user ID to mark own stories
$current_user_id = isset($_GET['current_user_id']) ? intval($_GET['current_user_id']) : 0;

// Get all active stories (not expired) grouped by user
// Handle NULL expires_at by checking if it's NULL or greater than NOW()
$query = "SELECT 
    s.id,
    s.user_id,
    s.media_path,
    s.media_type,
    s.created_at,
    s.expires_at,
    u.username,
    u.first_name,
    u.last_name,
    u.profile_photo_path
FROM stories s
INNER JOIN users u ON s.user_id = u.id
WHERE (s.expires_at IS NULL OR s.expires_at > NOW())
ORDER BY s.created_at DESC";

$result = $conn->query($query);

if (!$result) {
    echo json_encode(["status" => "error", "message" => "Database query failed: " . $conn->error]);
    $conn->close();
    exit;
}

$stories_by_user = [];
while ($row = $result->fetch_assoc()) {
    $user_id = intval($row['user_id']);
    
    if (!isset($stories_by_user[$user_id])) {
        $stories_by_user[$user_id] = [
            "user_id" => $user_id,
            "username" => $row['username'],
            "first_name" => $row['first_name'],
            "last_name" => $row['last_name'],
            "profile_photo_url" => $row['profile_photo_path'] ? $base_url . $row['profile_photo_path'] : null,
            "stories" => []
        ];
    }
    
    $media_url = $base_url . $row['media_path'];
    
    $stories_by_user[$user_id]["stories"][] = [
        "id" => intval($row['id']),
        "media_url" => $media_url,
        "media_type" => $row['media_type'],
        "created_at" => $row['created_at'],
        "expires_at" => $row['expires_at']
    ];
}

// Convert to array format and sort by latest story timestamp
$stories_list = array_values($stories_by_user);
usort($stories_list, function($a, $b) {
    $a_latest = $a['stories'][0]['created_at'] ?? '';
    $b_latest = $b['stories'][0]['created_at'] ?? '';
    return strcmp($b_latest, $a_latest); // Descending order
});

echo json_encode([
    "status" => "success",
    "stories" => $stories_list
]);

$conn->close();
?>

