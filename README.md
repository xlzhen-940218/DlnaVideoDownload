# DlnaVideoDownload

[中文版 (Chinese Version)](./README_CN.md)

A powerful Android tool for intercepting and downloading screencast videos. It transforms your phone into a DLNA renderer (DMR), capturing video URLs cast from other devices and downloading them directly to your local storage, with automatic conversion to MP4.

## ✨ Key Features

*   **DLNA Capture**: Acts as a smart TV device to intercept screencast requests from major video apps (e.g., YouTube, Netflix-like local apps, etc.).
*   **Multi-format Support**: Supports standard video URLs (MP4, MKV) and M3U8 streaming.
*   **Auto Transcoding**: Integrated with FFmpeg-kit to automatically merge and transcode M3U8 downloads into universal MP4 files.
*   **Download History**: Built-in history manager displaying video size, download date, and HD thumbnail previews.
*   **H5 Player**: Embedded HTML5 video player for instant preview without leaving the app.
*   **One-tap Export**: Save downloaded videos from private storage to the system's "Download" directory for easy access in the Gallery.

## 🚀 Modern Android Support

*   **Android 16 (API 37) Ready**: Fully compliant with the latest Android policies, including Foreground Service type declarations and granular permission management.
*   **Material 3 UI**: Modern design following Google's latest guidelines, featuring a beautiful **Dark Mode**.
*   **16KB Page Alignment**: Optimized native library loading for next-gen Android hardware.
*   **Immersive Experience**: Full edge-to-edge support for a seamless visual experience.
*   **Adaptive Icon**: Supports modern adaptive icons and Android 13+ **Themed Icons**.

## 🛠️ Tech Stack

*   **Cling**: High-performance DLNA/UPnP protocol library.
*   **FFmpeg-kit**: Professional video processing and format conversion.
*   **Glide**: Fast image loading for video thumbnails.
*   **FileDownloader**: Multi-threaded download engine with breakpoint resume.
*   **M3U8Downloader**: Specialized engine for M3U8 segment downloading.

## 📝 Usage Guide

1.  Launch the app and ensure your phone is on the same Wi-Fi network as the casting device.
2.  Grant necessary **Location Permissions** (required for network discovery) and **Notification Permissions**.
3.  Tap the "Cast" button in your video app and select the device named `Dlna Video Download`.
4.  The app will automatically capture the link and start downloading. View or export your files in "Download History".

## 🙏 Acknowledgements

Special thanks to these open-source projects:
*   [Cling](http://4thline.org/projects/cling/)
*   [FFmpeg-kit](https://github.com/arthenica/ffmpeg-kit)
*   [Glide](https://github.com/bumptech/glide)
*   [FileDownloader](https://github.com/lingochamp/FileDownloader)
*   [M3U8Downloader](https://github.com/Jay-Goo/M3U8Downloader)

---
*This project is for educational and research purposes only. Please do not use it for illegal activities.*
