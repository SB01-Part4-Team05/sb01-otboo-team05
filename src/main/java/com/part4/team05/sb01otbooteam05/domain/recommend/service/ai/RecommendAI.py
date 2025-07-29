from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util

app = Flask(__name__)

model = SentenceTransformer('all-MiniLM-L6-v2')

@app.route('/health', methods=['GET'])
def health():
    return jsonify({"status": "healthy"}), 200

@app.route('/rank', methods=['POST'])
def rank():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "요청 데이터가 없습니다"}), 400

        sets = data if isinstance(data, list) else data.get("input", [])

        if not sets or len(sets) == 0:
            return jsonify([]), 200

        set_texts = []
        for outfit in sets:
            if not isinstance(outfit, list):
                continue
            texts = [f"{item.get('color', '')} {item.get('name', '')}" for item in outfit if isinstance(item, dict)]
            set_texts.append(" ".join(texts))

        if not set_texts:
            return jsonify([]), 200

        embeddings = model.encode(set_texts)
        avg_embedding = embeddings.mean(axis=0)

        similarities = util.cos_sim(avg_embedding, embeddings)[0]
        sorted_indices = similarities.argsort(descending=True)

        sorted_sets = [sets[i] for i in sorted_indices.tolist()]
        return jsonify(sorted_sets), 200

    except Exception as e:
        logger.error(f"추천 처리 중 오류 발생: {str(e)}")
        return jsonify({"error": "내부 서버 오류"}), 500
