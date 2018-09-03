import unicornhathd

from sys import exit

try:
    from PIL import Image
except ImportError:
    exit("This script requires this pillow module\nInstall with : sudo pip install pillow")

import threading
import time
from operator import eq

# -------------------- DEFINE LED ------------------ #
LED_TYPE_SPRITE = 0
LED_TYPE_BLINK = 1
LED_TYPE_EFFECT = 2
LED_TYPE_NONE = 3

# Default LED is "0" (res/bird.png)
LED_DEFAULT_NAME = 0
LED_DEFAULT_SPEED = 0.5
LED_DEFAULT_BRIGHT = 0.5
LED_DEFAULT_ROTATION = 90
LED_DEFAULT_TYPE = LED_TYPE_SPRITE

LED_EMERGENCY_LED_IND = 8
LED_LEFT_LED_IND = 6
LED_RIGHT_LED_IND = 7

WIDTH, HEIGHT = unicornhathd.get_shape()

unicornhathd.rotation(LED_DEFAULT_ROTATION)
unicornhathd.brightness(LED_DEFAULT_BRIGHT)

# TODO : Selectable Load Image
g_Images = {
    0: Image.open('res/bird.png'),
    1: Image.open('res/lofi.png'),
    2: Image.open('res/windy.png'),
    3: Image.open('res/snow.png'),
    4: Image.open('res/rain.png'),
    5: Image.open('res/cute.png'),
    6: Image.open('res/movingArrowLeft_blink.png'),
    7: Image.open('res/movingArrowRight_blink.png'),
    8: Image.open('res/emergency.png'),
    9: Image.open('res/mario.png'),
    10: Image.open('res/boy.png'),
}
#######################################################


class UnicornLED(object):

    def __init__(self, state, saveStateCb):
        # self.m_curImageName = LED_DEFAULT_NAME
        # self.m_curSpeed = LED_DEFAULT_SPEED
        # self.m_curType = LED_TYPE_NONE
        # self.m_curBright = LED_DEFAULT_BRIGHT

        for ledState in state['LED_STATE']:
            print ledState

            self.m_curImageName = int(ledState['index'])
            self.m_curType = float(ledState['type'])
            self.m_curSpeed = float(ledState['speed'])
            self.m_curBright = float(ledState['brightness'])
            unicornhathd.brightness(self.m_curBright)

        self.m_saveStateCallback = saveStateCb

    def setAttribute(self, imageName, type, speed, brightness):
        if imageName != -1:
            self.m_curImageName = imageName

        if type != -1:
            self.m_curType = type

        if speed != -1:
            self.m_curSpeed = speed

        if brightness != -1:
            self.m_curBright = brightness
            unicornhathd.brightness(brightness)

        dicData = {}
        dicData['LED_STATE'] = []
        dicData['LED_STATE'].append({
            'index': self.m_curImageName,
            'type': self.m_curType,
            'speed': self.m_curSpeed,
            'brightness': self.m_curBright
        })

        self.m_saveStateCallback(dicData)

    def setEmergency(self):
        self.m_curImageName = LED_EMERGENCY_LED_IND
        self.m_curType = LED_TYPE_BLINK

    def blinkLED(self):
        unicornhathd.show()
        time.sleep(0.5)
        unicornhathd.off()
        time.sleep(0.3)

    def showLED(self, imagename, targetImage, type):
        for o_x in range(int(targetImage.size[0] / WIDTH)):
            for o_y in range(int(targetImage.size[1] / HEIGHT)):
                valid = False

                # if signal not equal, Interrupt LED!
                if not eq(self.m_curImageName, imagename):
                    break

                for x in range(WIDTH):
                    for y in range(HEIGHT):
                        pixel = targetImage.getpixel(((o_x * WIDTH) + y, (o_y * HEIGHT) + x))
                        r, g, b = int(pixel[0]), int(pixel[1]), int(pixel[2])
                        if r or g or b:
                            valid = True
                        unicornhathd.set_pixel(x, y, r, g, b)
                if valid:
                    if eq(type, LED_TYPE_SPRITE):
                        unicornhathd.show()
                        time.sleep(self.m_curSpeed)

                    elif eq(type, LED_TYPE_BLINK):
                        self.blinkLED()

                    elif eq(type, LED_TYPE_NONE):
                        unicornhathd.show()

    def controlLED(self):
        while True:
            try:
                if not eq(self.m_curImageName, "noname"):
                    try:
                        print("show!")
                        targetImage = g_Images[self.m_curImageName]
                        self.showLED(self.m_curImageName, targetImage, self.m_curType)

                    except KeyError:
                        self.m_curImageName = "noname"
                        print("not exist image")

            except KeyboardInterrupt:
                print("disconnected")
                unicornhathd.off()
                print("receiveMsg KeyboardInterrupt")
                break

    def run(self):
        t1 = threading.Thread(target=self.controlLED)
        t1.daemon = True
        t1.start()

# END

