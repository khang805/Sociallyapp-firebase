# Stories Feature Implementation Guide

## Overview
The Stories feature allows users to upload temporary images/videos that disappear after 24 hours. Stories are displayed at the top of the home screen in a horizontal scroll view.

## Database Setup

### MySQL Table
Run the SQL script `stories_table.sql` in your MySQL database:

```sql
CREATE TABLE IF NOT EXISTS stories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    media_path VARCHAR(255) NOT NULL,
    media_type ENUM('image', 'video') DEFAULT 'image',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Note:** Stories automatically expire after 24 hours. You can create a cron job or scheduled event to delete expired stories:
```sql
DELETE FROM stories WHERE expires_at < NOW();
```

## Web APIs Created

### 1. `upload_story.php`
- **Location:** `apis/upload_story.php`
- **Method:** POST
- **Parameters:**
  - `user_id` (POST): User ID uploading the story
  - `story_media` (FILE): Image or video file
- **Response:**
  ```json
  {
    "status": "success",
    "message": "Story uploaded successfully",
    "story": {
      "id": 1,
      "user_id": 1,
      "media_url": "http://192.168.100.139/assignment-03/uploads/stories/story_1_1234567890.jpg",
      "media_type": "image",
      "created_at": "2024-01-01 12:00:00",
      "expires_at": "2024-01-02 12:00:00"
    }
  }
  ```

### 2. `get_stories.php`
- **Location:** `apis/get_stories.php`
- **Method:** GET
- **Parameters:**
  - `current_user_id` (GET, optional): Current user ID
- **Response:**
  ```json
  {
    "status": "success",
    "stories": [
      {
        "user_id": 1,
        "username": "john_doe",
        "first_name": "John",
        "last_name": "Doe",
        "profile_photo_url": "http://192.168.100.139/assignment-03/uploads/profiles/profile_1.jpg",
        "stories": [
          {
            "id": 1,
            "media_url": "http://192.168.100.139/assignment-03/uploads/stories/story_1_1234567890.jpg",
            "media_type": "image",
            "created_at": "2024-01-01 12:00:00",
            "expires_at": "2024-01-02 12:00:00"
          }
        ]
      }
    ]
  }
  ```

## Android Implementation

### Files Modified/Created:

1. **Story.kt** - Updated to match API response structure
2. **userStory.kt** - Added `profilePhotoUrl` field
3. **StoryAdapter.kt** - Updated to use Glide for URL-based images
4. **HomeActivity.kt** - Migrated from Firebase to web services
5. **ProfileActivity.kt** - Added story upload functionality
6. **StoryViewerActivity.kt** - Updated to use URLs instead of Base64
7. **ApiService.kt** - Added `uploadStory()` and `getStories()` methods

### How It Works:

1. **Uploading Stories:**
   - User opens Profile (last icon in bottom navigation)
   - User clicks "New" button (addnew ImageView)
   - Gallery opens to select image
   - Image is uploaded to server via `upload_story.php`
   - Story expires after 24 hours

2. **Viewing Stories:**
   - Stories appear at the top of HomeActivity in horizontal RecyclerView
   - Shows all active stories from all users (including own stories)
   - Stories are sorted by latest timestamp
   - Clicking a story opens StoryViewerActivity
   - Stories automatically expire after 24 hours

3. **Story Display:**
   - Circular profile image with colored border
   - Username below image
   - Story count if user has multiple stories
   - Uses Glide for efficient image loading

## File Upload Directory

Make sure the `uploads/stories/` directory exists and has write permissions:
```bash
mkdir -p uploads/stories
chmod 777 uploads/stories
```

## Testing

1. **Upload a Story:**
   - Go to Profile → Click "New" → Select image → Upload

2. **View Stories:**
   - Go to Home → See stories at top → Click to view

3. **Verify Expiration:**
   - Stories older than 24 hours should not appear
   - Run cleanup query: `DELETE FROM stories WHERE expires_at < NOW();`

## Important Notes

- Base URL is set to `http://192.168.100.139/assignment-03/` in both PHP and Android code
- Stories support both images (jpg, jpeg, png, gif) and videos (mp4, mov, avi)
- Stories are automatically filtered to show only non-expired ones
- The feature is fully integrated with the existing web service architecture

