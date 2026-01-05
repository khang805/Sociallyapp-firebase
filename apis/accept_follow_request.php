<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['receiver_id']) || !isset($_POST['sender_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $receiver_id = intval($_POST['receiver_id']);
    $sender_id = intval($_POST['sender_id']);

    // Start transaction
    $conn->begin_transaction();

    try {
        // Update follow request status
        $update_request = $conn->prepare("UPDATE follow_requests SET status = 'accepted' WHERE sender_id = ? AND receiver_id = ? AND status = 'pending'");
        $update_request->bind_param("ii", $sender_id, $receiver_id);
        $update_request->execute();
        
        if ($update_request->affected_rows == 0) {
            throw new Exception("Follow request not found or already processed");
        }
        $update_request->close();

        // Create follow relationship
        $insert_follow = $conn->prepare("INSERT INTO follows (follower_id, following_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE id=id");
        $insert_follow->bind_param("ii", $sender_id, $receiver_id);
        $insert_follow->execute();
        $insert_follow->close();

        $conn->commit();
        echo json_encode(["status" => "success", "message" => "Follow request accepted"]);
    } catch (Exception $e) {
        $conn->rollback();
        echo json_encode(["status" => "error", "message" => $e->getMessage()]);
    }
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

