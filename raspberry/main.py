import time
from sys import exit
import threading
from operator import eq
from subprocess import call

from bluetooth import *


try:
    from PIL import Image
except ImportError:
    exit("This script requires this pillow module\nInstall with : sudo pip install pillow")

# ----------- DEFINE BLUETOOTH ATTRIBUTE ----------- #
BT_SIZE_READ_BYTE = 6


######################################################

# -------------- DEFINE VIBRATE SENSOR ------------- #


######################################################

# -------------------- DEFINE LED ------------------ #

class UnicornLED(object):
    def __init__(self):
        pass

    def testFunc(self):
        while True:
            print("test func!")
            time.sleep(1)

    def run(self):
        t1 = threading.Thread(target=self.testFunc)
        t1.daemon = True
        t1.start()

######################################################




def receiveMsg():
    while True:
        uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

        server_sock = BluetoothSocket(RFCOMM)
        server_sock.bind(('', PORT_ANY))
        server_sock.listen(1)

        port = server_sock.getsockname()[1]

        advertise_service(server_sock, "BtLED",
                          service_id=uuid,
                          service_classes=[uuid, SERIAL_PORT_CLASS],
                          profiles=[SERIAL_PORT_PROFILE])

        print("Waiting for connection : channel %d" % port)
        client_sock, client_info = server_sock.accept()
        print("Accepted connection from ", client_info)

        while True:
            try:
                print("Wating for recv")
                data = client_sock.recv(BT_SIZE_READ_BYTE)
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
    led = UnicornLED()

    try:
        t1 = threading.Thread(target=receiveMsg, args=())
        t1.daemon = True
        t1.start()

        t2 = threading.Thread(target=controlLED, args=())
        t2.daemon = True
        t2.start()

        led.run()
        # t3 = threading.Thread(target=led.testFunc, args=())
        # t3.daemon = True
        # t3.start()

        # t3 = threading.Thread(target=vibrateSensor, args=())
        # t3.daemon = True
        # t3.start()

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
