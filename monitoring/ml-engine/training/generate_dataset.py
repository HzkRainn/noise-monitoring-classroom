import random
import mysql.connector

# =========================
# DATABASE CONFIG
# =========================
db = mysql.connector.connect(
    host="localhost",
    user="root",
    password="",
    database="noise_monitoring_db"
)

cursor = db.cursor()

# =========================
# GENERATE HUMAN DATA
# =========================
def generate_human():

    return (
        1,  # classroom_id

        round(random.uniform(45, 75), 2),     # db_level
        round(random.uniform(80, 300), 2),    # dominant_frequency
        round(random.uniform(1, 8), 2),       # variance
        random.randint(1, 5),                 # spike_count

        "HUMAN"
    )

# =========================
# GENERATE NON HUMAN DATA
# =========================
def generate_non_human():

    return (
        1,

        round(random.uniform(80, 120), 2),
        round(random.uniform(1000, 5000), 2),
        round(random.uniform(10, 40), 2),
        random.randint(10, 25),

        "NON_HUMAN"
    )

# =========================
# INSERT QUERY
# =========================
query = """
INSERT INTO sensor_readings
(
    classroom_id,
    db_level,
    dominant_frequency,
    variance,
    spike_count,
    training_label
)
VALUES (%s,%s,%s,%s,%s,%s)
"""

# =========================
# GENERATE DATASET
# =========================
TOTAL_DATA = 1000

print("================================")
print(" GENERATING DATASET...")
print("================================")

for i in range(TOTAL_DATA):

    # 50% HUMAN
    # 50% NON_HUMAN

    if random.random() < 0.5:
        data = generate_human()
    else:
        data = generate_non_human()

    cursor.execute(query, data)

    if i % 100 == 0:
        print(f"Inserted {i} data...")

db.commit()

print("================================")
print(" DATASET GENERATED SUCCESSFULLY ")
print("================================")

print(f"Total data inserted: {TOTAL_DATA}")

cursor.close()
db.close()