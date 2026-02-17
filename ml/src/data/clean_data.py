import pandas as pd
from ..utils import reset_dir

def extract_continuous_series(df: pd.DataFrame, jump_point: int = 5) -> None:
    """
    Splits data into continuous series based on the 'time' column.
    If the difference between consecutive 'time' values is greater than `jump_point` minutes, a new series is started.
    """

    df['series'] = (df['time'].diff() > jump_point).cumsum()

    # save in ../data/continuous_series/`group_id`.csv
    reset_dir.reset_dir("../data/continuous_series")
    for group_id, group in df.groupby('series'):
        group.drop(columns=['series'], inplace=True)
        group.to_csv(f"../data/continuous_series/{group_id}.csv", index=False)