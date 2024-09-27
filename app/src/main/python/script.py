from pytubefix import YouTube, Playlist, Channel
from pytubefix.cli import on_progress
import os
import re

class YouTubeDownloader:
    def __init__(self, url):
        self.url = url
        self.yt = YouTube(url, on_progress_callback=on_progress)

    def download_highest_resolution(self):
        try:
            ys = self.yt.streams.get_highest_resolution()
            safe_title = self.sanitize_filename(self.yt.title)
            download_path = os.path.join("/storage/emulated/0/Download/", f"{safe_title}.mp4")
            ys.download(output_path=download_path)
            return f"Downloaded: {download_path}"
        except Exception as e:
            return f"Error: {str(e)}"

    def download_audio(self, mp3=True):
        try:
            ys = self.yt.streams.get_audio_only()
            safe_title = self.sanitize_filename(self.yt.title)
            download_path = os.path.join("/storage/emulated/0/Download/", f"{safe_title}.mp3")
            ys.download(output_path=download_path, mp3=mp3)
            return f"Downloaded: {download_path}"
        except Exception as e:
            return f"Error: {str(e)}"

    def download_subtitles(self, language_code='en', filename='captions.txt'):
        try:
            caption = self.yt.captions.get_by_language_code(language_code)
            caption.save_captions(filename)
            return f"Subtitles saved as: {filename}"
        except Exception as e:
            return f"Error: {str(e)}"

    def sanitize_filename(self, title):
        return re.sub(r'[<>:"/\\|?*]', '_', title)

class PlaylistDownloader:
    def __init__(self, url):
        self.playlist = Playlist(url)

    def download_all_videos_audio(self, mp3=True):
        results = []
        for video in self.playlist.videos:
            yt_downloader = YouTubeDownloader(video.url)
            result = yt_downloader.download_audio(mp3=mp3)
            results.append(result)
        return results

class ChannelDownloader:
    def __init__(self, url):
        self.channel = Channel(url)

    def get_channel_name(self):
        return self.channel.channel_name

    def download_all_videos(self):
        results = []
        for video in self.channel.videos:
            yt_downloader = YouTubeDownloader(video.url)
            result = yt_downloader.download_highest_resolution()
            results.append(result)
        return results

# Callable functions
def download_video(url):
    downloader = YouTubeDownloader(url)
    return downloader.download_highest_resolution()

def download_audio(url, mp3=True):
    downloader = YouTubeDownloader(url)
    return downloader.download_audio(mp3=mp3)

def download_subtitles(url, language_code='en', filename='captions.txt'):
    downloader = YouTubeDownloader(url)
    return downloader.download_subtitles(language_code, filename)

def download_playlist(url, mp3=True):
    downloader = PlaylistDownloader(url)
    return downloader.download_all_videos_audio(mp3=mp3)

def download_channel(url):
    downloader = ChannelDownloader(url)
    results = downloader.download_all_videos()
    return downloader.get_channel_name(), results
