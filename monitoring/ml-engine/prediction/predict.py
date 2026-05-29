import os
import sys
import joblib
import pandas as pd

# =====================================
# ABSOLUTE PATH
# =====================================

BASE_DIR = os.path.dirname(
    os.path.abspath(__file__)
)

MODEL_PATH = os.path.join(
    BASE_DIR,
    "..",
    "models",
    "random_forest_model.pkl"
)

# =====================================
# MODEL VALIDATION
# =====================================

if not os.path.exists(MODEL_PATH):

    print("MODEL_NOT_FOUND|0.0")

    sys.stdout.flush()

    sys.exit(1)

# =====================================
# LOAD MODEL
# =====================================

model = joblib.load(
    MODEL_PATH
)

# =====================================
# INPUT VALIDATION
# =====================================

try:

    dbLevel = float(sys.argv[1])

    dominantFrequency = float(sys.argv[2])

    variance = float(sys.argv[3])

    spikeCount = int(sys.argv[4])

except Exception as e:

    print("INVALID_INPUT|0.0")

    sys.stdout.flush()

    sys.exit(1)

# =====================================
# DATAFRAME
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

# =====================================
# PREDICTION
# =====================================

try:

    prediction = model.predict(data)[0]

    # =================================
    # CONFIDENCE SCORE
    # =================================

    probabilities = model.predict_proba(data)[0]

    confidence = max(probabilities)

    # =================================
    # OUTPUT FORMAT
    # prediction|confidence
    # =================================

    result = f"{prediction}|{confidence:.4f}"

    print(result)

except Exception as e:

    print("PREDICTION_ERROR|0.0")

    sys.stdout.flush()

    sys.exit(1)