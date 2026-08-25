# DlnaVideoDownload (投屏视频下载器)

[English Version](./README.md)

一个强大的 Android 投屏视频拦截与下载工具。它能将您的手机变为一个 DLNA 接收端（DMR），捕获其他设备投射过来的视频地址并直接下载到本地，支持自动转码为 MP4。

## ✨ 主要功能

*   **DLNA 投屏捕获**：伪装成智能电视设备，拦截各大视频 App（如优酷、爱奇艺、腾讯视频等）的投屏请求。
*   **多格式支持**：支持普通视频 URL（MP4, MKV）和 M3U8 流媒体。
*   **自动转码**：集成了 FFmpeg，下载完成后的 M3U8 会自动合并并转码为通用的 MP4 格式。
*   **下载历史管理**：内置完善的历史记录列表，记录视频大小、下载时间，并提供视频封面预览。
*   **H5 播放器**：内置基于 HTML5 的简易视频播放器，无需跳转第三方应用即可预览。
*   **一键导出**：支持将下载好的视频从应用私有目录一键保存到系统的“下载”公共目录，方便在相册中查看。

## 🚀 核心适配

*   **Android 16 (API 37) 兼容**：全面适配最新的 Android 系统政策，包括前台服务类型声明和精准权限管理。
*   **Material 3 UI**：遵循 Google 最新的设计规范，支持**深色模式**。
*   **16KB 内存页对齐**：优化了原生库加载，支持下一代 Android 硬件特性。
*   **沉浸式体验**：全站适配沉浸式状态栏，提供极致的视觉观感。
*   **自适应图标**：全新的自适应桌面图标，并支持 Android 13+ 的系统主题配色（Themed Icon）。

## 🛠️ 技术栈

*   **Cling**: 高性能 DLNA/UPnP 协议库。
*   **FFmpeg-kit**: 视频处理与格式转换。
*   **Glide**: 极速图片加载（用于生成视频封面）。
*   **FileDownloader**: 多线程断点续传下载。
*   **M3U8Downloader**: 专门针对 M3U8 切片的下载引擎。

## 📝 使用说明

1.  启动 App，并确保手机与投屏发送方（如另一台装有视频 App 的手机）在同一个 Wi-Fi 下。
2.  授予必要的**位置权限**（用于发现网络设备）和**通知权限**。
3.  在视频 App 中点击“投屏”按钮，选择名为 `Dlna Video Download` 的设备。
4.  App 会自动捕获并开始下载。下载完成后，可以在“下载历史”中查看或导出。

## 🙏 鸣谢

感谢以下开源项目的支持：
*   [Cling](http://4thline.org/projects/cling/)
*   [FFmpeg-kit](https://github.com/arthenica/ffmpeg-kit)
*   [Glide](https://github.com/bumptech/glide)
*   [FileDownloader](https://github.com/lingochamp/FileDownloader)
*   [M3U8Downloader](https://github.com/Jay-Goo/M3U8Downloader)

---
*本项目仅供学习和研究使用，请勿用于非法用途。*
