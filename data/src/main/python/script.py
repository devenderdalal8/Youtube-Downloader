import json
import os
import re
import requests
from pytubefix import YouTube, Playlist, Channel
from pytubefix import Search

# YouTubeDownloader for video
class YouTubeDownloader:
    def __init__(self, url):
        self.url = url
        self.yt = YouTube(url)

    def get_available_resolutions(self):
        streams = self.yt.streams.filter()
        available_resolutions = {stream.resolution for stream in streams if
                                 stream.resolution is not None}
        return sorted(list(available_resolutions))

    def video_details(self):
        try:
            details = Video(
                title=self.yt.title,
                description=self.yt.description,
                thumbnail_url=self.yt.thumbnail_url,
                base_url=self.url,
                video_id=self.yt.video_id,
                video_url=self.yt.streams.get_highest_resolution().url,
                duration=self.yt.length,
                views=self.yt.views,
                likes=self.yt.likes,
                resolution=self.get_available_resolutions(),
                upload_date=str(
                    self.yt.publish_date.isoformat() if self.yt.publish_date else None),
                channel_url=self.yt.channel_url,
                channel_id=self.yt.channel_id
            )
            return details.to_json()
        except Exception as e:
            return json.dumps({'error': str(e)}, indent=4)

    def get_video_url_by_resolution(self, target_resolution):
        try:
            streams = self.yt.streams.filter()
            available_resolutions = {stream.resolution for stream in streams}
            selected_stream = next(
                (stream for stream in streams if stream.resolution == target_resolution), None)

            if selected_stream:
                return selected_stream.url
            else:
                return f"No stream found for resolution: {target_resolution}"

        except Exception as e:
            return f"Error: {str(e)}"

    def download_with_progress(self, url, output_path, progress_callback=None):
        response = requests.get(url, stream=True)
        total_size = int(response.headers.get('content-length', 0))
        with open(output_path, 'wb') as file:
            for data in response.iter_content(chunk_size=1024):
                file.write(data)
                if progress_callback:
                    progress_callback(file.tell(), total_size)

    def download_highest_resolution(self):
        try:
            ys = self.yt.streams.get_highest_resolution()
            safe_title = self.sanitize_filename(self.yt.title)
            download_path = os.path.join("/storage/emulated/0/Download/", f"{safe_title}.mp4")
            self.download_with_progress(ys.url, download_path)
            return f"Downloaded: {download_path}"
        except Exception as e:
            return f"Error: {str(e)}"

    def download_audio(self, mp3=True):
        try:
            ys = self.yt.streams.get_audio_only()
            safe_title = self.sanitize_filename(self.yt.title)
            download_path = os.path.join("/storage/emulated/0/Download/", f"{safe_title}.mp3")
            self.download_with_progress(ys.url, download_path)
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


# PlaylistDownloader for playlist
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


# ChannelDownloader for channel video download
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


class Video:
    def __init__(self, title="", description="", thumbnail_url="", base_url="", video_url="",
                 video_id="",
                 duration="",
                 views="", likes=None,
                 resolution=None, resolution_list=None, upload_date=None, channel_url="",
                 channel_id=""):
        self.title = title
        self.description = description
        self.thumbnail_url = thumbnail_url
        self.video_url = video_url
        self.video_id = video_id
        self.base_url = base_url
        self.duration = duration
        self.views = views
        self.likes = likes  # Can be None
        self.resolution = resolution if resolution is not None else []
        self.resolution_list = resolution_list if resolution_list is not None else {}
        self.upload_date = upload_date if upload_date is not None else datetime.now().isoformat()
        self.channel_url = channel_url
        self.channel_id = channel_id

    def to_json(self):
        return json.dumps(self.__dict__)

class SearchVideo:
    def __init__(self, title="", thumbnail_url="", base_url="", video_id="", video_url="",
                 duration="", views="", upload_date=None):
        self.title = title
        self.thumbnail_url = thumbnail_url
        self.base_url = base_url
        self.video_id = video_id
        self.video_url = video_url
        self.duration = duration
        self.views = views
        self.upload_date = upload_date

    def to_json(self):
        return json.dumps(self.__dict__, indent=4)

class YouTubeVideoFetcher:
    def __init__(self, query):
        self.query = query

    def search_videos(self, max_results=10):
        try:
            search = Search(self.query)
            results = search.videos
            videos = []
            for result in results[:max_results]:
                video = SearchVideo(
                    title=result.title,
                    thumbnail_url=result.thumbnail_url,
                    video_id=result.video_id,
                    video_url=result.watch_url,
                    duration=result.length,
                    views=result.views,
                    upload_date=str(result.publish_date.isoformat()) if result.publish_date else None  # Handle publish date
                )
                videos.append(video.__dict__)

            return json.dumps({
                "videos": videos,
                "total": len(results)
            }, indent=4)

        except Exception as e:
            return json.dumps({'error': str(e)}, indent=4)


# Callable functions
def download_video(url):
    downloader = YouTubeDownloader(url)
    return downloader.download_highest_resolution()


def video_details(url):
    downloader = YouTubeDownloader(url)
    return downloader.video_details()


def video_resolution(url, resolution):
    downloader = YouTubeDownloader(url)
    return downloader.get_video_url_by_resolution(resolution)

def download_audio(url, mp3=True):
    downloader = YouTubeDownloader(url)
    return downloader.download_audio(mp3=mp3)

def download_subtitles(url, language_code='en', filename='captions.txt'):
    downloader = YouTubeDownloader(url)
    return downloader.download_subtitles(language_code, filename)

def download_playlist(url, mp3=True):
    downloader = PlaylistDownloader(url)
    return downloader.download_all_videos_audio(mp3=mp3)

def search_video(query):
    url_details = YouTubeVideoFetcher(query)
    return url_details.search_videos(max_results=5)

def download_channel(url):
    downloader = ChannelDownloader(url)
    results = downloader.download_all_videos()
    return downloader.get_channel_name(), results
