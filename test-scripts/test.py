import requests
from requests import Response
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:8082"
BASE_URL_API = f"{BASE_URL}/"

import random
import string

def generate_random_string(length):
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0',
    'Accept': '*/*',
    'Accept-Language': 'it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3',
    'Referer': 'http://localhost:3000/',
    #'content-type': 'application/json',
    'Origin': 'http://localhost:3000',
    'Connection': 'keep-alive',
    #"x-forwarded-for": "123456789abcdefghijklmnopqrstuvwxyz, 192.168.1.1"
}

session = requests.session()
session.auth = HTTPBasicAuth('changeit', 'changeit')

def doPost(url, json=None, data=None, auth=None, files=None) -> Response:
    global session
    global HEADERS
    response = session.post(url, headers=HEADERS, json=json, data=data, allow_redirects=False, auth=auth, files=files)
    print(f"\n-------- POST {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response
def doGet(url, auth=None) -> Response:
    global session
    global HEADERS
    response = session.get(url, headers=HEADERS, allow_redirects=False, auth=auth)
    print(f"\n-------- GET {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response
def doDelete(url) -> Response:
    global session
    global HEADERS
    response = session.delete(url, headers=HEADERS, allow_redirects=False)
    print(f"\n-------- DELETE {url} --------")
    print(response.status_code)
    print(response.cookies.items())
    print(response.headers)
    print(response.text)
    return response

def ping():
    doGet(f"{BASE_URL_API}print/ping")
    
def submitJob(html, type):
    json = {
        "html": html,
        "type": type,
        "id": generate_random_string(4),
        "operatorID": 1
    }
    doPost(f"{BASE_URL_API}print/", json=json)
    
ping()

JOB_TYPE_USER = "USER_BADGE"
JOB_TYPE_FURSUIT = "FURSUIT_BADGE"
with open("badgeUsers.html", "r") as f:
    badgeUser = f.read()
with open("badgeFursuits.html", "r") as f:
    badgeFursuit = f.read()
    
#submitJob(badgeUser, JOB_TYPE_USER)
#submitJob(badgeFursuit, JOB_TYPE_FURSUIT)