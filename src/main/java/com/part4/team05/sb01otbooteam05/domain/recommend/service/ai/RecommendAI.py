from flask import Flask, request, jsonify
from sentence_transformers import SentenceTransformer, util

app = Flask(__name__)
model = SentenceTransformer('all-MiniLM-L6-v2')

@app.route('/rank', methods=['POST'])
def rank():
    data = request.get_json()
    sets = data if isinstance(data, list) else data.get("input", [])

    set_texts = []
    for outfit in sets:
        texts = [f"{item.get('color', '')} {item.get('name', '')}" for item in outfit]
        set_texts.append(" ".join(texts))

    embeddings = model.encode(set_texts)
    avg_embedding = embeddings.mean(axis=0)

    similarities = util.cos_sim(avg_embedding, embeddings)[0]
    sorted_indices = similarities.argsort(descending=True)

    sorted_sets = [sets[i] for i in sorted_indices.tolist()]
    return jsonify(sorted_sets)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
