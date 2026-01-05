<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['sender_id']) || !isset($_POST['receiver_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $sender_id = intval($_POST['sender_id']);
    $receiver_id = intval($_POST['receiver_id']);

    // Prevent self-follow
    if ($sender_id == $receiver_id) {
        echo json_encode(["status" => "error", "message" => "Cannot follow yourself"]);
        exit;
    }

    // Check if already following
    $check_follow = $conn->prepare("SELECT id FROM follows WHERE follower_id = ? AND following_id = ?");
    $check_follow->bind_param("ii", $sender_id, $receiver_id);
    $check_follow->execute();
    if ($check_follow->get_result()->num_rows > 0) {
        echo json_encode(["status" => "error", "message" => "Already following this user"]);
        $check_follow->close();
        exit;
    }
    $check_follow->close();

    // Check if request already exists
    $check_request = $conn->prepare("SELECT id, status FROM follow_requests WHERE sender_id = ? AND receiver_id = ?");
    $check_request->bind_param("ii", $sender_id, $receiver_id);
    $check_request->execute();
    $request_result = $check_request->get_result();
    
    if ($request_result->num_rows > 0) {
        $existing = $request_result->fetch_assoc();
        if ($existing['status'] == 'pending') {
            echo json_encode(["status" => "error", "message" => "Follow request already pending"]);
            $check_request->close();
            exit;
        } else {
            // Update existing request to pending
            $update_stmt = $conn->prepare("UPDATE follow_requests SET status = 'pending' WHERE sender_id = ? AND receiver_id = ?");
            $update_stmt->bind_param("ii", $sender_id, $receiver_id);
            $update_stmt->execute();
            $update_stmt->close();
            echo json_encode(["status" => "success", "message" => "Follow request sent"]);
            $check_request->close();
            exit;
        }
    }
    $check_request->close();

    // Create new follow request
    $insert_stmt = $conn->prepare("INSERT INTO follow_requests (sender_id, receiver_id, status) VALUES (?, ?, 'pending')");
    $insert_stmt->bind_param("ii", $sender_id, $receiver_id);
    
    if ($insert_stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Follow request sent"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to send follow request"]);
    }
    
    $insert_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

