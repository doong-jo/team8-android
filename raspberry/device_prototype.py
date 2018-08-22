

from bluetooth import *
import signal
import time
from sys import exit

from multiprocessing import Process, Queue
from threading import Thread

from operator import eq
try:
    from PIL import Image
except ImportError:
    exit("This script requires this pillow module\nInstall with : sudo pip install pillow")

import unicornhathd

unicornhathd.rotation(90)
unicornhathd.brightness(1.0)

width, height = unicornhathd.get_shape()

characterImg = Image.open('lofi.png')
leftImg = Image.open('movingArrowLeft.png')
rightImg = Image.open('movingArrowRight.png')
emerImg = Image.open('emergency_modified_long.png')

def receiveMsg():
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
    print('accepted')

    while True:          
        print("Accepted connection from ", client_info)
        try:
            data = client_sock.recv(1024)
            print("data : %s", data);
            if data == "LED_CHARACTERS":
                for o_x in range(int(characterImg.size[0]/width)):
                  for o_y in range(int(characterImg.size[1]/height)):
                        valid = False

                        for x in range(width):
                            for y in range(height):
                                pixel = characterImg.getpixel(((o_x*width)+y, (o_y*height)+x))
                                r, g, b = int(pixel[0]),int(pixel[1]), int(pixel[2])
                                if r or g or b:
                                    valid = True
                                unicornhathd.set_pixel(x, y, r, g, b)
                        if valid:
                            unicornhathd.show()
                            time.sleep(0.5)
                

            
            if len(data) == 0: break
            
            # LED switch case

            #elif data == "LED_WINDY":
                

            #elif data == "LED_SNOW":


            #elif data == "LED_RAIN":


            #elif data == "LED_CUTE":


            elif data == "LED_LEFT":
                for o_x in range(int(leftImg.size[0]/width)):
                  for o_y in range(int(leftImg.size[1]/height)):
                        valid = False

                        for x in range(width):
                            for y in range(height):
                                pixel = leftImg.getpixel(((o_x*width)+y, (o_y*height)+x))
                                r, g, b = int(pixel[0]),int(pixel[1]), int(pixel[2])
                                if r or g or b:
                                    valid = True
                                unicornhathd.set_pixel(x, y, r, g, b)
                        if valid:
                            unicornhathd.show()
                            time.sleep(0.1)
                

            elif data == "LED_RIGHT":
                for o_x in range(int(rightImg.size[0]/width)):
                  for o_y in range(int(rightImg.size[1]/height)):
                        valid = False

                        for x in range(width):
                            for y in range(height):
                                pixel = rightImg.getpixel(((o_x*width)+y, (o_y*height)+x))
                                r, g, b = int(pixel[0]),int(pixel[1]), int(pixel[2])
                                if r or g or b:
                                    valid = True
                                unicornhathd.set_pixel(x, y, r, g, b)
                        if valid:
                            unicornhathd.show()
                            time.sleep(0.1)
                

            elif data == "LED_EMERGENCY":
                for o_x in range(int(emerImg.size[0]/width)):
                    for o_y in range(int(emerImg.size[1]/height)):

                        for x in range(width):
                            for y in range(height):
                                pixel = emerImg.getpixel(((o_x*width)+y, (o_y*height)+x))
                                r, g, b = int(pixel[0]),int(pixel[1]), int(pixel[2])
                                #if r or g or b:
                                #    valid = True
                                unicornhathd.set_pixel(x, y, r, g, b)
                        unicornhathd.show()
                        time.sleep(0.5)


            print("received [%s]" % data)
            print("send [%s]" % data[::-1])
            client_sock.send(data[::-1])
        except IOError:
            print("disconnected")
            client_sock.close()
            server_sock.close()
            print("all done")
            break

        except KeyboardInterrupt:
            print("disconnected")
            client_sock.close()
            server_sock.close()
            unicornhathd.off()
            print("all done")
            break

#receiveMsg()
#pr1 = Process(target=showLED)
#pr2 = Process(target=receiveMsg)

#th1 = Thread(target=showLED)
#th2 = Thread(target=receiveMsg)

#pr1.start()
#pr2.start()

#pr1.join()
#pr2.join()

#th1.start()
#th2.start()

#th1.join()
#th2.join()

receiveMsg()

