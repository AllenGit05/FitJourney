import json
import requests
import os

# Read project config
config_path = os.path.join(os.path.dirname(__file__), '../app/google-services.json')
with open(config_path, 'r') as f:
    config = json.load(f)

project_id = config['project_info']['project_id']
base_url = f"https://firestore.googleapis.com/v1/projects/{project_id}/databases/(default)/documents"

collections_to_wipe = [
    "users", "workouts", "diet_entries", "water_entries",
    "steps_entries", "weight_entries", "chat_messages",
    "habits", "progress_photo_metadata", "weekly_reports", "sync_metadata"
]

print(f"Wiping Firebase project: {project_id}")
print("NOTE: Run this manually if needed, or use Firebase Console to delete collections.")
print("This script is a template. Real deletion requires OAuth2 tokens if using REST API.")
print("Collections that will be created fresh:")
for col in collections_to_wipe:
    print(f"  - {col}")
