<?php
include 'db.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['user_id']) || !isset($_POST['other_user_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $user_id = intval($_POST['user_id']);
    $other_user_id = intval($_POST['other_user_id']);

    // Delete all vanish mode messages that have been seen between these two users
    // This is called when chat is closed
    $delete_stmt = $conn->prepare("UPDATE messages SET deleted_at = NOW() WHERE is_vanish_mode = TRUE AND is_seen = TRUE AND ((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) AND deleted_at IS NULL");
    $delete_stmt->bind_param("iiii", $user_id, $other_user_id, $other_user_id, $user_id);

    if ($delete_stmt->execute()) {
        $affected_rows = $delete_stmt->affected_rows;
        echo json_encode([
            "status" => "success",
            "message" => "Vanish mode messages deleted",
            "deleted_count" => $affected_rows
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to delete vanish mode messages: " . $conn->error]);
    }

    $delete_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

