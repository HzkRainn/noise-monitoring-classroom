import os
import sys
import joblib
import pandas as pd

# =====================================
# ABSOLUTE PATH
# =====================================

BASE_DIR = os.path.dirname(os.path.abspath(__file__))

MODEL_PATH = os.path.join(
    BASE_DIR,
    "..",
    "models",
    "random_forest_model.pkl"
)

# =====================================
# LOAD MODEL
# =====================================

model = joblib.load(MODEL_PATH)

# =====================================
# INPUT
# =====================================

dbLevel = float(sys.argv[1])
dominantFrequency = float(sys.argv[2])
variance = float(sys.argv[3])
spikeCount = int(sys.argv[4])

# =====================================
# PREDICT
# =====================================

data = pd.DataFrame([[
    dbLevel,
    dominantFrequency,
    variance,
    spikeCount
]], columns=[
    "dbLevel",
    "dominantFrequency",
    "variance",
    "spikeCount"
])

prediction = model.predict(data)

print(prediction[0])