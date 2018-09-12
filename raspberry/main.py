import time
from subprocess import call

from unicornled import UnicornLED
from vibratesw import vibrateSW
from bluetoothrfcomm import BluetoothRFCOMM
from filemgr import FileManager
from gyroscope import Gyroscope

# -------------------- DEFINE LED ------------------ #




def main():
    filemanager = FileManager()
    # sw = vibrateSW(23)
    bluetooth = BluetoothRFCOMM()
    gyroSensor = Gyroscope()
    led = UnicornLED(filemanager.readState(), filemanager.saveLEDState)

    try:
        # sw.run(led.setEmergency, bluetooth.sendMsg)
        led.run()
        bluetooth.run(led.setAttribute, led.getLEDInfo)
        gyroSensor.run(led.inturrptLED, bluetooth.sendMsg)

    except KeyboardInterrupt:
        print("main KeyboardInterrupt")

    while True:
        try:
            pass
        except KeyboardInterrupt:
            print("main KeyboardInterrupt")


if __name__ == '__main__':
    main()