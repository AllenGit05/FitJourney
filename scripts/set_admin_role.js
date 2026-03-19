/**
 * set_admin_role.js
 * 
 * This script uses the Firebase Admin SDK to grant the 'admin' role to a specific user
 * via Custom Claims. This is the secure way to handle administrative access.
 * 
 * Usage:
 * 1. Install dependencies: npm install firebase-admin
 * 2. Download your service account key JSON from Firebase Console (Project Settings > Service Accounts).
 * 3. Set the path to your service account key and the target UID below.
 * 4. Run: node set_admin_role.js
 */

const admin = require('firebase-admin');

// TODO: Replace with the path to your service account key file
const serviceAccount = require('./serviceAccountKey.json');

// TODO: Replace with the UID of the user you want to make an admin
const targetUid = 'TARGET_USER_UID_HERE';

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function setAdminRole(uid) {
  try {
    await admin.auth().setCustomUserClaims(uid, { role: 'admin' });
    console.log(`Successfully granted admin role to user: ${uid}`);
    
    // Verify the claims
    const user = await admin.auth().getUser(uid);
    console.log('Current custom claims:', user.customClaims);
    
    process.exit(0);
  } catch (error) {
    console.error('Error setting custom claims:', error);
    process.exit(1);
  }
}

setAdminRole(targetUid);
