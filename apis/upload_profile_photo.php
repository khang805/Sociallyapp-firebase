<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['user_id']) || !isset($_FILES['profile_photo'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $user_id = intval($_POST['user_id']);

    // Handle file upload
    $upload_dir = 'uploads/profiles/';
    if (!file_exists($upload_dir)) {
        mkdir($upload_dir, 0777, true);
    }

    $file = $_FILES['profile_photo'];
    $file_extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    $allowed_extensions = ['jpg', 'jpeg', 'png', 'gif'];

    if (!in_array($file_extension, $allowed_extensions)) {
        echo json_encode(["status" => "error", "message" => "Invalid file type"]);
        exit;
    }

    $file_name = 'profile_' . $user_id . '_' . time() . '.' . $file_extension;
    $file_path = $upload_dir . $file_name;

    if (move_uploaded_file($file['tmp_name'], $file_path)) {
        // Check if profile exists
        $check_stmt = $conn->prepare("SELECT id FROM user_profiles WHERE user_id = ?");
        $check_stmt->bind_param("i", $user_id);
        $check_stmt->execute();
        $check_result = $check_stmt->get_result();

        if ($check_result->num_rows > 0) {
            // Update existing profile
            $update_stmt = $conn->prepare("UPDATE user_profiles SET profile_photo_path = ? WHERE user_id = ?");
            $update_stmt->bind_param("si", $file_path, $user_id);
            $update_stmt->execute();
            $update_stmt->close();
        } else {
            // Insert new profile
            $insert_stmt = $conn->prepare("INSERT INTO user_profiles (user_id, profile_photo_path) VALUES (?, ?)");
            $insert_stmt->bind_param("is", $user_id, $file_path);
            $insert_stmt->execute();
            $insert_stmt->close();
        }

        $check_stmt->close();

        echo json_encode([
            "status" => "success",
            "message" => "Profile photo uploaded successfully",
            "profile_photo_url" => "http://192.168.0.113/assignment-03/" . $file_path
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to upload file"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

