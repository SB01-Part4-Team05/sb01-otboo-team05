from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util
import logging

app = Flask(__name__)

model = SentenceTransformer('all-MiniLM-L6-v2')
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"}), 200
@app.route('/rank', methods=['POST'])
def rank():
    try:
        data = request.get_json()
        if not data:
            return jsonify([[]]), 200

        sets = data if isinstance(data, list) else data.get("input", [])
        if not sets or not isinstance(sets, list):
            return jsonify([[]]), 200

        set_texts = []
        for outfit in sets:
            if not isinstance(outfit, list):
                set_texts.append("")
                continue
            texts = [f"{item.get('color', '')} {item.get('name', '')}".strip() for item in outfit if isinstance(item, dict)]
            set_texts.append(" ".join(texts).strip())

        if not any(set_texts):
            return jsonify(sets), 200

        embeddings = model.encode(set_texts)
        avg_embedding = embeddings.mean(axis=0)

        similarities = util.cos_sim(avg_embedding, embeddings)[0]
        sorted_indices = similarities.argsort(descending=True)
        sorted_sets = [sets[i] for i in sorted_indices.tolist()]

        return jsonify(sorted_sets), 200

    except Exception as e:
        logger.exception("AI 추천 중 오류 발생")
        return jsonify([[]]), 200
