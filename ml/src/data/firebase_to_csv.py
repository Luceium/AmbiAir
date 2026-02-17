import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import csv

# Replace with the path to your Firebase Admin SDK credentials JSON file
cred = credentials.Certificate("passive-cool-firebase-adminsdk-nc11k-538c970b9e.json")

try:
    firebase_admin.initialize_app(cred, {
        'projectId': "passive-cool",  # Replace with your Firebase Project ID
    })
except ValueError as e:
    if str(e) == 'The default Firebase app already exists.':
        pass  # App already initialized
    else:
        raise

db = firestore.client()
collection_ref = db.collection("periodic_data")
docs = collection_ref.stream()

output_file = "periodic_backup_data.csv"

with open(output_file, 'w', newline='') as csvfile:
    csv_writer = csv.writer(csvfile)
    header_written = False
    data_count = 0

    for doc in docs:
        doc_dict = doc.to_dict()
        data_count += 1

        if not header_written and doc_dict:
            header = doc_dict.keys()
            csv_writer.writerow(header)
            header_written = True

        csv_writer.writerow(doc_dict.values())

print(f"Successfully exported {data_count} entries to {output_file}")

# It's good practice to close the Firebase app if it's no longer needed in a long-running script
# firebase_admin.delete_app(firebase_admin.get_app())
