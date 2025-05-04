# AIPowered_Study_Assistant_App
 
# AI-Powered Study Assistant Backend

This repository contains the backend API for the AI-Powered Study Assistant application. The backend provides text summarization capabilities through a REST API.

## Prerequisites

- Python 3.8 or higher
- pip (Python package installer)

## Installation

1. Clone the repository:
```bash
git clone <repository-url>
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

## Project Structure

```
ML_Backend/
├── app.py              # Main Flask application
├── requirements.txt    # Python dependencies
└── text_summarizer/    # Text summarization module
    ├── __init__.py
    └── summarizer.py   # Summarization implementation
``` 