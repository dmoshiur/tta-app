# ThinkTank Academia - API Contract v1

All requests and responses use the standard envelope structure where applicable.
Base URL: `BuildConfig.API_BASE_URL`

## Response Envelope

Successful Response:
```json
{
  "success": true,
  "data": {}
}
```

Error Response:
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "User-friendly error message",
    "details": {}
  }
}
```

---

## Endpoints

### 1. Authentication & Users

#### Register
* **POST** `/api/v1/auth/register`
* **Request Body**:
```json
{
  "name": "Full Name",
  "email": "user@example.com",
  "password": "securepassword123"
}
```
* **Response**:
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": "user_id_123",
      "name": "Full Name",
      "email": "user@example.com"
    }
  }
}
```

#### Login
* **POST** `/api/v1/auth/login`
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securepassword123"
}
```
* **Response**:
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": "user_id_123",
      "name": "Full Name",
      "email": "user@example.com"
    }
  }
}
```

#### Forgot Password
* **POST** `/api/v1/auth/forgot-password`
* **Request Body**:
```json
{
  "email": "user@example.com"
}
```

#### Reset Password
* **POST** `/api/v1/auth/reset-password`
* **Request Body**:
```json
{
  "token": "reset_token",
  "password": "new_secure_password"
}
```

#### Get Current Profile
* **GET** `/api/v1/users/me`
* **Headers**: `Authorization: Bearer <token>`
* **Response**:
```json
{
  "success": true,
  "data": {
    "id": "user_id_123",
    "name": "Full Name",
    "email": "user@example.com",
    "avatar_url": "https://example.com/avatar.png"
  }
}
```

#### Update Profile
* **PUT** `/api/v1/users/me`
* **Headers**: `Authorization: Bearer <token>`
* **Request Body**:
```json
{
  "name": "Updated Name",
  "avatar_url": "https://example.com/new_avatar.png"
}
```

#### Change Password
* **POST** `/api/v1/users/change-password`
* **Headers**: `Authorization: Bearer <token>`
* **Request Body**:
```json
{
  "current_password": "oldpassword123",
  "new_password": "newpassword123"
}
```

---

### 2. Dashboard
* **GET** `/api/v1/dashboard`
* **Headers**: `Authorization: Bearer <token>`
* **Response**:
```json
{
  "success": true,
  "data": {
    "learning_progress": {
      "course_percentage": 45,
      "completed_lessons": 9,
      "remaining_lessons": 11,
      "completed_courses": 1
    },
    "recent_activity": [],
    "recommendations": []
  }
}
```

---

### 3. Courses

#### List Courses
* **GET** `/api/v1/courses`
* **Query Params**: `page` (int), `search` (string), `category` (string), `sort` (string)
* **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "course_1",
      "title": "Introduction to Geopolitics",
      "description": "Understanding global power struggles and diplomatic relations.",
      "thumbnail_url": "",
      "instructor": "Dr. Anthony Vance",
      "category": "World",
      "difficulty": "Beginner",
      "duration": "6 hours",
      "enrolled": false,
      "progress": 0,
      "lesson_count": 8,
      "modules": []
    }
  ]
}
```

#### Course Details
* **GET** `/api/v1/courses/{id}`
* **Response**:
```json
{
  "success": true,
  "data": {
    "id": "course_1",
    "title": "Introduction to Geopolitics",
    "description": "Understanding global power struggles and diplomatic relations.",
    "thumbnail_url": "",
    "instructor": "Dr. Anthony Vance",
    "category": "World",
    "difficulty": "Beginner",
    "duration": "6 hours",
    "enrolled": false,
    "progress": 0,
    "lesson_count": 8,
    "modules": [
      {
        "id": "mod_1",
        "title": "Foundations of Modern Statecraft",
        "lessons": [
          {
            "id": "lesson_1",
            "title": "The Concept of Sovereignty",
            "duration": "15m",
            "completed": false
          }
        ],
        "quizzes": [
          {
            "id": "quiz_1",
            "title": "Module 1 Assessment",
            "duration_minutes": 10
          }
        ]
      }
    ]
  }
}
```

#### Enroll in Course
* **POST** `/api/v1/courses/{id}/enroll`
* **Headers**: `Authorization: Bearer <token>`

---

### 4. Lessons

#### Get Lesson Details
* **GET** `/api/v1/lessons/{id}`
* **Headers**: `Authorization: Bearer <token>`
* **Response**:
```json
{
  "success": true,
  "data": {
    "id": "lesson_1",
    "course_id": "course_1",
    "title": "The Concept of Sovereignty",
    "content": "Sovereignty is the full right and power of a governing body over itself...",
    "completed": false,
    "resources": [
      {
        "title": "Treaty of Westphalia (1648) text",
        "url": "https://example.com/westphalia.pdf"
      }
    ],
    "previous_lesson_id": null,
    "next_lesson_id": "lesson_2"
  }
}
```

#### Mark Lesson Completed
* **POST** `/api/v1/lessons/{id}/complete`
* **Headers**: `Authorization: Bearer <token>`

---

### 5. Quizzes

#### Get Quiz Questions
* **GET** `/api/v1/quizzes/{id}`
* **Headers**: `Authorization: Bearer <token>`
* **Response**:
```json
{
  "success": true,
  "data": {
    "id": "quiz_1",
    "title": "Module 1 Assessment",
    "duration_minutes": 10,
    "questions": [
      {
        "id": "q1",
        "text": "Which historical treaty established the modern system of state sovereignty?",
        "options": [
          "Treaty of Versailles",
          "Treaty of Westphalia",
          "Treaty of Utrecht",
          "Treaty of Ghent"
        ]
      }
    ]
  }
}
```

#### Submit Quiz Answers
* **POST** `/api/v1/quizzes/{id}/submit`
* **Headers**: `Authorization: Bearer <token>`
* **Request Body**:
```json
{
  "answers": {
    "q1": "Treaty of Westphalia"
  }
}
```
* **Response**:
```json
{
  "success": true,
  "data": {
    "score_percentage": 100,
    "total_questions": 1,
    "correct_answers": 1,
    "review": [
      {
        "question_id": "q1",
        "question_text": "Which historical treaty established the modern system of state sovereignty?",
        "user_answer": "Treaty of Westphalia",
        "correct_answer": "Treaty of Westphalia",
        "is_correct": true,
        "explanation": "The Peace of Westphalia signed in 1648 established the principle of co-existing sovereign states."
      }
    ]
  }
}
```

---

### 6. Explore Content (Articles, Books, Knowledge, World, Humanity, Society)

#### Get Explore Items
* **GET** `/api/v1/explore`
* **Query Params**: `type` (ARTICLE, BOOK, KNOWLEDGE, WORLD, HUMANITY, SOCIETY), `category` (string), `search` (string)
* **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "explore_1",
      "type": "ARTICLE",
      "title": "The Evolution of Global Diplomacy",
      "author": "Dr. Miriam Al-Sabah",
      "date": "2026-09-10",
      "reading_time": "12m",
      "cover_url": "",
      "summary": "An exploration of diplomacy from bilateral treaties to modern multilateral forums.",
      "content": "Modern diplomacy trace its roots to...",
      "sources": ["UN Charter", "Vienna Convention"],
      "bookmarked": false,
      "details": {
        "key_ideas": ["Sovereign immunity", "Multilateral cooperation"],
        "important_lessons": ["Communication prevents conflicts"]
      }
    }
  ]
}
```

---

### 7. Bookmarks

#### List Bookmarks
* **GET** `/api/v1/bookmarks`
* **Headers**: `Authorization: Bearer <token>`

#### Add/Remove Bookmark
* **POST** `/api/v1/bookmarks/{itemId}`
* **Headers**: `Authorization: Bearer <token>`

---

### 8. Notifications

#### List Notifications
* **GET** `/api/v1/notifications`
* **Headers**: `Authorization: Bearer <token>`
* **Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "notif_1",
      "title": "New Course Available",
      "message": "Philosophy 101: Understanding Human Morality is now live!",
      "read": false,
      "timestamp": 1726750000000
    }
  ]
}
```

#### Mark Notification as Read
* **POST** `/api/v1/notifications/{id}/read`
* **Headers**: `Authorization: Bearer <token>`
