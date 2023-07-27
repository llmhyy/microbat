def genMsg(message, cls):
    return f"[{cls.__name__}] {message}"

def printMsg(message, cls):
    print(genMsg(message, cls))