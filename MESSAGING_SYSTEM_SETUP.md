# Messaging System Implementation Guide

## Overview
A complete messaging system with vanish mode, media sharing, message editing/deletion, and offline support.

## Features Implemented

### 1. Vanish Mode
- Messages disappear once seen and chat is closed
- Toggle vanish mode when sending messages
- Automatic deletion of vanish mode messages when chat closes

### 2. Media Sharing
- Send text messages
- Send images (jpg, jpeg, png, gif, webp)
- Send videos (mp4, mov, avi, mkv, webm)
- Send files (pdf, doc, docx, txt, zip, rar)

### 3. Message Editing & Deletion
- Edit messages within 5 minutes of sending
- Delete messages within 5 minutes of sending
- Only sender can edit/delete their own messages

### 4. Offline Support
- Messages cached locally in SQLite
- Load from cache first, then sync with server
- Works offline for viewing cached messages

## Database Setup

### MySQL/MariaDB (Server)
Run the SQL file to create the messages table:
```sql
-- Run: messages_table.sql
```

### SQLite (Android - Local Cache)
Automatically created by `MessageDatabaseHelper.kt`

## API Endpoints

All endpoints are in the `apis/` directory:

1. **send_message.php** - Send a message (text, image, video, or file)
2. **get_messages.php** - Get messages between two users
3. **get_conversations.php** - Get all conversations for a user
4. **mark_messages_seen.php** - Mark messages as seen
5. **delete_vanish_messages.php** - Delete vanish mode messages
6. **edit_message.php** - Edit a message (within 5 minutes)
7. **delete_message.php** - Delete a message (within 5 minutes)

## Android Components

### Activities
- **ChatListActivity** - Shows all conversations
- **ChatActivity** - Individual chat screen with message input

### Adapters
- **ConversationAdapter** - Displays conversation list
- **ChatMessageAdapter** - Displays messages in chat

### Data Models
- **ChatMessage** - Represents a single message
- **Conversation** - Represents a conversation with another user

### Database Helper
- **MessageDatabaseHelper** - SQLite helper for local caching

## Usage

### Starting Chat List
```kotlin
val intent = Intent(this, ChatListActivity::class.java)
startActivity(intent)
```

### Starting Individual Chat
```kotlin
val intent = Intent(this, ChatActivity::class.java)
intent.putExtra("other_user_id", userId)
intent.putExtra("other_username", username)
intent.putExtra("other_first_name", firstName)
intent.putExtra("other_last_name", lastName)
intent.putExtra("other_profile_photo_url", profilePhotoUrl)
startActivity(intent)
```

## Features Details

### Vanish Mode
- Toggle vanish mode using the icon in chat header
- When enabled, messages are marked with `is_vanish_mode = true`
- When chat closes, all seen vanish mode messages are deleted from server
- Local cache also cleans up vanish mode messages

### Message Editing
- Long press on your own message (within 5 minutes)
- Select "Edit" from dialog
- Enter new text and save
- Message is updated on server and local cache

### Message Deletion
- Long press on your own message (within 5 minutes)
- Select "Delete" from dialog
- Confirm deletion
- Message is removed from server and local cache

### Media Sharing
- Click attach button in chat
- Select image, video, or file from gallery/files
- File is uploaded and message is sent
- Media is displayed in chat

### Offline Support
- Messages are cached in SQLite when loaded
- On app start, cached messages are shown first
- Then server is queried for updates
- Works offline for viewing cached messages

## Auto-Refresh
- ChatListActivity refreshes every 5 seconds
- ChatActivity refreshes every 3 seconds
- Ensures new messages appear automatically

## File Structure

```
apis/
  ├── send_message.php
  ├── get_messages.php
  ├── get_conversations.php
  ├── mark_messages_seen.php
  ├── delete_vanish_messages.php
  ├── edit_message.php
  └── delete_message.php

app/src/main/java/com/example/assignment1/
  ├── ChatListActivity.kt
  ├── ChatActivity.kt
  ├── ConversationAdapter.kt
  ├── ChatMessageAdapter.kt
  ├── ChatMessage.kt
  ├── Conversation.kt
  └── MessageDatabaseHelper.kt

app/src/main/res/layout/
  ├── activity_chat_list.xml
  ├── activity_chat.xml
  ├── item_conversation.xml
  └── item_message.xml
```

## Notes

1. **Base URL**: Update `BASE_URL` in `ApiService.kt` if your server URL changes
2. **File Upload Directory**: Ensure `uploads/messages/` directory exists on server with write permissions
3. **Drawable Resources**: Some drawable resources may need to be created:
   - `circle_badge` - For unread count badge
   - `rounded_edittext` - For message input background
   - `sent_message_background` - For sent message bubble
   - `received_message_background` - For received message bubble

4. **Permissions**: Ensure app has required permissions:
   - Internet (for API calls)
   - Read/Write External Storage (for media files)

## Testing Checklist

- [ ] Send text message
- [ ] Send image message
- [ ] Send video message
- [ ] Send file message
- [ ] Edit message within 5 minutes
- [ ] Delete message within 5 minutes
- [ ] Try editing/deleting after 5 minutes (should fail)
- [ ] Enable vanish mode and send message
- [ ] Close chat with vanish mode enabled (messages should be deleted)
- [ ] Mark messages as seen
- [ ] View conversations list
- [ ] Open individual chat
- [ ] Test offline mode (view cached messages)
- [ ] Auto-refresh works (new messages appear automatically)


