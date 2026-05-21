import pandas as pd
import joblib

from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score
from sklearn.metrics import classification_report

# =========================================
# LOAD DATASET
# =========================================
print("===================================")
print(" LOADING DATASET ")
print("===================================")

dataset = pd.read_csv("../dataset/noise_dataset.csv")

print(dataset.head())

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

y = dataset["label"]

# =========================================
# SPLIT TRAIN & TEST
# =========================================
X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.2,
    random_state=42
)

print("\nTraining data:", len(X_train))
print("Testing data:", len(X_test))

# =========================================
# RANDOM FOREST MODEL
# =========================================
print("\n===================================")
print(" TRAINING RANDOM FOREST ")
print("===================================")

model = RandomForestClassifier(
    n_estimators=100,
    random_state=42
)

model.fit(X_train, y_train)

# =========================================
# PREDICTION
# =========================================
predictions = model.predict(X_test)

# =========================================
# EVALUATION
# =========================================
accuracy = accuracy_score(y_test, predictions)

print("\n===================================")
print(" TRAINING RESULT ")
print("===================================")

print(f"Accuracy: {accuracy * 100:.2f}%")

print("\nClassification Report:")
print(classification_report(y_test, predictions))

# =========================================
# SAVE MODEL
# =========================================
joblib.dump(
    model,
    "../models/random_forest_model.pkl"
)

print("\n===================================")
print(" MODEL SAVED SUCCESSFULLY ")
print("===================================")