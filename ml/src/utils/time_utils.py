import pandas as pd

def minutes_of_year_to_datetime(minutes_of_year: int) -> pd.Timestamp:
    """
    Convert minutes of the year to a datetime object.
    
    Args:
        minutes_of_year (int): Minutes since the start of the year.
        
    Returns:
        pd.Timestamp: Corresponding datetime object.
    """
    # Calculate the number of days and remaining minutes
    days = minutes_of_year // (24 * 60)
    minutes = minutes_of_year % (24 * 60)
    
    # Create a datetime object for the start of the year
    start_of_year = pd.Timestamp(year=2025, month=1, day=1)
    
    # Add the days and minutes to the start of the year
    return start_of_year + pd.Timedelta(days=days, minutes=minutes)