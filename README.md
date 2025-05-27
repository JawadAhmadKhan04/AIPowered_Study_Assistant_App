# AIPowered_Study_Assistant_App
This repository contains both the backend API and frontend application for the AI-Powered Study Assistant. The backend provides text summarization capabilities through a REST API, while the frontend offers a user-friendly interface for interacting with these features.

## Prerequisites

- Python 3.8 or higher
- pip (Python package installer)
- Android Studio (for frontend development)
- JDK 11 or higher

# Kotlin Frontend

1. Open the project in Android Studio.
2. Sync the project with Gradle files.
3. Run the application on an emulator or a physical device.

## Running the Application

1. Open the project in Android Studio.
2. Click on the "Run" button to launch the application on an emulator or a physical device.

## Frontend Structure

```
Frontend/app/src/main/java/com/musketeers_and_me/ai_powered_study_assistant_app/
├── assets/                 # Raw asset files bundled with the app (e.g., text, fonts, templates)
├── Courses/                # Screens and logic for creating, viewing, and managing courses
├── DatabaseProvider/       # Database access and operations (e.g., Firebase integration)
├── GroupStudy/             # Features for group study sessions and collaborative tools
├── LectureAndNotes/        # Management of lectures, notes, and image uploads
├── Models/                 # Data models and classes representing app entities
├── Opening_Registration/   # User onboarding, login, and registration flows
├── OuterStructure/         # App-wide structural components and navigation logic
├── QuizCenter/             # Quiz management, participation, and results display
├── QuizStructure/          # Definitions and logic for quiz formats and questions
├── Services/               # Background services and app-wide service logic
├── SmartDigest/            # AI-powered summarization and smart content extraction
├── Utils/                  # Utility classes and helper functions for common tasks
├── AuthService.kt          # Authentication service logic
├── MainActivity.kt         # Main entry point and navigation for the app
└── MyApplication.kt        # Application class for global app configuration
```
 
# Backend _ ML

This repository contains the backend API for the AI-Powered Study Assistant application. The backend provides text summarization capabilities through a REST API.

## Installation

1. Clone the repository:
```bash
cd ML_Backend
```

2. Create and activate a virtual environment:
```bash
# Windows
python -m venv venv
.\venv\Scripts\activate

# Linux/MacOS
python -m venv venv
source venv/bin/activate
```

3. Install the required dependencies:

## Running the Application

1. Start the Flask server:
```bash
python app.py
```
The server will start running at `http://127.0.0.1:5000`

## API Endpoints

---

### 📄 Text Summarization API

* **URL**: `/summarize`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                    |
| ------------------ | ------ | ------------------------------ |
| text               | string | The text you want to summarize |
| context (optional) | string | Additional context if needed   |

#### ✅ Success Response:

```json
{
    "summary": "Summarized text here"
}
```

#### ❌ Error Response:

```json
{
    "error": "Error message here"
}
```

---

### 📄 Key Points API

* **URL**: `/key_points`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                    |
| ------------------ | ------ | ------------------------------ |
| text               | string | The text from which you want to generate key points         |
| context (optional) | string | Additional context if needed   |

---

### 📄 Concept List API

* **URL**: `/concept_list`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                    |
| ------------------ | ------ | ------------------------------ |
| text               | string | The text from which you want to generate concept list |
| context (optional) | string | Additional context if needed   |

---

### 📄 Quiz Generation API

* **URL**: `/quiz`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                    |
| ------------------ | ------ | ------------------------------ |
| text               | string | The text from which you want to generate the quiz |
| context (optional) | string | Additional context if needed   |
| question_count     | int    | Total number of questions to be generated for the quiz |

---


## 🚀 Testing the API

You can test the API using tools like **Postman**:

1. Open **Postman**
2. Create a new **POST** request to: `http://127.0.0.1:5000/summarize`
3. Under the **Headers** tab, Postman will automatically set `Content-Type` when using form-data.
4. Go to the **Body** tab:

   * Select **form-data**
   * Add a key `text`, set its type to **Text**, and provide the input you want summarized
   * *(Optional)* Add a `context` key if needed
5. Click **Send** to receive the summarized output.

---

## Common Issues

1. If you get a "Method Not Allowed" error:
   - Make sure you're using POST method, not GET
   - Check that the URL is correct

2. If you get an "Unsupported Media Type" error:
   - Ensure the Content-Type header is set to application/json
   - Make sure you're sending raw JSON data, not form-data

## ML Backend Structure

```
ML_Backend/
├── app.py              # Main Flask application
├── requirements.txt    # Python dependencies
└── text_summarizer/    # Text summarization module
    ├── __init__.py
    └── summarizer.py   # Summarization implementation
```


# Backend _ STT

The Backend is hosted on Glitch and thus no setup is required

## Installation

1. Clone the repository:
```bash
cd STT_Backend
```

2. Create and activate a virtual environment:
```bash
# Windows
python -m venv venv2
.\venv2\Scripts\activate

# Linux/MacOS
python -m venv venv2
source venv2/bin/activate
```

3. Install the required dependencies:

## Running the Application

The server is running at `http://127.0.0.1:5000`

## API Endpoints

---

### 📄 Uploading Audio API

* **URL**: `/upload-audio`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                     |
| ------------------ | ------ | ------------------------------  |
| audio              | File   | The audio file you need to send |

---

### 📄 List Audios API

* **URL**: `/list_audios`
* **Method**: `GET`
* **Content-Type**: `multipart/form-data`

---

### 📄 Speech to text API

* **URL**: `/speech-to-text-url`
* **Method**: `POST`
* **Content-Type**: `multipart/form-data`

#### ✅ Request Parameters (form-data):

| Key                | Type   | Description                    |
| ------------------ | ------ | ------------------------------ |
| data               | string | The data to be returned        |
| audio-url          | string | The url of the audio           |

---

## STT Backend Structure

```
STT_Backend/
├── api.py              # Main Flask application and functionality
├── requirements.txt    # Python dependencies
├── start.sh            # Starting the server
``` 
