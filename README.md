# SUERTE - Madrid Civic Engagement App

SUERTE is a mobile application designed to empower Madrid's citizens by providing an intuitive platform to report infrastructure issues directly to municipal authorities. 
From potholes to broken streetlights, SUERTE fosters civic engagement and streamlines communication between residents and local government. 

---

## Features

- **User Authentication**: Secure account creation, login, and password recovery with Firebase Authentication.
- **Incident Reporting**: Report infrastructure issues with auto-filled location and date using GPS.
- **Incident Management**: View, manage, and delete personal and community-reported incidents.
- **Municipal Office Directory**: Find the nearest municipal offices sorted by proximity.
- **Accessibility Features**: Light and dark modes with WCAG-compliant color palettes.
- **Real-Time Notifications**: Get updates on the status of your reports via MQTT.

## Technologies Used

- **Android Studio**: Development environment.
- **Firebase**: Authentication and Firestore database for data storage.
- **Google Maps API**: Location-based features and municipal office distance calculations.
- **HiveMQ MQTT**: Real-time user-admin communication.
- **SharedPreferences**: Local storage for settings and temporary data.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/suerte.git
   ```
2. Open the project in Android Studio.
3. Set up Firebase:
   - Add your `google-services.json` file to the `app/` directory.
   - Update the Firebase configuration in the project settings.
4. Run the application on an emulator or physical device.

## Usage

1. **Sign Up or Login** to create a secure account.
2. **Report Issues**: Use the incident reporting form to submit details about an issue.
3. **View Reports**: Check your submitted reports or explore community-reported incidents.
4. **Find Municipal Offices**: Use the built-in directory for office locations nearest to you.
5. **Switch Modes**: Enable light or dark mode based on your preferences.

## License
This project is licensed under the GPL-3.0 License. See the `LICENSE` file for details.
