<?php
include 'db.php';
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    if (!isset($_POST['sender_id']) || !isset($_POST['receiver_id'])) {
        echo json_encode(["status" => "error", "message" => "Missing required fields"]);
        exit;
    }

    $sender_id = intval($_POST['sender_id']);
    $receiver_id = intval($_POST['receiver_id']);
    $message_text = isset($_POST['message_text']) ? trim($_POST['message_text']) : null;
    $media_type = isset($_POST['media_type']) ? $_POST['media_type'] : 'text';
    $is_vanish_mode = isset($_POST['is_vanish_mode']) ? filter_var($_POST['is_vanish_mode'], FILTER_VALIDATE_BOOLEAN) : false;

    // Prevent self-messaging
    if ($sender_id == $receiver_id) {
        echo json_encode(["status" => "error", "message" => "Cannot send message to yourself"]);
        exit;
    }

    // Validate media type
    $allowed_media_types = ['text', 'image', 'video', 'file'];
    if (!in_array($media_type, $allowed_media_types)) {
        echo json_encode(["status" => "error", "message" => "Invalid media type"]);
        exit;
    }

    $media_path = null;
    $base_url = "http://192.168.100.139/assignment-03/";

    // Handle file upload if media type is not text
    if ($media_type != 'text' && isset($_FILES['media_file'])) {
        $upload_dir = 'uploads/messages/';
        if (!file_exists($upload_dir)) {
            mkdir($upload_dir, 0777, true);
        }

        $file = $_FILES['media_file'];
        $file_extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
        
        // Validate file type based on media_type
        if ($media_type == 'image') {
            $allowed_extensions = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
        } elseif ($media_type == 'video') {
            $allowed_extensions = ['mp4', 'mov', 'avi', 'mkv', 'webm'];
        } else { // file
            $allowed_extensions = ['pdf', 'doc', 'docx', 'txt', 'zip', 'rar'];
        }

        if (!in_array($file_extension, $allowed_extensions)) {
            echo json_encode(["status" => "error", "message" => "Invalid file type for $media_type"]);
            exit;
        }

        $file_name = 'msg_' . $sender_id . '_' . $receiver_id . '_' . time() . '_' . uniqid() . '.' . $file_extension;
        $file_path = $upload_dir . $file_name;

        if (!move_uploaded_file($file['tmp_name'], $file_path)) {
            echo json_encode(["status" => "error", "message" => "Failed to upload file"]);
            exit;
        }

        $media_path = $file_path;
    } elseif ($media_type != 'text' && !isset($_FILES['media_file'])) {
        echo json_encode(["status" => "error", "message" => "Media file required for non-text messages"]);
        exit;
    }

    // Validate: text message must have message_text, media message must have media_path
    if ($media_type == 'text' && (empty($message_text) || $message_text === null)) {
        echo json_encode(["status" => "error", "message" => "Message text is required for text messages"]);
        exit;
    }

    if ($media_type != 'text' && empty($media_path)) {
        echo json_encode(["status" => "error", "message" => "Media file is required"]);
        exit;
    }

    // Insert message into database
    $insert_stmt = $conn->prepare("INSERT INTO messages (sender_id, receiver_id, message_text, media_type, media_path, is_vanish_mode) VALUES (?, ?, ?, ?, ?, ?)");
    $insert_stmt->bind_param("iisssi", $sender_id, $receiver_id, $message_text, $media_type, $media_path, $is_vanish_mode);

    if ($insert_stmt->execute()) {
        $message_id = $conn->insert_id;
        $media_url = $media_path ? $base_url . $media_path : null;
        
        echo json_encode([
            "status" => "success",
            "message" => "Message sent successfully",
            "message_id" => $message_id,
            "sender_id" => $sender_id,
            "receiver_id" => $receiver_id,
            "message_text" => $message_text,
            "media_type" => $media_type,
            "media_url" => $media_url,
            "is_vanish_mode" => $is_vanish_mode,
            "created_at" => date('Y-m-d H:i:s')
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Failed to send message: " . $conn->error]);
    }

    $insert_stmt->close();
    $conn->close();
} else {
    echo json_encode(["status" => "error", "message" => "Invalid request method"]);
}
?>

