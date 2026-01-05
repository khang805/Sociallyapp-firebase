<?php
include 'db.php';
header('Content-Type: application/json');

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);
$base_url = "http://192.168.100.139/assignment-03/";

// Get all unique conversations for a user
// This query gets the latest message from each conversation
$query = "SELECT 
    m.id as message_id,
    m.sender_id,
    m.receiver_id,
    m.message_text,
    m.media_type,
    m.media_path,
    m.is_seen,
    m.is_vanish_mode,
    m.created_at,
    CASE 
        WHEN m.sender_id = ? THEN m.receiver_id
        ELSE m.sender_id
    END as other_user_id,
    CASE 
        WHEN m.sender_id = ? THEN u_receiver.username
        ELSE u_sender.username
    END as other_username,
    CASE 
        WHEN m.sender_id = ? THEN u_receiver.first_name
        ELSE u_sender.first_name
    END as other_first_name,
    CASE 
        WHEN m.sender_id = ? THEN u_receiver.last_name
        ELSE u_sender.last_name
    END as other_last_name,
    CASE 
        WHEN m.sender_id = ? THEN u_receiver.profile_photo_path
        ELSE u_sender.profile_photo_path
    END as other_profile_photo_path,
    (SELECT COUNT(*) FROM messages m2 
     WHERE ((m2.sender_id = ? AND m2.receiver_id = CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END)
            OR (m2.sender_id = CASE WHEN m.sender_id = ? THEN m.receiver_id ELSE m.sender_id END AND m2.receiver_id = ?))
     AND m2.receiver_id = ? AND m2.is_seen = FALSE AND m2.deleted_at IS NULL) as unread_count
FROM messages m
LEFT JOIN users u_sender ON m.sender_id = u_sender.id
LEFT JOIN users u_receiver ON m.receiver_id = u_receiver.id
WHERE (m.sender_id = ? OR m.receiver_id = ?)
AND m.deleted_at IS NULL
GROUP BY CASE 
    WHEN m.sender_id = ? THEN m.receiver_id
    ELSE m.sender_id
END
ORDER BY m.created_at DESC";

$stmt = $conn->prepare($query);
$stmt->bind_param("iiiiiiiiiiiiii", 
    $user_id, $user_id, $user_id, $user_id, $user_id, 
    $user_id, $user_id, $user_id, $user_id, $user_id,
    $user_id, $user_id, $user_id
);
$stmt->execute();
$result = $stmt->get_result();

$conversations = [];
while ($row = $result->fetch_assoc()) {
    $media_url = null;
    if ($row['media_path']) {
        $media_url = $base_url . $row['media_path'];
    }

    $other_profile_photo_url = null;
    if ($row['other_profile_photo_path']) {
        $other_profile_photo_url = $base_url . $row['other_profile_photo_path'];
    }

    $conversations[] = [
        "message_id" => intval($row['message_id']),
        "other_user_id" => intval($row['other_user_id']),
        "other_username" => $row['other_username'],
        "other_first_name" => $row['other_first_name'],
        "other_last_name" => $row['other_last_name'],
        "other_profile_photo_url" => $other_profile_photo_url,
        "last_message_text" => $row['message_text'],
        "last_message_media_type" => $row['media_type'],
        "last_message_media_url" => $media_url,
        "last_message_time" => $row['created_at'],
        "unread_count" => intval($row['unread_count'])
    ];
}

echo json_encode([
    "status" => "success",
    "conversations" => $conversations
]);

$stmt->close();
$conn->close();
?>

