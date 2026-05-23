# vNavi Browser

<p align="center">
  <img src="logo.svg" width="100" height="100" alt="vNavi Logo">
</p>

<p align="center">
  <strong>Inspired by Min Browser, re-engineered for Android under 100KB.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Pure%20Java-orange.svg">
  <img src="https://img.shields.io/badge/Android-5.0%20--%2017-green.svg">
  <img src="https://img.shields.io/badge/Size-%3C%20100KB-blue.svg">
  <img src="https://img.shields.io/badge/License-MIT-lightgrey.svg">
  <img src="https://img.shields.io/badge/Dependencies-Zero-red.svg">
</p>

---

vNavi is an ultra-lightweight, high-performance web navigator designed for users who crave speed, privacy, and extreme efficiency. By stripping away all external dependencies and utilizing pure native APIs, vNavi achieves a premium browsing experience in a binary size that fits within a single network packet.

## 🚀 Key Features

- **Extreme Lightweight**: Target binary size < 100KB. Zero AndroidX, Zero Material Components, Zero bloated libraries.
- **Safari-inspired UI**: A modern, single-hand friendly interface featuring a floating bottom "Pill" address bar.
- **Pill-Tab Switching**: Fluidly switch between multiple tabs by swiping left or right on the address bar.
- **AMOLED Pure Black**: Built-in `#000000` theme for maximum battery saving and visual comfort on OLED screens.
- **Privacy & Security**:
    - **Native Ad Block**: Built-in domain-level interception via `shouldInterceptRequest`.
    - **JavaScript Toggle**: Easily enable or disable scripts per session.
- **Advanced Navigation**:
    - **Smart Auto-Hide**: The address bar disappears when scrolling down and reappears on scroll up.
    - **Find in Page**: Native text search with result highlighting and navigation.
    - **Custom User-Agent**: Masquerade as an iPhone, Tablet, or Desktop to bypass site limitations.

## 🎨 Visual Identity & Iconography

vNavi's visual language is built on the same "zero-waste" philosophy as its code. 

### Modern Adaptive Icon
The application icon is not just a bitmap; it's a precision-engineered **Android Vector Drawable**. 
- **Core Design**: A minimalist browser frame combined with a compass, representing navigation and speed.
- **Implementation**: Hand-coded XML in `ic_launcher_foreground_color.xml`, ensuring the icon remains sharp at any resolution without adding to the APK size.
- **Aesthetic**: Uses high-contrast Slate and Slate-Dark tones (`#0F172A`, `#1E293B`) to complement the AMOLED-focused interface.

## 🛠 Engineering Philosophy

### The "Under 100KB" Challenge
Most modern Android apps exceed 10MB just for the "Hello World" boilerplate. vNavi takes the opposite path:
- **Pure Java**: Written in Java to avoid the ~1MB overhead of the Kotlin Standard Library.
- **Zero-Dependency**: No `appcompat` means no hundreds of unused XML resources and method counts.
- **Hybrid UI**: We use a unique architecture where the complex UI (Settings, Home Dashboard) is rendered via high-performance **Local HTML5 (Assets)**, keeping the DEX bytecode minimal.
- **DEX Optimization**: Aggressive R8/ProGuard rules to strip every unused byte.

## 📱 Gestures & Controls

| Action | Control |
| :--- | :--- |
| **Switch Tabs** | Swipe Left/Right on the bottom pill bar |
| **Search** | Tap the center of the pill bar |
| **Tab Management** | Long-press the `❐` icon in the menu |
| **Manage Favorites** | Long-press any icon on the Home page |
| **Save Images** | Long-press any image on a webpage |
| **Reload Page** | Select `↻` in menu or Pull down from top |

## 🏗 Build Requirements

- **Android Studio**: Ladybug or newer.
- **Build System**: Gradle 8.0+.
- **Min SDK**: API 21 (Android 5.0).
- **Target SDK**: API 37 (Android 17).

## 📥 Installation

Compile directly from source using Android Studio. The resulting APK is optimized for direct installation and side-loading.

```bash
./gradlew assembleRelease
```

---

*Pure. Light. Fast. This is vNavi.*

Developed with ❤️ by **LiferLighdow**
