import unicornhathd

import time
from operator import eq

# -------------------- DEFINE LED ------------------ #
LED_TYPE_SPRITE = 0
LED_TYPE_BLINK = 1
LED_TYPE_EFFECT = 2

LED_DEFAULT_SPEED = 0.5
LED_DEFAULT_BRIGHT = 0.5
LED_DEFAULT_ROTATION = 90
LED_DEFAULT_TYPE = LED_TYPE_SPRITE

LED_EMERGENCY_LED_IND = 8
LED_LEFT_LED_IND = 6
LED_RIGHT_LED_IND = 7

g_curImgName = "noname"
g_curSpeed = DEFAULT_SPEED
g_curType = DEFAULT_TYPE
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

WIDTH, HEIGHT = unicornhathd.get_shape()

unicornhathd.rotation(DEFAULT_ROTATION)
unicornhathd.brightness(DEFAULT_BRIGHT)
#######################################################


class UnicornLED(object):

    def __init__(self):
        self.m_curImageName = LED_DEFAULT

    def blinkLED(self):
        unicornhathd.show()
        time.sleep(0.5)
        unicornhathd.off()
        time.sleep(0.3)

    def showLED(self, imagename, targetImage, type):
        global g_curImgName
        global g_curSpeed

        for o_x in range(int(targetImage.size[0] / WIDTH)):
            for o_y in range(int(targetImage.size[1] / HEIGHT)):
                valid = False

                # if signal not equal, Interrupt LED!
                if not eq(g_curImgName, imagename):
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
                        time.sleep(g_curSpeed)
                    elif eq(type, LED_TYPE_BLINK):
                        blinkLED()

    def controlLED(self):
        while True:
            try:
                global g_curImgName
                global g_curSpeed
                global g_curType

                imagename = g_curImgName
                speed = g_curSpeed
                type = g_curType

                if not eq(imagename, "noname"):
                    print("current imagename : %d", imagename)
                    print("current speed : %f", speed)
                    print("current type : %d", type)

                    try:
                        print("try show!")
                        targetImage = g_Images[imagename]
                        showLED(imagename, targetImage, type)
                    except KeyError:
                        g_curImgName = "noname"
                        print("not exist image")

                # else:
                #     print("not set image")

            except KeyboardInterrupt:
                print("disconnected")
                unicornhathd.off()
                print("receiveMsg KeyboardInterrupt")
                break
            # time.sleep(0.5)