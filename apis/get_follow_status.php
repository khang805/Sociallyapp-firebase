<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['follower_id']) || !isset($_GET['following_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing required fields"]);
    exit;
}

$follower_id = intval($_GET['follower_id']);
$following_id = intval($_GET['following_id']);

// Check if following
$check_follow = $conn->prepare("SELECT id FROM follows WHERE follower_id = ? AND following_id = ?");
$check_follow->bind_param("ii", $follower_id, $following_id);
$check_follow->execute();
$is_following = $check_follow->get_result()->num_rows > 0;
$check_follow->close();

// Check if there's a pending request
$check_request = $conn->prepare("SELECT id FROM follow_requests WHERE sender_id = ? AND receiver_id = ? AND status = 'pending'");
$check_request->bind_param("ii", $follower_id, $following_id);
$check_request->execute();
$has_pending_request = $check_request->get_result()->num_rows > 0;
$check_request->close();

$status = "not_following";
if ($is_following) {
    $status = "following";
} else if ($has_pending_request) {
    $status = "requested";
}

echo json_encode([
    "status" => "success",
    "follow_status" => $status,
    "is_following" => $is_following,
    "has_pending_request" => $has_pending_request
]);
$conn->close();
?>

