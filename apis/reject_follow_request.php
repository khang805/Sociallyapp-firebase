<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['receiver_id']) || !isset($_POST['sender_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $receiver_id = intval($_POST['receiver_id']);
    $sender_id = intval($_POST['sender_id']);

    $update_stmt = $conn->prepare("UPDATE follow_requests SET status = 'rejected' WHERE sender_id = ? AND receiver_id = ? AND status = 'pending'");
    $update_stmt->bind_param("ii", $sender_id, $receiver_id);
    
    if ($update_stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Follow request rejected"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to reject follow request"]);
    }
    
    $update_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

