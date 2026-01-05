<?php
include 'db.php';

header('Content-Type: application/json');

// Base URL for image paths
$base_url = "http://192.168.100.139/assignment-03/";

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);

// profile_photo_path is now in users table, not user_profiles
$query = "SELECT 
    u.id,
    u.username,
    u.first_name,
    u.last_name,
    u.profile_photo_path,
    f.created_at
FROM follows f
INNER JOIN users u ON f.following_id = u.id
WHERE f.follower_id = ?
ORDER BY f.created_at DESC";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$following = [];
while ($row = $result->fetch_assoc()) {
    $profile_photo_url = null;
    if ($row['profile_photo_path']) {
        $profile_photo_url = $base_url . $row['profile_photo_path'];
    }
    
    $following[] = [
        "id" => intval($row['id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "profile_photo_url" => $profile_photo_url
    ];
}

echo json_encode(["status" => "success", "following" => $following]);
$stmt->close();
$conn->close();
?>

