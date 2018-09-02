import RPi.GPIO as GPIO

import time

class Sw420(object):
    def __init__(self, pin):
        GPIO.setmode(GPIO.BCM)
        self.pin = pin
        GPIO.setup(self.pin, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

        GPIO.add_event_detect(self.pin, GPIO.RISING, callback=self.callback, bouncetime=1)
        self.count = 0

    def callback(self, pin):
        self.count += 1

    def vibrate_sensor(self, callback):

        try:
            while True:
                time.sleep(1)

                if self.count >= 10:
                    print("Detect vibrate")
                    callback()

                else:
                    print("Not detect vibrate")

                sensor.count = 0

        except KeyboardInterrupt:
            GPIO.cleanup()

        # while True:
        #     result = GPIO.input(23)
        #     if result == 1:
        #         print("detect vibrate.")
        #         time.sleep(0.05)
        #
        #     else:
        #         print("not detect vibrate.")
        #         time.sleep(0.05)