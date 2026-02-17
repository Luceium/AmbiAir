import os

def reset_dir(path: str) -> None:
    """
    Deletes all files in the specified directory
    """
    if os.path.exists(path):
            for file in os.listdir(path):
                os.remove(os.path.join(path, file))