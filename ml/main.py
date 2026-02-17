import pandas as pd
from sklearn.linear_model import LogisticRegression
import pickle

# Get and format training data
df = pd.read_csv('data/expanded_data.csv').drop(columns=['date_time'])
X = df.drop(columns=['window-open', 'window-event'])
y_window_open = df['window-open']

# Train model: logistic regression on window status
model = LogisticRegression(max_iter=1000)
model.fit(X, y_window_open)

# Export model as .pkl file
with open('models/logistic_regression_window_status.pkl', 'wb') as f:
    pickle.dump(model, f)