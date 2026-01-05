<?php
include 'db.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['message_id']) || !isset($_POST['user_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $message_id = intval($_POST['message_id']);
    $user_id = intval($_POST['user_id']);
    $new_text = isset($_POST['message_text']) ? trim($_POST['message_text']) : null;

    if (empty($new_text)) {
        echo json_encode(["status" => "error", "message" => "Message text cannot be empty"]);
        exit;
    }

    // Check if message exists and belongs to user, and is within 5 minutes
    $check_stmt = $conn->prepare("SELECT sender_id, created_at FROM messages WHERE id = ? AND deleted_at IS NULL");
    $check_stmt->bind_param("i", $message_id);
    $check_stmt->execute();
    $result = $check_stmt->get_result();

    if ($result->num_rows == 0) {
        echo json_encode(["status" => "error", "message" => "Message not found"]);
        $check_stmt->close();
        $conn->close();
        exit;
    }

    $message = $result->fetch_assoc();
    $check_stmt->close();

    // Verify ownership
    if ($message['sender_id'] != $user_id) {
        echo json_encode(["status" => "error", "message" => "You can only edit your own messages"]);
        $conn->close();
        exit;
    }

    // Check if within 5 minutes (300 seconds)
    $created_at = strtotime($message['created_at']);
    $current_time = time();
    $time_diff = $current_time - $created_at;

    if ($time_diff > 300) {
        echo json_encode(["status" => "error", "message" => "Message can only be edited within 5 minutes"]);
        $conn->close();
        exit;
    }

    // Update message
    $update_stmt = $conn->prepare("UPDATE messages SET message_text = ?, updated_at = NOW() WHERE id = ?");
    $update_stmt->bind_param("si", $new_text, $message_id);

    if ($update_stmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Message updated successfully",
            "message_id" => $message_id,
            "new_text" => $new_text
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to update message: " . $conn->error]);
    }

    $update_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>


