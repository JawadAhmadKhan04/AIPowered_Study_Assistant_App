from flask import Flask, request, jsonify
from text_summarizer import TextSummarizer

app = Flask(__name__)
summarizer = TextSummarizer()

@app.route("/summarize", methods=["POST"])
def summarize():
    try:
        data = request.get_json()
        if not data or "text" not in data:
            return jsonify({"error": "No text provided"}), 400
            
        text = data["text"]
        # num_sentences = data.get("num_sentences", 3)
        
        # if not isinstance(num_sentences, int) or num_sentences < 1:
        #     return jsonify({"error": "num_sentences must be a positive integer"}), 400
            
        summary = summarizer.summarize(text)
        return jsonify({"summary": summary})
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(debug=True)


