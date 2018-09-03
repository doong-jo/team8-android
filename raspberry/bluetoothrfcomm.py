from bluetooth import *

import threading

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


# ----------- DEFINE BLUETOOTH ATTRIBUTE ----------- #
BT_SIZE_READ_BYTE = 6
BT_UUID = "94f39d29-7d6d-437d-973b-fba39e49d4ee"
######################################################


class BluetoothRFCOMM(object):
    def __init__(self):
        pass

    def receiveMsg(self, ledcb):
        while True:

            server_sock = BluetoothSocket(RFCOMM)
            server_sock.bind(('', PORT_ANY))
            server_sock.listen(1)

            port = server_sock.getsockname()[1]

            advertise_service(server_sock, "BtLED",
                              service_id=BT_UUID,
                              service_classes=[BT_UUID, SERIAL_PORT_CLASS],
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

                    if signalData == 0:
                        ledcb(valueData, optionalData, -1, -1)

                    elif signalData == 1:
                        ledcb(-1, -1, valueData * 0.1 + optionalData * 0.01, -1)

                    elif signalData == 2:
                        ledcb(-1, -1, -1, valueData * 0.1 + optionalData * 0.01)

                except IOError:
                    print("disconnected")
                    client_sock.close()
                    server_sock.close()
                    print("all done (disconnected)")
                    break

                except KeyboardInterrupt:
                    print("receiveMsg KeyboardInterrupt")
                    break


    def run(self, ledcb):
        t1 = threading.Thread(target=self.receiveMsg, args=(ledcb, ))
        t1.daemon = True
        t1.start()
