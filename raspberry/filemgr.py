import json

# -------------------- DEFINE FILE ------------------ #
FILE_STATE_JSON_NAME = "state.json"
#######################################################


class FileManager(object):
    def __init__(self):
        pass

    def saveLEDState(self, dictionaryData):
        print("saveLEDState")

        with open(FILE_STATE_JSON_NAME, 'w') as outfile:
            json.dump(dictionaryData, outfile)

    def readState(self):
        with open(FILE_STATE_JSON_NAME, 'r') as f:
            stateDic = json.load(f)

        return stateDic

    # def saveImage(self):
        # pass

