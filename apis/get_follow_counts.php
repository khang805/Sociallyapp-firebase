<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);

// Get followers count
$followers_query = "SELECT COUNT(*) as count FROM follows WHERE following_id = ?";
$followers_stmt = $conn->prepare($followers_query);
$followers_stmt->bind_param("i", $user_id);
$followers_stmt->execute();
$followers_result = $followers_stmt->get_result();
$followers_count = $followers_result->fetch_assoc()['count'];
$followers_stmt->close();

// Get following count
$following_query = "SELECT COUNT(*) as count FROM follows WHERE follower_id = ?";
$following_stmt = $conn->prepare($following_query);
$following_stmt->bind_param("i", $user_id);
$following_stmt->execute();
$following_result = $following_stmt->get_result();
$following_count = $following_result->fetch_assoc()['count'];
$following_stmt->close();

echo json_encode([
    "status" => "success",
    "followers_count" => intval($followers_count),
    "following_count" => intval($following_count)
]);
?>

