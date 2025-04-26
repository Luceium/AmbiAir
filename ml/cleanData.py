# Clean data

# This script cleans the data from the CSV file and fills in missing values

import pandas as pd
import numpy as np

# Load the CSV file
df = pd.read_csv("periodic_backup_data.csv")
# Display the first few rows of the DataFrame
print("Original DataFrame:")
print(df.head())
# Fill missing values (gaps in time series)
#note time is an int representing minutes since start of year
