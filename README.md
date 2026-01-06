# Project Documentation: Full Socially App 

## 1. Introduction

### 1.1. Project Overview
**"Socially"** is a comprehensive, feature-rich social networking application for the Android platform. Engineered with **Kotlin** and powered by **Google's Firebase Suite**, it provides a robust platform for users to
connect and interact through real-time messaging, video calling, content sharing, and a dynamic social feed. This project serves as a strong example of modern Android development practices, emphasizing a scalable,
serverless, and real-time backend architecture.

### 1.2. Purpose
This document provides a complete technical overview of the "Socially" Android application. It is intended for developers, project managers, and quality assurance teams. Its purpose is to detail the project's 
architecture, technological stack, key features, setup procedures, and component-level implementation guidelines.

---

## 2. System Architecture & Technology Stack

### 2.1. Architectural Design
The application is built upon the **Model-View-ViewModel (MVVM)** architectural pattern. This design paradigm ensures a clean separation of concerns:

* **View (Activities/Fragments):** The UI layer, responsible for displaying data and forwarding user actions to the ViewModel. It observes `LiveData` for state changes and updates the UI accordingly.
    * *Example:* `ChatListActivity.xml` defines the UI for the chat list screen.
* **ViewModel:** Acts as a bridge between the View and the Model (Repository). It holds UI-related data in a lifecycle-conscious way, processes user actions, and is agnostic to the Android framework.
* **Model (Repository & Data Sources):** Manages all data operations. It abstracts the data sources (Firebase services) from the rest of the app, providing a clean API for data access.

### 2.2. Technology Stack

| Category | Technology/Library | Purpose |
| :--- | :--- | :--- |
| **Language** | Kotlin | Primary development language. |
| **Backend** | Firebase Suite | Serverless backend for Auth, Database, Storage, and Notifications. |
| **Authentication** | Firebase Auth | Secure user authentication (Email/Password, Google Sign-In). |
| **Real-time DB** | Firebase Realtime DB | NoSQL database for low-latency, real-time features like chat. |
| **Structured DB** | Cloud Firestore | Advanced NoSQL database for structured data (User Profiles, Posts). |
| **Storage** | Firebase Storage | Cloud storage for user-generated content (Images, Videos). |
| **Video Calling** | Agora RTC SDK | Powering high-quality, real-time video calling. |
| **UI & Views** | ViewBinding | Type-safe access to views (eliminating `findViewById`). |
| **Image Loading** | Glide | Efficiently loading and caching images from Firebase Storage. |

---

## 3. Core Features & Implementation Details

### 3.1. User Authentication
* **Functionality:** Secure sign-up, sign-in, and session management using Google Sign-In and traditional email/password methods.
* **Implementation:**
    * Leverages `com.google.firebase:firebase-auth` for backend operations.
    * Uses `com.google.android.libraries.identity.googleid` for the "Sign in with Google" UI flow.
    * User sessions are managed automatically by the Firebase SDK.

### 3.2. Real-time Chat
* **Functionality:** One-on-one instant messaging with conversation lists and chat screens.
* **Implementation:**
    * **Data Model:** Chat messages and conversation metadata are stored in **Firebase Realtime Database** to ensure minimal latency and instant updates.
    * **UI:**
        * `activity_chat_list.xml`: Displays a list of active conversations in a RecyclerView.
        * `activity_chat.xml`: Displays the message thread for a single conversation.
    * **Logic:** A `ChatListActivity` (or Repository) attaches a `ValueEventListener` to the Realtime Database node to listen for incoming messages instantly.

### 3.3. Content Feed & Posting
* **Functionality:** Users can create posts with images and captions. A central feed displays posts from followed users.
* **Implementation:**
    * **Data Model:** Post data (author ID, caption, timestamp, likes count) is stored in **Realtime database** for powerful querying and filtering capabilities.
    * **Storage:** Post images are uploaded to **Realtime database**. The resulting download URL is saved within the corresponding Firestore document.
    * **Image Loading:** **Glide** is used to fetch, cache, and display these images efficiently.

### 3.4. Video Calling
* **Functionality:** High-quality, real-time video calls between two users.
* **Implementation:**
    * The `io.agora.rtc:full-sdk` is integrated to manage video call sessions.
    * **Process:** The app initializes the Agora engine with a unique App ID, joins a specific channel, and renders local and remote video streams on surface views.

### 3.5. Follow System
* **Functionality:** Users can follow and unfollow other profiles to curate their feed.
* **Implementation:**
    * **Data Structure:** A `Followers` and `Following` collection in **Realtime database**. Each document links the current user ID to the target user ID.
    * **Logic:** When a user taps "Follow," a transaction updates both the target user's follower count and the current user's following count atomically.

### 3.6. Story / Status Uploading
* **Functionality:** Users can upload temporary status updates (images/videos) that disappear after 24 hours.
* **Implementation:**
    * **Storage:** Media is uploaded to a dedicated `stories/` bucket in **Realtime database**.
    * **Data Model:** Metadata (timestamp, media URL) is stored in Firestore. The app filters out stories older than 24 hours during the fetch query.
    * **UI:** A horizontal RecyclerView at the top of the main feed displays active stories from followed users using circular indicators.

### 3.7. Social Engagement (Likes & Comments)
* **Functionality:** Users can like posts and leave comments to interact with content.
* **Implementation:**
    * **Likes:** Stored as a sub-collection within each Post document in Firestore to scale efficiently.
    * **Comments:** Real-time updates using Firestore listeners, allowing users to see new comments appear instantly as they are posted.

### 3.8. User Search
* **Functionality:** Efficiently discover other users by username or real name.
* **Implementation:**
    * **Querying:** Uses Firestore indexed queries (e.g., `startAt` and `endAt` methods) to perform prefix matching on usernames.
    * **UI:** A dedicated search fragment with a `SearchView` that filters results dynamically as the user types.
 
* ### 3.9. Edit Profile
* **Functionality:** Users can update their personal information, including their profile picture, username, and bio.
* **Implementation:**
    * **Data Updates:** Changes to text fields (Bio, Username) are updated directly in the user's document in **Realtime database**.
    * **Profile Picture:** If a new image is selected, it is first uploaded to **Realtime database** (replacing the old file). The new download URL is then updated in the Firestore user document.
    * **UI:** Input fields are pre-filled with existing data. A "Save" button triggers the update transaction, ensuring the UI reflects changes immediately upon success.

---

## 4. Project Setup Guide

### 4.1. Prerequisites
* **Android Studio:** Iguana (2023.2.1) or newer.
* **Android Device/Emulator:** Running API 24 (Nougat) or higher.
* **Firebase Project:** A configured project on the Firebase Console.
* **Agora Account:** An active App ID for video calling.

### 4.2. Installation Steps

 1. Clone the Repository
```bash
git clone [([https://github.com/khang805/Sociallyapp-firebase])]
cd socially-android

 2. Firebase Setup:
* Go to the [Firebase Console](https://console.firebase.google.com/).
* Create a new project.
* Enable **Authentication** (Email/Password and Google providers).
* Enable **Firestore Database**, **Realtime Database**, and **Storage**.
* Register your Android app in the console using your package name (e.g., `com.example.socially`).
* **Crucial Step:** Download the `google-services.json` file.
* Move this file into the `app/` directory of your Android project.

 3. Agora Setup
* Sign up at the [Agora Console](https://console.agora.io/).
* Create a project to obtain an **App ID**.
* Add this App ID to your project (typically in `strings.xml` or a `Constants` object) to initialize the video engine.

 4. Sync and Run
* Open the project in **Android Studio**.
* Allow Gradle to sync (this will process the `google-services.json` file).
* Build and run the application on your device or emulator.

---

## 5. Conclusion
The **"Socially"** application stands as a robust and scalable social media platform prototype. Its modern architecture and reliance on the powerful **Firebase ecosystem** make it a strong foundation for future
 development and feature expansion without the need for managing a custom backend server. This document will serve as a living guide for all ongoing development efforts.


