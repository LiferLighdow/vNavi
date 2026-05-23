# vNavi Browser

> **Inspired by Min Browser, re-engineered for Android under 100KB.**

vNavi is an ultra-lightweight, high-performance web navigator designed for users who crave speed, privacy, and extreme efficiency. By stripping away all external dependencies and using pure native APIs, vNavi achieves a fully functional browsing experience in a fraction of the size of traditional browsers.

## 🚀 Key Features

- **Extreme Lightweight**: Target binary size under 100KB. No AndroidX, no Material Components, zero bloated libraries.
- **Safari-inspired UI**: A modern, single-hand friendly interface featuring a floating bottom "Pill" address bar.
- **Pill-Tab Switching**: Fluidly switch between multiple tabs by swiping left or right on the address bar.
- **AMOLED Pure Black**: Built-in #000000 theme for maximum battery saving and visual comfort on OLED screens.
- **Privacy & Security**:
    - **Native Ad Block**: Built-in domain-level interception via `shouldInterceptRequest`.
    - **JavaScript Toggle**: Easily enable or disable scripts per session.
- **Customizable Home Page**: A clean, HTML5-based dashboard for your favorite sites with automatic high-res favicon fetching.
- **Advanced Navigation**:
    - **Smart Auto-Hide**: The address bar disappears when scrolling down and reappears on scroll up.
    - **Find in Page**: Native text search with result highlighting and navigation.
    - **Custom User-Agent**: Masquerade as an iPhone, Android Tablet, or Desktop (Chrome/Safari) to bypass site limitations.
- **Efficient Downloader**: Smart filename guessing and direct integration with Android's system Download Manager.

## 🛠 Tech Stack & Engineering

- **Language**: Pure Java (to avoid Kotlin Standard Library overhead).
- **Minimum Support**: Android 5.0 (API 21+).
- **Target SDK**: Android 15/16 (API 35/36) for modern performance.
- **UI Architecture**: A hybrid of native Java View management and high-performance Local HTML5 (Assets) for settings and dashboards.
- **DEX Optimization**: ProGuard/R8 enabled to minify the code to the absolute limit.

## 📱 Gestures & Controls

- **Switch Tabs**: Swipe left/right on the bottom address bar.
- **Search**: Tap the center of the address bar for an immersive search overlay.
- **Tab Management**: Long-press the `❐` icon in the menu to see and close active tabs.
- **Edit Favorites**: Long-press any icon on the home page to edit or delete.
- **Save Images**: Long-press any image on a webpage to download it directly.

## 📥 Installation

Since vNavi is a pure engineering project, you can compile it directly from source using Android Studio. The resulting APK will be surprisingly small, often fitting within a single network packet.

---

*Pure. Light. Fast. This is vNavi.*
