

# 🍲 Foodware - A Modern Android Recipe Tracker

Foodware is a sleek and modern Android application designed to help you organize your favorite recipes, discover new ones, and manage your kitchen pantry. Built with Material Design 3 and Firebase, it provides a seamless and interactive user experience.

## ✨ Features

### Core Features
* **Splash Screen:** A branded launch screen for a professional feel.
* **User Onboarding:** A multi-step questionnaire to personalize the user experience (goals, diet, allergies, etc.).
* **Secure Authentication:** Sign up and log in using Email/Password, Google, and Facebook.
* **Dynamic Dashboard:** An interactive main screen with a collapsing toolbar and quick-action buttons.
* **Recipe Management:**
    * Add, view, edit, and delete personal recipes.
    * Include details like ingredients, step-by-step instructions, cooking time, and servings.
* **Organization:**
    * Mark recipes as **Favorites** for quick access.
    * Organize recipes by **Categories**.

### Future Features
* 🗓️ **Meal Planner:** Plan your meals for the week on a calendar.
* 🛒 **Shopping List Generator:** Automatically create a shopping list from your meal plan.
* 🍳 **Cooking Mode:** A distraction-free UI to follow recipes while cooking.
* ☁️ **Cloud Sync:** Sync recipes and user data across multiple devices.
* 🌐 **Import from Web:** Grab recipes from popular websites and save them in the app.

## 🛠️ Technologies Used

* **Language:** Java
* **Backend & Authentication:** Firebase (Authentication, Cloud Firestore)
* **UI Components:** Material Design 3, `CoordinatorLayout`, `CollapsingToolbarLayout`, `ViewPager2`, `RecyclerView`
* **Build Tool:** Gradle

## 🚀 Getting Started & Installation

Follow these instructions to get a copy of the project up and running on your local machine.

> ## ⚠️ **Security Warning** ⚠️
>
> The setup instructions below require you to place secret API keys directly into project files that are committed to Git (`google-services.json` and `strings.xml`). **This is not a recommended practice as it will expose your secret keys in your Git repository.**
>
> Automated security scanners like GitGuardian will flag this as a high-severity vulnerability. For a production app, you should use a secure method like storing keys in `local.properties` and injecting them at build time.

### Step-by-Step Setup

**1. Clone the Repository**
```bash
git clone [https://github.com/YOUR_USERNAME/Foodware.git](https://github.com/YOUR_USERNAME/Foodware.git)
cd Foodware
````

**2. Firebase Setup**

  * Go to the [Firebase Console](https://console.firebase.google.com/) and create your own Firebase project.
  * Inside your project, add an Android app with the package name `com.example.recipietracker`.
  * Provide your **Debug SHA-1 certificate fingerprint** during the setup.
  * Download the generated **`google-services.json`** file and place it in the **`app/`** directory of your project, replacing the existing placeholder if any.

**3. Facebook API Keys Setup**

  * Go to your app on the [Meta for Developers](https://developers.facebook.com) dashboard to get your **App ID** and **Client Token**.

  * Open the `app/src/main/res/values/strings.xml` file.

  * Find the following lines and replace the placeholder text with your actual keys:

    ```xml
    <string name="facebook_app_id">YOUR_FACEBOOK_APP_ID_HERE</string>
    <string name="facebook_client_token">YOUR_FACEBOOK_CLIENT_TOKEN_HERE</string>
    <string name="fb_login_protocol_scheme">fbYOUR_FACEBOOK_APP_ID_HERE</string>
    ```

**4. Build and Run**

  * Open the project in Android Studio.
  * Let Gradle sync and build the project.
  * Run the app on an emulator or a physical device.

The project should now build and run successfully.

```
```
