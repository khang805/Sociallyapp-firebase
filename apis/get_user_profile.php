<?php
include 'db.php';

header('Content-Type: application/json');

if (!isset($_GET['user_id'])) {
    echo json_encode(["status" => "error", "message" => "Missing user_id"]);
    exit;
}

$user_id = intval($_GET['user_id']);

// Get user basic info
$user_query = "SELECT id, username, first_name, last_name, email, dob FROM users WHERE id = ?";
$user_stmt = $conn->prepare($user_query);
$user_stmt->bind_param("i", $user_id);
$user_stmt->execute();
$user_result = $user_stmt->get_result();

if ($user_result->num_rows == 0) {
    echo json_encode(["status" => "error", "message" => "User not found"]);
    exit;
}

$user = $user_result->fetch_assoc();

// Get profile info
$profile_query = "SELECT profile_photo_path, bio, website, phone, gender FROM user_profiles WHERE user_id = ?";
$profile_stmt = $conn->prepare($profile_query);
$profile_stmt->bind_param("i", $user_id);
$profile_stmt->execute();
$profile_result = $profile_stmt->get_result();
$profile = $profile_result->fetch_assoc();

// Get follow counts
$followers_count_query = "SELECT COUNT(*) as count FROM follows WHERE following_id = ?";
$followers_stmt = $conn->prepare($followers_count_query);
$followers_stmt->bind_param("i", $user_id);
$followers_stmt->execute();
$followers_result = $followers_stmt->get_result();
$followers_count = $followers_result->fetch_assoc()['count'];

$following_count_query = "SELECT COUNT(*) as count FROM follows WHERE follower_id = ?";
$following_stmt = $conn->prepare($following_count_query);
$following_stmt->bind_param("i", $user_id);
$following_stmt->execute();
$following_result = $following_stmt->get_result();
$following_count = $following_result->fetch_assoc()['count'];

// Get posts count
$posts_count_query = "SELECT COUNT(*) as count FROM posts WHERE user_id = ?";
$posts_stmt = $conn->prepare($posts_count_query);
$posts_stmt->bind_param("i", $user_id);
$posts_stmt->execute();
$posts_result = $posts_stmt->get_result();
$posts_count = $posts_result->fetch_assoc()['count'];

$response = [
    "status" => "success",
    "user" => [
        "id" => intval($user['id']),
        "username" => $user['username'],
        "first_name" => $user['first_name'],
        "last_name" => $user['last_name'],
        "email" => $user['email'],
        "dob" => $user['dob'],
        "profile_photo_url" => $profile && $profile['profile_photo_path'] 
            ? "http://192.168.0.113/assignment-03/" . $profile['profile_photo_path'] 
            : null,
        "bio" => $profile ? $profile['bio'] : null,
        "website" => $profile ? $profile['website'] : null,
        "phone" => $profile ? $profile['phone'] : null,
        "gender" => $profile ? $profile['gender'] : null,
        "followers_count" => intval($followers_count),
        "following_count" => intval($following_count),
        "posts_count" => intval($posts_count)
    ]
];

echo json_encode($response);

$user_stmt->close();
if ($profile_stmt) $profile_stmt->close();
$followers_stmt->close();
$following_stmt->close();
$posts_stmt->close();
?>

