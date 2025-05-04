from flask import Flask, request, jsonify
from Smart_Digest import SmartDigest

app = Flask(__name__)
smart_digest = SmartDigest()

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

        summary = smart_digest.summarize_text(context, text)
        return jsonify({"summary": summary})

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route("/key_points", methods=["POST"])
def KeyPoints():
    try:
        context = request.form.get("context")
        text = request.form.get("text")

        if not context or not text:
            return jsonify({"error": "Both 'text' and 'context' are required."}), 400

        key_points = smart_digest.extract_key_points(context, text)
        return jsonify({"key_points": key_points})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route("/concept_list", methods=["POST"])
def ConceptList():
    try:
        context = request.form.get("context")
        text = request.form.get("text")

        if not context or not text:
            return jsonify({"error": "Both 'text' and 'context' are required."}), 400

        concept_list = smart_digest.extract_concept_list(context, text)
        return jsonify({"concept_list": concept_list})

    except Exception as e:
        return jsonify({"error": str(e)}), 500



if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)


