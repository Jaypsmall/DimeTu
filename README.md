# 👁️ DIMETU (v1.0.1)  ![Android](https://img.shields.io/badge/Android-181717?style=flat&logo=android&logoColor=38B8D8) ![Kotlin](https://img.shields.io/badge/kotlin-181717?style=flat&logo=kotlin&logoColor=38B8D8) 

**DiMeTu** Apk is a powerful WhatsApp database explorer designed exclusively for rooted Android devices. Built with a clean, modern interface and optimized for speed, it allows developers, researchers, and cybersecurity enthusiasts to inspect WhatsApp's internal SQLite database in real time without exporting backups.

<a href="https://github.com/Jaypsmall/DimeTu/releases/download/root/DimeTu_v1.0.1.apk">
 <img src="https://img.shields.io/badge/DOWNLOAD_DIMETU_v1.0.1_APK-181717?style=flat&logo=android&logoColor=38B8D8" alt="Download Release"> 
</a>
  
---

## 🚀 Key Features

   💬 **WhatsApp Chat Explorer**
   Browse conversations directly from the local WhatsApp database.

   📨 **Live Message Viewer**
   Read messages in chronological order with automatic updates.

   🔄 **Real-Time Database Monitoring**
   Continuously detects newly received messages and refreshes conversations automatically.

   📱 **LID → Phone Number Resolution**
   Automatically converts WhatsApp's internal LID identifiers into real phone numbers using the latest database mapping.

   🗂️ **Direct SQLite Access**
   Communicates directly with `msgstore.db` through native SQLite queries.

   ⚡ **Root Access Ready**
   Optimized for Magisk-powered devices using privileged shell commands for maximum compatibility.

   🎨 **Modern Interface**
   Built with Material Design and Jetpack Compose for a smooth user experience.

---

📸 **Screenshots**

<p align="center" width="100%">
  <img src="https://github.com/user-attachments/assets/3cd92087-6c17-424c-bd1c-b3922e526fb7" width="45%" />
  <img src="https://github.com/user-attachments/assets/04a57512-851e-47d6-b7f0-c2f969529c51" width="45%" />

<p align="center" width="100%">
  <img src="https://github.com/user-attachments/assets/74f060e7-1b5c-4dfc-97ec-cb067fd4dd19" width="45%" />
  <img src="https://github.com/user-attachments/assets/22ad014c-a248-4618-a592-3bb2057ef953" width="45%" />
</p>

---

# 🛠️ Architecture and Technologies

**Platform:** Native Android (Jetpack Compose)

**Language:** 100% Kotlin

**Database:** SQLite

**Architecture:** Repository Pattern + Coroutines + StateFlow

**Root Engine:** Magisk / Superuser Shell

**Optimization:** R8 / ProGuard Ready

---

# ⚠️ Requirements

- Rooted Android Device (Magisk Recommended)
- Android 8.0 (API 26) or higher
- WhatsApp installed on the device
- SQLite available through Termux or bundled binary

---

# 🔍 Database Support

Compatible with modern WhatsApp database structures including:

- `chat`
- `message`
- `jid`
- `jid_map`
- LID mapping
- Real phone number resolution

Designed to support the latest Meta database architecture.

---

# ⚡ Planned Features

- 🔎 Instant message search
- 📂 Media browser
- 📊 Chat statistics
- ⭐ Favorite conversations
- 📤 Conversation export
- 🔔 Real-time notifications
- 🖥️ Floating monitor overlay
- 📈 Database integrity diagnostics

---

# ⚠️ LEGAL NOTICE

This application is intended **solely** for:

- Personal analysis on devices you own.
- Authorized security research.
- Educational purposes.
- Database structure investigation.

Accessing data on devices or accounts without authorization may violate applicable laws and platform terms. The author assumes no responsibility for misuse.

---

# 📄 License and Copyright

Copyright © 2026. All rights reserved.

The source code of this application is the private property of the developer. Unauthorized reproduction, redistribution, reverse engineering, or commercial use without explicit permission is prohibited.

---

# 👨‍💻 Developed by

**Jaypsmall**

Android Developer • Root Research • SQLite Explorer • Cybersecurity Enthusiast

*"Exploring what runs beneath the surface."*
