import unittest

import sys
import joblib
import numpy as np

class TestModel(unittest.TestCase):
    def test_should_open(self):
        old_indoor_temp = 20.0
        indoor_temp = 22.0
        old_outdoor_temp = 15.0
        outdoor_temp = 18.0
        time_of_day = 12.0

        sin_minutes_of_day = np.sin(2 * np.pi * time_of_day / (24 * 60))
        cos_minutes_of_day = np.cos(2 * np.pi * time_of_day / (24 * 60))
        sin_day_of_year = np.sin(2 * np.pi * 1 / 365)
        cos_day_of_year = np.cos(2 * np.pi * 1 / 365)
        temp_diff = indoor_temp - outdoor_temp
        rolling_mean_indoor_temp = 0 # TODO
        rolling_std_indoor_temp = 0 # TODO: Probablly get rid of this
        rolling_mean_outdoor_temp = 0
        rolling_std_outdoor_temp = 0
        rolling_mean_temp_diff = 0
        rolling_std_temp_diff = 0
        lagged_indoor_temp = old_indoor_temp
        lagged_outdoor_temp = old_outdoor_temp


        model = joblib.load("ambiair_model.pkl")
        X = np.array([[indoor_temp,outdoor_temp,sin_minutes_of_day,cos_minutes_of_day,sin_day_of_year,cos_day_of_year,temp_diff,rolling_mean_indoor_temp,rolling_std_indoor_temp,rolling_mean_outdoor_temp,rolling_std_outdoor_temp,rolling_mean_temp_diff,rolling_std_temp_diff,lagged_indoor_temp,lagged_outdoor_temp]])
        prediction = model.predict(X)[0]
        self.assertTrue(prediction, "The model should predict that the window should be open.")

if __name__ == '__main__':
    unittest.main()