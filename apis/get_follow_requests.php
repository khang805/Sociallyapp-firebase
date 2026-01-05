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
    fr.id,
    fr.sender_id,
    fr.status,
    fr.created_at,
    u.username,
    u.first_name,
    u.last_name,
    u.profile_photo_path
FROM follow_requests fr
INNER JOIN users u ON fr.sender_id = u.id
WHERE fr.receiver_id = ? AND fr.status = 'pending'
ORDER BY fr.created_at DESC";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$requests = [];
while ($row = $result->fetch_assoc()) {
    $profile_photo_url = null;
    if ($row['profile_photo_path']) {
        $profile_photo_url = $base_url . $row['profile_photo_path'];
    }
    
    $requests[] = [
        "id" => intval($row['id']),
        "sender_id" => intval($row['sender_id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "profile_photo_url" => $profile_photo_url,
        "created_at" => $row['created_at']
    ];
}

echo json_encode(["status" => "success", "requests" => $requests]);
$stmt->close();
$conn->close();
?>

