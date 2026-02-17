# inference.py
import sys
import joblib
import numpy as np

if len(sys.argv) != 5:
    print("Invalid input. Usage: python inference.py <indoor_temp> <outdoor_temp> <time> <window_open>")
    sys.exit(1)

try:
    indoor_temp = float(sys.argv[1])
    outdoor_temp = float(sys.argv[2])
    time_of_day = float(sys.argv[3])
    window_open = 1.0 if sys.argv[4].lower() == "true" else 0.0
except ValueError:
    print("Invalid input. Make sure to pass numeric values.")
    sys.exit(1)

model = joblib.load("ambiair_model.pkl")
X = np.array([[indoor_temp, outdoor_temp, time_of_day, window_open]])
prediction = model.predict(X)[0]
print(prediction)
