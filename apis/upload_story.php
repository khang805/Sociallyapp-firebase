<?php
include 'db.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['user_id']) || !isset($_FILES['story_media'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $user_id = intval($_POST['user_id']);

    // Handle file upload
    $upload_dir = 'uploads/stories/';
    if (!file_exists($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $file = $_FILES['story_media'];
    $file_extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    
    // Determine media type
    $media_type = 'image';
    $allowed_image_extensions = ['jpg', 'jpeg', 'png', 'gif'];
    $allowed_video_extensions = ['mp4', 'mov', 'avi'];
    
    if (in_array($file_extension, $allowed_video_extensions)) {
        $media_type = 'video';
        $allowed_extensions = $allowed_video_extensions;
    } else {
        $allowed_extensions = $allowed_image_extensions;
    }

    if (!in_array($file_extension, $allowed_extensions)) {
        echo json_encode(["status" => "error", "message" => "Invalid file type. Only images (jpg, jpeg, png, gif) and videos (mp4, mov, avi) are allowed."]);
        exit;
    }

    // Generate unique filename
    $file_name = 'story_' . $user_id . '_' . time() . '_' . uniqid() . '.' . $file_extension;
    $file_path = $upload_dir . $file_name;

    if (move_uploaded_file($file['tmp_name'], $file_path)) {
        // Set expiration time to 24 hours from now
        $expires_at = date('Y-m-d H:i:s', strtotime('+24 hours'));
        
        // Insert story into database
        $insert_stmt = $conn->prepare("INSERT INTO stories (user_id, media_path, media_type, expires_at) VALUES (?, ?, ?, ?)");
        $insert_stmt->bind_param("isss", $user_id, $file_path, $media_type, $expires_at);
        
        if ($insert_stmt->execute()) {
            $story_id = $conn->insert_id;
            
            // Base URL for media paths
            $base_url = "http://192.168.100.139/assignment-03/";
            $media_url = $base_url . $file_path;
            
            echo json_encode([
                "status" => "success",
                "message" => "Story uploaded successfully",
                "story" => [
                    "id" => $story_id,
                    "user_id" => $user_id,
                    "media_url" => $media_url,
                    "media_type" => $media_type,
                    "created_at" => date('Y-m-d H:i:s'),
                    "expires_at" => $expires_at
                ]
            ]);
        } else {
            // Delete uploaded file if database insert fails
            unlink($file_path);
            echo json_encode(["status" => "error", "message" => "Failed to save story to database"]);
        }
        
        $insert_stmt->close();
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to upload file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}

$conn->close();
?>

