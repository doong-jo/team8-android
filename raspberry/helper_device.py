from bluetooth import *
import time
from sys import exit
import threading
from operator import eq
try:
    from PIL import Image
except ImportError:
    exit("This script requires this pillow module\nInstall with : sudo pip install pillow")

import unicornhathd

WIDTH, HEIGHT   = unicornhathd.get_shape()

TYPE_SPRITE     = 0
TYPE_BLINK      = 1
TYPE_EFFECT     = 2

SIZE_READ_BYTE  = 6

DEFAULT_SPEED   = 0.5
DEFAULT_BRIGHT  = 0.5
DEFAULT_ROTATION = 90

g_curImgName      = "noname"
g_curSpeed        = DEFAULT_SPEED
g_curType         = TYPE_SPRITE

unicornhathd.rotation(DEFAULT_ROTATION)
unicornhathd.brightness(DEFAULT_BRIGHT)

g_Images = {
    1: Image.open('lofi.png'),
    6: Image.open('movingArrowLeft_blink.png'),
    7: Image.open('movingArrowRight_blink.png'),
    8: Image.open('emergency.png'),
}

def blinkLED():
    unicornhathd.show()
    time.sleep(0.5)
    unicornhathd.off()
    time.sleep(0.3)

def showLED(imagename, targetImage, type):
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
                if eq(type, TYPE_SPRITE):
                    unicornhathd.show()
                    time.sleep(g_curSpeed)
                elif eq(type, TYPE_BLINK):
                    blinkLED()

def controlLED():
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

def receiveMsg():
    while True:
        uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

        server_sock=BluetoothSocket( RFCOMM )
        server_sock.bind(('',PORT_ANY))
        server_sock.listen(1)

        port = server_sock.getsockname()[1]

        advertise_service( server_sock, "BtLED",
                service_id = uuid,
                service_classes = [ uuid, SERIAL_PORT_CLASS ],
                profiles = [ SERIAL_PORT_PROFILE ] )

        print("Waiting for connection : channel %d" % port)
        client_sock, client_info = server_sock.accept()
        print("Accepted connection from ", client_info)

        while True:
            try:
                print("Wating for recv")
                data = client_sock.recv(SIZE_READ_BYTE)
                print("data : %s", data);

                splitData = data.split('-')

                try:
                    signalData = int(splitData[0])
                    valueData = int(splitData[1])
                    optionalData = int(splitData[2])
                except KeyError:
                    pass

                # DEFINE SIGNAL_INDEX
                # LED           0
                # SPEED         1
                # BRIGHTNESS    2
                # CLOSE         -1

                # - : splite word

                # LED
                # SIGNAL_INDEX-LED_INDEX-TYPE(SPRITE, BLINK, EFFECT)#
                # LED example : 0-01-0 (LED-01st-LED-SPRITE => LED 1 sprite)

                # SPEED
                # SIGNAL_INDEX-SPEED#
                # SPEED example : 1-05-0 (0~10) (SPEED-5 => frame speed = interval 0.5 sec)

                # BRIGHTNESS
                # SIGNAL_INDEX-BRIGHTNESS#
                # BRIGHTNESS example : 2-05-0 (0~10) (BRIGHTNESS-5 => brightness level 5)

                global g_curImgName
                global g_curSpeed
                global g_curType

                if signalData == 0:
                    g_curImgName = valueData
                    print("g_curImgName ", g_curImgName)
                    # TypeData
                    g_curType = optionalData

                elif signalData == 1:
                    g_curSpeed = valueData * 0.1 + optionalData * 0.01

                elif signalData == 2:
                    unicornhathd.brightness(valueData * 0.1 + optionalData * 0.01)

            except IOError:
                print("disconnected")
                client_sock.close()
                server_sock.close()
                unicornhathd.off()
                print("all done (disconnected)")
                break

            except KeyboardInterrupt:
                print("disconnected")
                unicornhathd.off()
                print("receiveMsg KeyboardInterrupt")
                break

            # time.sleep(0.5)


def main():
    try:
        t1 = threading.Thread(target=receiveMsg, args=())
        t1.daemon = True
        t1.start()

        t2 = threading.Thread(target=controlLED, args=())
        t2.daemon = True
        t2.start()

    except KeyboardInterrupt:
        print("disconnected")
        unicornhathd.off()
        print("main KeyboardInterrupt")

    while True:
        try:
            pass
        except KeyboardInterrupt:
            print("disconnected")
            unicornhathd.off()
            print("main KeyboardInterrupt")


if __name__ == '__main__':
    main()