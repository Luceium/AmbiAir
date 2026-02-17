The goal of our ML model is to decide when to open or close the window. We accomplish this by classifying when a window event should occur.

We train the model on collected historical time series data. Our sensor data includes:

- indoor temperature
- outdoor temperature
- minutes since start of year
- window status (open/closed)
- window event (true/false)
  To represent the cyclical nature of the seasonal and day-night cycles we extract minuets of day and days of years. Both are represented as cos and sin values.

Some challenges include:

- dealing with time series data
- learning human inefficiencies
- low volume of data
- disproportionately more non-events
- gaps in time series data

To counter these challenges we tried:

- oversampling
- synthetic data creation using rule based system on historical data
