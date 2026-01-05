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

    // Mark all messages from other_user_id to user_id as seen
    $update_stmt = $conn->prepare("UPDATE messages SET is_seen = TRUE WHERE sender_id = ? AND receiver_id = ? AND is_seen = FALSE");
    $update_stmt->bind_param("ii", $other_user_id, $user_id);

    if ($update_stmt->execute()) {
        $affected_rows = $update_stmt->affected_rows;
        echo json_encode([
            "status" => "success",
            "message" => "Messages marked as seen",
            "affected_rows" => $affected_rows
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to mark messages as seen: " . $conn->error]);
    }

    $update_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

