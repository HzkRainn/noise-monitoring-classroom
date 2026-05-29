import os
import pandas as pd
import joblib

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from sklearn.metrics import classification_report

# =========================================
# ABSOLUTE PATH CONFIG
# =========================================

BASE_DIR = os.path.dirname(
    os.path.abspath(__file__)
)

DATASET_PATH = os.path.join(
    BASE_DIR,
    "..",
    "dataset",
    "noise_dataset.csv"
)

MODEL_PATH = os.path.join(
    BASE_DIR,
    "..",
    "models",
    "random_forest_model.pkl"
)

# =========================================
# LOAD DATASET
# =========================================

print("===================================")
print(" LOADING DATASET ")
print("===================================")

print(f"Dataset Path : {DATASET_PATH}")

# =========================================
# DATASET VALIDATION
# =========================================

if not os.path.exists(DATASET_PATH):

    raise FileNotFoundError(
        f"Dataset not found : {DATASET_PATH}"
    )

dataset = pd.read_csv(
    DATASET_PATH
)

# =========================================
# EMPTY DATASET CHECK
# =========================================

if dataset.empty:

    raise Exception(
        "Dataset is empty"
    )

print("\nDATASET PREVIEW:")
print(dataset.head())

print("\n===================================")
print(" DATASET INFO ")
print("===================================")

print(f"Total Data : {len(dataset)}")

print("\nColumns:")
print(dataset.columns.tolist())

# =========================================
# REQUIRED COLUMN VALIDATION
# =========================================

required_columns = [
    "dbLevel",
    "dominantFrequency",
    "variance",
    "spikeCount",
    "trainingLabel"
]

missing_columns = [
    column
    for column in required_columns
    if column not in dataset.columns
]

if missing_columns:

    raise Exception(
        f"Missing columns : {missing_columns}"
    )

# =========================================
# FEATURES & LABEL
# =========================================

X = dataset[
    [
        "dbLevel",
        "dominantFrequency",
        "variance",
        "spikeCount"
    ]
]

y = dataset["trainingLabel"]

# =========================================
# LABEL DISTRIBUTION
# =========================================

print("\n===================================")
print(" LABEL DISTRIBUTION ")
print("===================================")

print(
    y.value_counts()
)

# =========================================
# SPLIT TRAIN & TEST
# =========================================

X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42,
    stratify=y
)

print("\n===================================")
print(" DATA SPLIT ")
print("===================================")

print(f"Training data : {len(X_train)}")
print(f"Testing data  : {len(X_test)}")

# =========================================
# RANDOM FOREST MODEL
# =========================================

print("\n===================================")
print(" TRAINING RANDOM FOREST ")
print("===================================")

model = RandomForestClassifier(
    n_estimators=150,
    max_depth=12,
    random_state=42,
    class_weight="balanced"
)

model.fit(
    X_train,
    y_train
)

# =========================================
# PREDICTION
# =========================================

predictions = model.predict(
    X_test
)

# =========================================
# EVALUATION
# =========================================

accuracy = accuracy_score(
    y_test,
    predictions
)

print("\n===================================")
print(" TRAINING RESULT ")
print("===================================")

print(f"Accuracy : {accuracy * 100:.2f}%")

print("\nClassification Report:")

print(
    classification_report(
        y_test,
        predictions,
        zero_division=0
    )
)

# =========================================
# FEATURE IMPORTANCE
# =========================================

print("\n===================================")
print(" FEATURE IMPORTANCE ")
print("===================================")

feature_names = [
    "dbLevel",
    "dominantFrequency",
    "variance",
    "spikeCount"
]

for feature, importance in zip(
    feature_names,
    model.feature_importances_
):

    print(
        f"{feature} : {importance:.4f}"
    )

# =========================================
# CREATE MODEL DIRECTORY
# =========================================

model_directory = os.path.dirname(
    MODEL_PATH
)

os.makedirs(
    model_directory,
    exist_ok=True
)

# =========================================
# SAVE MODEL
# =========================================

joblib.dump(
    model,
    MODEL_PATH
)

print("\n===================================")
print(" MODEL SAVED SUCCESSFULLY ")
print("===================================")

print(f"Model Path : {MODEL_PATH}")

# =========================================
# FINAL STATUS
# =========================================

print("\n===================================")
print(" TRAINING FINISHED ")
print("===================================")