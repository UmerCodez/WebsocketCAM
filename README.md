<div align="center">

# Websocket CAM

<img src="https://github.com/UmerCodez/WebsocketCAM/blob/main/assets/app-icon.png" width="150">

![GitHub License](https://img.shields.io/github/license/UmerCodez/LittleRelay?style=for-the-badge) ![Android Badge](https://img.shields.io/badge/Android-7.0+-34A853?logo=android&logoColor=fff&style=for-the-badge) ![Jetpack Compose Badge](https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=fff&style=for-the-badge) ![Material 3](https://img.shields.io/badge/Material%203-ebe89d?style=for-the-badge&logo=materialdesign&logoColor=white) ![GitHub Release](https://img.shields.io/github/v/release/UmerCodez/WebsocketCAM?include_prereleases&style=for-the-badge)

[<img src="https://github.com/UmerCodez/WebsocketCAM/blob/main/assets/gio-github.png" height="80">](https://github.com/UmerCodez/WebsocketCAM/releases) [<img src="https://github.com/UmerCodez/WebsocketCAM/blob/main/assets/gio-obtainium.png" height="80">](https://github.com/ImranR98/Obtainium)

### Easily access your device’s camera using a WebSocket client API to perform real-time AI and computer vision tasks on a live video stream.

<img src="https://github.com/UmerCodez/WebsocketCAM/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" width="250" heigth="250"> <img src="https://github.com/UmerCodez/WebsocketCAM/blob/main/fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" width="250" heigth="250"> 

</div>

The app broadcasts live camera frames as JPEG images over WebSocket connections, allowing multiple clients to connect concurrently and perform AI or computer vision processing in parallel.

## Displaying live camera stream using Python
A simple Python example using OpenCV and WebSocket libraries to connect to the WebSocket CAM app and display the live camera stream.

```python
import cv2
import numpy as np
import websocket

SERVER_URL = "ws://192.168.18.50:8080" 

def on_message(ws, message):
    # Convert received bytes into numpy array
    np_arr = np.frombuffer(message, np.uint8)
    frame = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    if frame is not None:
        cv2.imshow("Camera Stream", frame)
        if cv2.waitKey(1) & 0xFF == 27:  # ESC key to exit
            ws.close()
            cv2.destroyAllWindows()
    else:
        print("Failed to decode frame")

def on_error(ws, error):
    print("WebSocket error:", error)

def on_close(ws, close_status_code, close_msg):
    print("Connection closed:", close_status_code, close_msg)
    cv2.destroyAllWindows()

def on_open(ws):
    print("Connected to Websocket CAM, open camera to see live stream")

if __name__ == "__main__":
    # Ensure OpenCV windows don't freeze the thread
    websocket.enableTrace(False)
    ws = websocket.WebSocketApp(
        SERVER_URL,
        on_open=on_open,
        on_message=on_message,
        on_error=on_error,
        on_close=on_close,
    )

    try:
        ws.run_forever()
    except KeyboardInterrupt:
        print("\n Stopped by user")
        ws.close()
        cv2.destroyAllWindows()
```

Start the WebSocket server in the app, then run the this Python script in multiple terminals to view multiple live streams simultaneously
