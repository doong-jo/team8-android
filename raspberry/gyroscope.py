from mpu6050 import mpu6050



class gyroscope(object):

    def __init__(self, pin):
        self.sensor = mpu6050(0x68)
        pass

    def gyroscope_sensor(self):
        self.gyroscope_sensor().
        pass

    def run(self, ledcb, bluetoothcb):
        t1 = threading.Thread(target=self.gyroscope_sensor)
        t1.daemon = True
        t1.start()

# END

