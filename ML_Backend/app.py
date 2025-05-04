from flask import Flask, request, jsonify
from text_summarizer import TextSummarizer

app = Flask(__name__)
summarizer = TextSummarizer()

@app.route('/test', methods=['GET'])
def test():
    return jsonify({"message": "Server is working"})


@app.route("/summarize", methods=["POST"])
def summarize():
    try:
        context = request.form.get("context")
        text = request.form.get("text")

        if not context or not text:
            return jsonify({"error": "Both 'text' and 'context' are required."}), 400

        summary = summarizer.summarize_text(context, text)
        return jsonify({"summary": summary})

    except Exception as e:
        return jsonify({"error": str(e)}), 500
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)


