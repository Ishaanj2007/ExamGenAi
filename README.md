# ExamGen AI - Automated Question Paper Generator

ExamGen AI is a modern, high-performance Android application built using **Kotlin**, **Jetpack Compose**, and **Google Gemini AI**. It allows educators, teachers, and students to automatically generate customizable, professional, and print-ready examination question papers from entered study passages or topics.

Made with ❤️ by **Ishaan**
- **Instagram**: [@ishaanj_19](https://instagram.com/ishaanj_19)
- **GitHub**: [ishaanj2007](https://github.com/ishaanj2007)
- **WhatsApp Support**: `ishaan_jadhav`

---

## 🌟 Key Features

- **Gemini-Powered Generation**: Instantly build balanced exams (MCQs, Short Answer, Long Answer, or mixed types) with custom difficulty levels.
- **Model Selector & Dashboard Status Chip**: Select your preferred Google Gemini model (Gemini 3.5 Flash, Gemini 1.5 Pro, etc.) in Settings. The chosen model dynamically displays as a live status badge directly on the home dashboard and within the generator card!
- **Offline-First Persistence**: Powered by a robust local **Room SQLite Database** to securely save, search, and manage your generated history.
- **Export to PDF**: Generate high-fidelity, beautifully formatted, print-ready PDF exam papers using native PDFBox rendering.
- **Responsive Bento Grid UI**: Elegant, Material 3-compliant grid layout adapting beautifully to any mobile device, aspect ratio, or tablet orientation.
- **Adaptive App Icon**: A custom-designed adaptive launcher icon featuring a clean minimalist graduation cap combined with glowing gold AI sparkles.
- **Clickable Developer Logo**: An interactive branding logo in the Settings screen that takes you directly to Ishaan's GitHub profile.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: 100% Jetpack Compose with Material Design 3 (M3).
- **Language**: Kotlin.
- **Local Database**: Room persistence library.
- **PDF Generation**: PDFBox (Android port) for precise vector document layout.
- **AI Integration**: Google Gemini API via server-side secure endpoints.
- **Design Layout**: Adaptive/Responsive designs incorporating full Edge-to-Edge support (`enableEdgeToEdge()` & standard `WindowInsets` handling).

---

## 🚀 How to Build & Run locally

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/ishaanj2007/ExamGenAI.git
   cd ExamGenAI
   ```

2. **Open in Android Studio**:
   - File -> Open -> Select the project root folder.
   - Wait for Gradle to download and sync all dependencies.

3. **Set Up API Keys**:
   - Create a `.env` file or enter your Google Gemini API Key in the AI Studio Secrets panel.

4. **Run the App**:
   - Connect your Android device or start an emulator.
   - Click the **Run** (green play) button in Android Studio.

---

## 📦 How to Download/Release directly on GitHub

To build a release-ready APK or AAB for publishing on GitHub Releases:

1. In Android Studio, go to the top menu: **Build > Build Bundle(s) / APK(s) > Build APK(s)** (or **Generate Signed Bundle / APK...** if you want to sign it with a custom release keystore).
2. Once compilation finishes, locate the generated APK in your project directory at:
   `app/build/outputs/apk/debug/app-debug.apk` (or `release/app-release.apk`).
3. Create a **New Release** on your GitHub repository (`ishaanj2007/ExamGenAI`):
   - Go to your repository on GitHub.
   - Click **Releases** on the right side.
   - Click **Draft a new release**.
   - Set the Tag version (e.g., `v1.0.0`) and Release Title.
   - Drag and drop your compiled **`app-debug.apk`** or **`app-release.apk`** file into the binaries upload section.
   - Click **Publish release**! Your users can now download the app directly.
