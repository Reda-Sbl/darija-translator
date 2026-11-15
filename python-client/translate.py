"""Python client for the Darija Translator REST service.
Usage: python translate.py "Good morning, how are you?"
"""
import base64
import json
import os
import sys
import urllib.request

URL = os.getenv("TRANSLATOR_URL", "http://localhost:8080/api/translate")
USER = os.getenv("TRANSLATOR_USER", "reda")
PASSWORD = os.getenv("TRANSLATOR_PASS", "changeme")


def translate(text: str) -> str:
    token = base64.b64encode(f"{USER}:{PASSWORD}".encode()).decode()
    req = urllib.request.Request(
        URL,
        data=json.dumps({"text": text}).encode("utf-8"),
        headers={"Content-Type": "application/json", "Authorization": f"Basic {token}"},
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=30) as res:
        return json.load(res)["translation"]


if __name__ == "__main__":
    text = " ".join(sys.argv[1:]) or input("Text to translate: ")
    print(translate(text))
