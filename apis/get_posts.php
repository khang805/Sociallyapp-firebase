<?php
include 'db.php';

header('Content-Type: application/json');

// Base URL for image paths, matching the one used in ApiService.kt
$base_url = "http://192.168.100.139/assignment-03/";

$user_id = isset($_GET['user_id']) ? intval($_GET['user_id']) : 0;

// Get all posts with user information and like/comment counts
// profile_photo_path is now in users table, not user_profiles
$query = "SELECT 
    p.id,
    p.user_id,
    p.image_path,
    p.caption,
    u.username,
    u.first_name,
    u.last_name,
    u.profile_photo_path,
    (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id) as like_count,
    (SELECT COUNT(*) FROM post_comments WHERE post_id = p.id) as comment_count,
    (SELECT COUNT(*) FROM post_likes WHERE post_id = p.id AND user_id = ?) as is_liked
FROM posts p
INNER JOIN users u ON p.user_id = u.id
ORDER BY p.id DESC
LIMIT 50";

$stmt = $conn->prepare($query);
$stmt->bind_param("i", $user_id);
$stmt->execute();
$result = $stmt->get_result();

$posts = [];
while ($row = $result->fetch_assoc()) {
    $profile_photo_url = null;
    if ($row['profile_photo_path']) {
        // Construct the full, accessible URL for the Android app
        $profile_photo_url = $base_url . $row['profile_photo_path'];
    }
    
    $posts[] = [
        "id" => intval($row['id']),
        "user_id" => intval($row['user_id']),
        "username" => $row['username'],
        "first_name" => $row['first_name'],
        "last_name" => $row['last_name'],
        "image_url" => $base_url . $row['image_path'],
        "profile_photo_url" => $profile_photo_url,
        "caption" => $row['caption'],
        "like_count" => intval($row['like_count']),
        "comment_count" => intval($row['comment_count']),
        "is_liked" => intval($row['is_liked']) > 0
    ];
}

echo json_encode(["status" => "success", "posts" => $posts]);
$stmt->close();
$conn->close();
?>

