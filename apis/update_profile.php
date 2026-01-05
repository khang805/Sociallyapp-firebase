<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['user_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing user_id"]);
        exit;
    }

    $user_id = intval($_POST['user_id']);
    $first_name = isset($_POST['first_name']) ? trim($_POST['first_name']) : null;
    $last_name = isset($_POST['last_name']) ? trim($_POST['last_name']) : null;
    $username = isset($_POST['username']) ? trim($_POST['username']) : null;
    $bio = isset($_POST['bio']) ? trim($_POST['bio']) : null;
    $website = isset($_POST['website']) ? trim($_POST['website']) : null;
    $phone = isset($_POST['phone']) ? trim($_POST['phone']) : null;
    $gender = isset($_POST['gender']) ? trim($_POST['gender']) : null;

    // Update users table
    if ($first_name !== null || $last_name !== null || $username !== null) {
        $updates = [];
        $params = [];
        $types = "";

        if ($first_name !== null) {
            $updates[] = "first_name = ?";
            $params[] = $first_name;
            $types .= "s";
        }
        if ($last_name !== null) {
            $updates[] = "last_name = ?";
            $params[] = $last_name;
            $types .= "s";
        }
        if ($username !== null) {
            // Check if username already exists (excluding current user)
            $check_stmt = $conn->prepare("SELECT id FROM users WHERE username = ? AND id != ?");
            $check_stmt->bind_param("si", $username, $user_id);
            $check_stmt->execute();
            $check_result = $check_stmt->get_result();
            if ($check_result->num_rows > 0) {
                echo json_encode(["status" => "error", "message" => "Username already exists"]);
                $check_stmt->close();
                exit;
            }
            $check_stmt->close();

            $updates[] = "username = ?";
            $params[] = $username;
            $types .= "s";
        }

        if (!empty($updates)) {
            $params[] = $user_id;
            $types .= "i";
            $update_query = "UPDATE users SET " . implode(", ", $updates) . " WHERE id = ?";
            $update_stmt = $conn->prepare($update_query);
            $update_stmt->bind_param($types, ...$params);
            $update_stmt->execute();
            $update_stmt->close();
        }
    }

    // Update or insert user_profiles table
    $check_profile = $conn->prepare("SELECT id FROM user_profiles WHERE user_id = ?");
    $check_profile->bind_param("i", $user_id);
    $check_profile->execute();
    $profile_exists = $check_profile->get_result()->num_rows > 0;
    $check_profile->close();

    if ($profile_exists) {
        // Update existing profile
        $updates = [];
        $params = [];
        $types = "";

        if ($bio !== null) {
            $updates[] = "bio = ?";
            $params[] = $bio;
            $types .= "s";
        }
        if ($website !== null) {
            $updates[] = "website = ?";
            $params[] = $website;
            $types .= "s";
        }
        if ($phone !== null) {
            $updates[] = "phone = ?";
            $params[] = $phone;
            $types .= "s";
        }
        if ($gender !== null) {
            $updates[] = "gender = ?";
            $params[] = $gender;
            $types .= "s";
        }

        if (!empty($updates)) {
            $params[] = $user_id;
            $types .= "i";
            $update_query = "UPDATE user_profiles SET " . implode(", ", $updates) . " WHERE user_id = ?";
            $update_stmt = $conn->prepare($update_query);
            $update_stmt->bind_param($types, ...$params);
            $update_stmt->execute();
            $update_stmt->close();
        }
    } else {
        // Insert new profile
        $insert_stmt = $conn->prepare("INSERT INTO user_profiles (user_id, bio, website, phone, gender) VALUES (?, ?, ?, ?, ?)");
        $insert_stmt->bind_param("issss", $user_id, $bio, $website, $phone, $gender);
        $insert_stmt->execute();
        $insert_stmt->close();
    }

    echo json_encode(["status" => "success", "message" => "Profile updated successfully"]);
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

