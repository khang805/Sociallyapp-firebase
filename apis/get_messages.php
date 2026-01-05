<?php
include 'db.php';
header('Content-Type: application/json');

if (!isset($_GET['user1_id']) || !isset($_GET['user2_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing required parameters"]);
    exit;
}

$user1_id = intval($_GET['user1_id']);
$user2_id = intval($_GET['user2_id']);
$limit = isset($_GET['limit']) ? intval($_GET['limit']) : 50;
$offset = isset($_GET['offset']) ? intval($_GET['offset']) : 0;

$base_url = "http://192.168.100.139/assignment-03/";

// Get messages between two users (bidirectional)
$query = "SELECT 
    m.id,
    m.sender_id,
    m.receiver_id,
    m.message_text,
    m.media_type,
    m.media_path,
    m.is_seen,
    m.is_vanish_mode,
    m.created_at,
    m.updated_at,
    u.username as sender_username,
    u.first_name as sender_first_name,
    u.last_name as sender_last_name,
    u.profile_photo_path as sender_profile_photo
FROM messages m
INNER JOIN users u ON m.sender_id = u.id
WHERE ((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?))
AND m.deleted_at IS NULL
ORDER BY m.created_at DESC
LIMIT ? OFFSET ?";

$stmt = $conn->prepare($query);
$stmt->bind_param("iiiiii", $user1_id, $user2_id, $user2_id, $user1_id, $limit, $offset);
$stmt->execute();
$result = $stmt->get_result();

$messages = [];
while ($row = $result->fetch_assoc()) {
    $media_url = null;
    if ($row['media_path']) {
        $media_url = $base_url . $row['media_path'];
    }

    $sender_profile_photo_url = null;
    if ($row['sender_profile_photo']) {
        $sender_profile_photo_url = $base_url . $row['sender_profile_photo'];
    }

    $messages[] = [
        "id" => intval($row['id']),
        "sender_id" => intval($row['sender_id']),
        "receiver_id" => intval($row['receiver_id']),
        "message_text" => $row['message_text'],
        "media_type" => $row['media_type'],
        "media_url" => $media_url,
        "is_seen" => (bool)$row['is_seen'],
        "is_vanish_mode" => (bool)$row['is_vanish_mode'],
        "created_at" => $row['created_at'],
        "updated_at" => $row['updated_at'],
        "sender_username" => $row['sender_username'],
        "sender_first_name" => $row['sender_first_name'],
        "sender_last_name" => $row['sender_last_name'],
        "sender_profile_photo_url" => $sender_profile_photo_url
    ];
}

// Reverse to show oldest first
$messages = array_reverse($messages);

echo json_encode([
    "status" => "success",
    "messages" => $messages,
    "count" => count($messages)
]);

$stmt->close();
$conn->close();
?>

