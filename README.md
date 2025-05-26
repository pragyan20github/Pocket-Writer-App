# PocketWriter

**PocketWriter** is a modern Android application paired with a Spring Boot backend that empowers users to design custom article templates and create, edit, and manage rich articles with text and images. The app features a clean, intuitive UI built with Jetpack Compose and supports flexible layouts for both templates and articles.

---

## ✨ Features

- **Article Feed Screen:**  
  Browse all your articles in a scrollable feed with thumbnails and previews.

- **Article Detail Screen:**  
  View the full content of any article, including images and formatted text.

- **Add/Edit Article Screen:**  
  Create or edit articles using your custom templates. Supports both text and image blocks.

- **Template Feed Screen:**  
  See all your saved templates, ready to use for new articles.

- **Template Builder Screen:**  
  Design your own templates by adding text/image blocks. Templates can be edited, deleted, or used for future articles.

- **Edit Template Screen:**  
  Update or delete any saved template.

- **Image Upload Support:**  
  Easily add images to both articles and templates.

- **Modern UI:**  
  Built with Material3 and Jetpack Compose for a smooth, responsive, and visually appealing experience.

- **(Planned) Advanced Layouts & AI:**  
  Upcoming features include drag-and-drop block placement, support for videos, and AI-powered template suggestions.

---

## 🛠️ Technologies Used

- **Frontend:**  
  - Kotlin, Jetpack Compose, Material3  
  - Retrofit (REST API), Coil (image loading), Accompanist Permissions  
  - AndroidX Navigation

- **Backend:**  
  - Java 24, Spring Boot 3.4  
  - PostgreSQL (database)  
  - RESTful API with JPA

---

## 🚀 Local Setup Instructions

### **Prerequisites**

- [Android Studio](https://developer.android.com/studio) (latest recommended)
- [Java 24](https://adoptium.net/) (for backend)
- [Maven](https://maven.apache.org/download.cgi)
- [PostgreSQL](https://www.postgresql.org/) (default user: `postgres`, db: `postgres`)

---

### **1. Backend Setup (Spring Boot)**

1. **Clone the repository**  

git clone https://github.com/yourusername/pocketwriter.git
cd pocketwriter/backend



2. **Configure Database**  
Edit `src/main/resources/application.properties` if needed:

spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=YOUR_PASSWORD



3. **Run the Backend**  
mvn spring-boot:run



The backend will start on [http://localhost:8080](http://localhost:8080).

---

### **2. Android App Setup**

1. **Open the project in Android Studio**  
Open the `app` module.

2. **Install dependencies**  
Android Studio will prompt you to sync Gradle and download dependencies.

3. **Run the app**  
- Select an emulator or device.
- Click **Run**.

**Note:**  
The app connects to the backend at `http://10.0.2.2:8080/` (the default for Android emulator to access your local machine).  
If you run the backend on a real device, update the `BASE_URL` in `ApiService.kt` accordingly.

---

### **3. Environment Variables**

- **Backend:**  
- `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` (set in `application.properties`)
- **Android App:**  
- No environment variables required for local use.

---

## 🎬 Demo Video

> **[Attach your video here after recording]**

**Demo Flow:**
- Launch the app (see splash screen).
- Browse the article feed.
- Create a new template: add a text block for "Headline" , a image block for "Cover Image" , and a text block for "Body".
- Save the template.
- Create an article using the new template, fill in sample content, and upload an image.
- View the article in the feed.

---

## 💡 Additional Notes

- All data is stored locally in your PostgreSQL database.  
  Each user starts with an empty database unless seeded.
- The app is designed for easy extension:  
  - Add new block types, layouts, or AI features with minimal changes.
- Upcoming features: drag-and-drop block placement, video support, and AI template suggestions.

---

## Created by
- Pragyan Srivastava
- Contact: pragyan@iitg.ac.in


