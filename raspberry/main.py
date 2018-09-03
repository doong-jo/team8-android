import time
from subprocess import call

from unicornled import UnicornLED
from vibratesw import vibrateSW
from bluetoothrfcomm import BluetoothRFCOMM
from filemgr import FileManager

# -------------------- DEFINE LED ------------------ #




def main():
    filemanager = FileManager()
    sw = vibrateSW(23)
    led = UnicornLED(filemanager.readState(), filemanager.saveLEDState)
    bluetooth = BluetoothRFCOMM()

    try:
        sw.run(led.setEmergency)
        led.run()
        bluetooth.run(led.setAttribute)

    except KeyboardInterrupt:
        print("main KeyboardInterrupt")

    while True:
        try:
            pass
        except KeyboardInterrupt:
            print("main KeyboardInterrupt")


if __name__ == '__main__':
    main()
