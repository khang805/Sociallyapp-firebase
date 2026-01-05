<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['follower_id']) || !isset($_POST['following_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $follower_id = intval($_POST['follower_id']);
    $following_id = intval($_POST['following_id']);

    $delete_stmt = $conn->prepare("DELETE FROM follows WHERE follower_id = ? AND following_id = ?");
    $delete_stmt->bind_param("ii", $follower_id, $following_id);
    
    if ($delete_stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Unfollowed successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to unfollow"]);
    }
    
    $delete_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

