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

    // Check if message exists and belongs to user, and is within 5 minutes
    $check_stmt = $conn->prepare("SELECT sender_id, created_at, media_path FROM messages WHERE id = ? AND deleted_at IS NULL");
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
        echo json_encode(["status" => "error", "message" => "You can only delete your own messages"]);
        $conn->close();
        exit;
    }

    // Check if within 5 minutes (300 seconds)
    $created_at = strtotime($message['created_at']);
    $current_time = time();
    $time_diff = $current_time - $created_at;

    if ($time_diff > 300) {
        echo json_encode(["status" => "error", "message" => "Message can only be deleted within 5 minutes"]);
        $conn->close();
        exit;
    }

    // Delete media file if exists
    if ($message['media_path'] && file_exists($message['media_path'])) {
        unlink($message['media_path']);
    }

    // Soft delete message
    $delete_stmt = $conn->prepare("UPDATE messages SET deleted_at = NOW() WHERE id = ?");
    $delete_stmt->bind_param("i", $message_id);

    if ($delete_stmt->execute()) {
        echo json_encode([
            "status" => "success",
            "message" => "Message deleted successfully",
            "message_id" => $message_id
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to delete message: " . $conn->error]);
    }

    $delete_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>


