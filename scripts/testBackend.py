import json

import requests
from requests import Response
from requests.auth import HTTPBasicAuth

BASE_URL = "http://localhost:8082/"
BASE_URL_API = f"{BASE_URL}api/"

import random
import string

def generate_random_string(length):
    letters = string.ascii_letters + string.digits
    return ''.join(random.choice(letters) for i in range(length))

RANDOM_MAIL = True
ACCOUNT_EMAIL = (generate_random_string(10) if RANDOM_MAIL else "dkopasdkopsadosa") + "@keysmasher.femboyyyyy.it"
ACCOUNT_PWD = "A1b2C3d5!"

print(f"ACCOUNT_EMAIL = '{ACCOUNT_EMAIL}'")
print(f"ACCOUNT_PWD = '{ACCOUNT_PWD}'")
print()

ACCOUNT_EMAIL = 'zXWsqWIoLS@keysmasher.femboyyyyy.it'
ACCOUNT_PWD = 'A1b2C3d5!'

HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:133.0) Gecko/20100101 Firefox/133.0',
    'Accept': '*/*',
    #'Accept-Language': 'it-IT,it;q=0.8,en-US;q=0.5,en;q=0.3',
    'Accept-Language': 'en-US,it;q=0.8,en-US;q=0.5,en;q=0.3',
    'Referer': 'http://localhost:3000/',
    #'content-type': 'application/json',
    'Origin': 'http://localhost:3000',
    'Connection': 'keep-alive',
    #"x-forwarded-for": "123456789abcdefghijklmnopqrstuvwxyz, 192.168.1.1"
}

session = requests.session()

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

def register() -> Response:
    json = {
        "email": ACCOUNT_EMAIL,
        "password": ACCOUNT_PWD,
        "username": "Pisnello",
    }
    return doPost(f'{BASE_URL_API}authentication/register', json=json)
def login() -> Response:
    global HEADERS
    json = {
        "email": ACCOUNT_EMAIL,
        #"password": ACCOUNT_PWD + "staminchia"
        "password": ACCOUNT_PWD
    }
    req = doPost(f'{BASE_URL_API}authentication/login', json=json)
    if (req.status_code == 200):
        token = req.json()["accessToken"]
        val = f"Bearer {token}"
        HEADERS["Authorization"] = val
        session.cookies.set("zebra-proxy", val)
        print(HEADERS)

    return req
def getMe() -> Response:
    return doGet(f'{BASE_URL_API}authentication/me')

def getGallery() -> Response:
    return doGet(f'{BASE_URL_API}gallery/')
def getFeatured() -> Response:
    return doGet(f'{BASE_URL_API}gallery/featured')
def getSpecies() -> Response:
    return doGet(f'{BASE_URL_API}gallery/species')
def getFursuitBySlug(slug: str) -> Response:
    return doGet(f'{BASE_URL_API}gallery/{slug}/')

def removeDanglingMedia() -> Response:
    return doPost(f'{BASE_URL_API}admin/gallery/remove-dangling-media')

def createNewFursuit(slug: str) -> Response:
    json = {
        "slug": slug,
        "name": {"ita": "DIOMERDAita", "eng": "DIOMERDAeng"},
        "description": {"ita": "Una fursuit di prova per testare l'upload delle immagini", "eng": "A test fursuit to test image upload"},
        "species": {"ita": "Canide", "eng": "Canine"},
        "date": "2024-06-01",
        "bgColor": "#ff0000",
        "isPublic": True
    }
    return doPost(f'{BASE_URL_API}admin/gallery/', json=json)
def updateFursuit(slug: str) -> Response:
    json = {
        "slug": "stranck3",
        "name": {"ita": "DIOMERDdddd", "eng": "DIOMERDAbbbbbbbb"},
        "description": {"ita": "Una SUCACA fursuit di prova per testare l'upload delle immagini", "eng": "A SUCACA test fursuit to test image upload"},
        "species": {"ita": "Canide2", "eng": "Canine2"},
        "date": "2024-06-01",
        "bgColor": "#ff0000",
        "isPublic": True
    }
    return doPost(f'{BASE_URL_API}admin/gallery/{slug}/', json=json)
def deleteFursuit(slug: str) -> Response:
    return doDelete(f'{BASE_URL_API}admin/gallery/{slug}/')
def getQuoteFromFursuit(slug: str) -> Response:
    return doGet(f'{BASE_URL_API}admin/gallery/{slug}/get-quote')

def uploadThumbnail(slug: str) -> Response:
    #imageName = 'testImage.png'
    imageName = 'testImageSmall.jpg'
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}admin/gallery/{slug}/thumbnail', files=files)
def deleteThumbnail(slug: str) -> Response:
    return doDelete(f'{BASE_URL_API}admin/gallery/{slug}/thumbnail')

def newImage(slug: str, imageName: str) -> Response:
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}admin/gallery/{slug}/new-image', files=files)
def setImageFeatured(imageId: int, featured: bool) -> Response:
    json = {
        "featured": featured
    }
    return doPost(f'{BASE_URL_API}admin/gallery/images/{imageId}/featured', json=json)
def changeFursuitOfImage(imageId: int, slug: str) -> Response:
    json = {
        "slug": slug
    }
    return doPost(f'{BASE_URL_API}admin/gallery/images/{imageId}/fursuit', json=json)
def deleteImage(imageId: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/gallery/images/{imageId}/')

def getOpening() -> Response:
    return doGet(f'{BASE_URL_API}commission-dates/')
def setOpening(date: str) -> Response:
    json = {
        "date": date
    }
    return doPost(f'{BASE_URL_API}admin/commission-dates/opening', json=json)
def setClosing(date: str) -> Response:
    json = {
        "date": date
    }
    return doPost(f'{BASE_URL_API}admin/commission-dates/closing', json=json)

def getCalcData() -> Response:
    return doGet(f'{BASE_URL_API}calc/bundles-options')
def createOptions(opt: dict) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/options/', json=opt)
def createBundle(bundle: dict) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/bundles/', json=bundle)
def createOpt(opt: dict) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/opt/', json=opt)
def deleteOption(id: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/options/{id}')
def deleteBundle(id: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/bundles/{id}')
def deleteOpt(id: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/opt/{id}')

def uploadBundleThumbnail(bundleId: int) -> Response:
    #imageName = 'testImage.png'
    imageName = 'testImageSmall.jpg'
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}admin/calc/bundles/{bundleId}/thumbnail', files=files)
def deleteBundleThumbnail(bundleId: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/bundles/{bundleId}/thumbnail')
def uploadBundleExample(bundleId: int) -> Response:
    #imageName = 'testImage.png'
    imageName = 'testImageSmall.jpg'
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}admin/calc/bundles/{bundleId}/example', files=files)
def deleteBundleExample(bundleId: int, imageId: int) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/bundles/{bundleId}/example/{imageId}')

def getCountries() -> Response:
    return doGet(f'{BASE_URL_API}calc/countries')
def createSession(email: str) -> Response:
    json = {
        "email": email
    }
    return doPost(f'{BASE_URL_API}calc/session/', json=json)
def getSession(email: str, code: str) -> Response:
    return doGet(f'{BASE_URL_API}calc/session/?email={email}&code={code}')
def deleteSession(email: str, code: str) -> Response:
    return doDelete(f'{BASE_URL_API}calc/session/?email={email}&code={code}')
def updateSession(email: str, code: str, selection: dict) -> Response:
    return doPost(f'{BASE_URL_API}calc/session/update?email={email}&code={code}', json=selection)
def sendSessionEmail(email: str, code: str) -> Response:
    return doPost(f'{BASE_URL_API}calc/session/send?email={email}&code={code}')
def verifySecretCode(email: str, code: str, secretCode: str) -> Response:
    return doGet(f'{BASE_URL_API}calc/quote/verify-secret?email={email}&code={code}&suUuperSec3ttC0deUwuu={secretCode}')
def createUpdateQuote(email: str, code: str, quoteReq: dict, secretCode: str = None) -> Response:
    doPost(f'{BASE_URL_API}calc/quote/?email={email}&code={code}{f"&suUuperSec3ttC0deUwuu={secretCode}" if secretCode != None else ""}', json=quoteReq)
def getQuote(email: str, code: str) -> Response:
    return doGet(f'{BASE_URL_API}calc/quote/?email={email}&code={code}')
def uploadQuoteImage(email: str, code: str) -> Response:
    #imageName = 'testImage.png'
    imageName = 'testImageSmall.jpg'
    files = {
        'image': (imageName, open(imageName, 'rb')),
    }
    return doPost(f'{BASE_URL_API}calc/quote/image?email={email}&code={code}', files=files)
def deleteQuoteImage(email: str, code: str, imageId: int) -> Response:
    return doDelete(f'{BASE_URL_API}calc/quote/image/{imageId}?email={email}&code={code}')
def getSecretCode(email: str, code: str, expireDays: int) -> Response:
    return doGet(f'{BASE_URL_API}admin/calc/quote/generate-secret-code?email={email}&code={code}&expireDays={expireDays}')
def adminUpdateQuote(email: str, code: str, quoteReq: dict) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/quote/?email={email}&code={code}', json=quoteReq)
def adminGetQuote(email: str, code: str) -> Response:
    return doGet(f'{BASE_URL_API}admin/calc/quote/?email={email}&code={code}')
def adminDeleteQuote(email: str, code: str) -> Response:
    return doDelete(f'{BASE_URL_API}admin/calc/quote/?email={email}&code={code}')
def adminGetUnconfirmedQuotes() -> Response:
    return doGet(f'{BASE_URL_API}admin/calc/quote/list?from=2000-03-30T20:38:10Z&to=2300-03-30T20:38:10Z&confirmed=false')
def adminGetConfirmedQuotes() -> Response:
    return doGet(f'{BASE_URL_API}admin/calc/quote/list?from=2000-03-30T20:38:10Z&to=2300-03-30T20:38:10Z&confirmed=true')
def adminSendQuotePerEmail(email: str, code: str) -> Response:
    json = {
        "beforeQuoteText": generate_random_string(20),
        "afterQuoteText": generate_random_string(20)
    }
    return doPost(f'{BASE_URL_API}admin/calc/quote/send?email={email}&code={code}', json=json)
def adminConfirmQuote(email: str, code: str) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/quote/confirm?email={email}&code={code}')
def adminCreateFursuitFromQuote(email: str, code: str) -> Response:
    return doPost(f'{BASE_URL_API}admin/calc/quote/create-fursuit?email={email}&code={code}')

def sendEmail(email: str) -> Response:
    json = {
        "email": email,
        "name": generate_random_string(10),
        "text": generate_random_string(20)
    }
    return doPost(f'{BASE_URL_API}contact-form/', json=json)

def generateRandomOption(children: list, id: int=None) -> dict:
    d = {
        "slug": generate_random_string(10),
        "name": {"ita": generate_random_string(10), "eng": generate_random_string(10)},
        "description": {"ita": generate_random_string(20), "eng": generate_random_string(20)},
        "price": random.randint(1, 100) * 100,
        "optionLevel": ["PARTS", "OPTIONS", "COMPLEXITY"][random.randint(0, 2)],
        "optionType": ["CHECKBOX", "MULTI", "LIST"][random.randint(0, 2)],
        "displayOrder": random.randint(0, 100),
        "categorySlug": f"category{random.randint(1, 3)}",
        "childOptions": children,
        #Extra, you might want to comment them
        "optionsOpt": [],
        "min": None,
        "max": None
    }
    if (d["optionType"] == "MULTI"):
        d["min"] = random.randint(1, 4)
        d["max"] = random.randint(d["min"], d["min"] + 4)
    elif (d["optionType"] == "LIST"):
        opts = []
        for i in range(random.randint(1, 5)):
            opts.append(generateOpt())
        d["optionsOpt"] = opts
    if id is not None:
        d["id"] = id
    return d
def generateRandomOptionsTree(depth: int) -> dict:
    if depth == 0:
        return generateRandomOption([])
    children = []
    for i in range(random.randint(1, 3)):
        children.append(generateRandomOptionsTree(depth - 1))
    return generateRandomOption(children)
def generateBundle(options: list, id: int=None) -> dict:
    d = {
        "slug": generate_random_string(10),
        "name": {"ita": generate_random_string(10), "eng": generate_random_string(10)},
        "description": {"ita": generate_random_string(20), "eng": generate_random_string(20)},
        "price": random.randint(1, 100) * 100,
        "displayOrder": random.randint(0, 100),
        "mandatoryOptions": options,
        "selectableOptions": options
    }
    if id is not None:
        d["id"] = id
    return d
def generateOpt(parent: int = None) -> dict:
    d = {
        "slug": generate_random_string(10),
        "name": {"ita": generate_random_string(10), "eng": generate_random_string(10)},
        "description": {"ita": generate_random_string(20), "eng": generate_random_string(20)},
        "price": random.randint(1, 100) * 100,
        "displayOrder": random.randint(0, 100)
    }
    if parent is not None:
        d["parentOptionId"] = parent
    return d
def generateAndAttachOpt(options: list = None) -> dict:
    if options is None:
        options = getCalcData().json()["topLevelOptions"]
    for option in options:
        if option["optionType"] == "LIST":
            for _ in range(random.randint(2, 4)):
                newOpt = generateOpt(option["id"])
                createOpt(newOpt)
        if len(option["childOptions"]) > 0:
            generateAndAttachOpt(option["childOptions"])
def generateRandomSelection(calcData: dict) -> dict:
    def randomSel(option):
        qty = 1
        if option["optionType"] == "MULTI":
            qty = random.randint(option["min"], option["max"])
        optId = None
        if option["optionType"] == "LIST" and len(option["optionsOpt"]) > 0:
            optId = random.choice(option["optionsOpt"])["id"]
        ret = [{
            "optionId": option["id"],
            "quantity": qty,
            "optionOptId": optId
        }]
        for child in option["childOptions"]:
            if random.choice([0, 1, 2]) == 0:
                ret.extend(randomSel(child))
        return ret

    selection = {
        "selectedOptions": [],
        "selectedBundleId": None
    }
    options = calcData["topLevelOptions"]
    options = {opt["id"]: opt for opt in options}
    bundle = random.choice(calcData["bundles"])
    selection["selectedBundleId"] = bundle["id"]
    for opt in bundle["mandatoryOptions"]:
        selection["selectedOptions"].extend(randomSel(options[opt]))
    for opt in bundle["selectableOptions"]:
        if random.choice([0, 1]) == 0:
            selection["selectedOptions"].extend(randomSel(options[opt]))

    return selection
def generateRandomQuoteReq(email: str, code: str) -> dict:
    return {
        "tosAccepted": True,
        "firstName": generate_random_string(10),
        "lastName": generate_random_string(10),
        "birthday": f"1990-01-{random.randint(10, 28)}",
        "country": generate_random_string(10),
        "fullAddress": generate_random_string(20),
        "state": generate_random_string(3),
        "zipCode": generate_random_string(5),
        "allergies": generate_random_string(20),
        "headCircumference": f"{random.randint(50, 60)}cm",
        "armA": f"{random.randint(50, 60)}cm",
        "armB": f"{random.randint(50, 60)}cm",
        "armC": f"{random.randint(50, 60)}cm",
        "armD": f"{random.randint(50, 60)}cm",
        "armE": f"{random.randint(50, 60)}cm",
        "armF": f"{random.randint(50, 60)}cm",
        "shoulderToShoulderLength": f"{random.randint(50, 60)}cm",
        "wingsElasticCircumference": f"{random.randint(50, 60)}cm",
        "feetNumber": f"{random.randint(30, 40)} EU",
        "paymentMethod": random.choice(["PAYPAL", "CREDIT_CARD", "BANK_TRANSFER"]),
        "fiscalCode": generate_random_string(16),
        "socialAccounts": generate_random_string(20),
        "socialAccountNames": generate_random_string(20),
        "fursonaName": generate_random_string(10),
        "species": generate_random_string(10),
        "clientNotes": generate_random_string(20),
        "suitPreferences": generate_random_string(20),
        "email": email,
        "code": code
    }
def generateRandomAdminQuote(quote: dict, quoteLines: list) -> dict:
    return {
        "quote": quote,
        "makerNotes": generate_random_string(20),
        "designPrice": random.randint(1, 100) * 100,
        "discount": random.randint(0, 5) * 100,
        "deliveryCost": random.randint(0, 5) * 100,
        "paidSoFar": random.randint(0, 20) * 100,
        "quoteLines": quoteLines,
        "displayOrder": random.randint(0, 100)
    }
def generateRandomAdminQuoteLine() -> dict:
    return {
        "pricePerUnit": random.randint(1, 100) * 100,
        "quantity": random.randint(1, 5),
        "name": {"ita": generate_random_string(20), "eng": generate_random_string(20)},
        "level": random.randint(0, 2),
        "displayOrder": random.randint(0, 100)
    }

register()
#login()
#getMe()

#getGallery()
#getFeatured()
#getSpecies()
#createNewFursuit("stranck4")
#createNewFursuit("DIOMERDA")
#updateFursuit("stranck4")
#uploadThumbnail("stranck3")
#deleteThumbnail("stranck3")
#removeDanglingMedia()
#newImage("stranck3", "horizontal.jpg")
#newImage("stranck3", "vertical.jpg")
#newImage("DIOMERDA", "vertical.jpg")
#setImageFeatured(1, True)
#setImageFeatured(2, True)
#setImageFeatured(3, True)
#setImageFeatured(3, False)
#changeFursuitOfImage(2, "DIOMERDA")
#deleteImage(23)
#getFursuitBySlug("stranck3")
#deleteFursuit("stranck3")

#setOpening("2024-07-01T12:00:00Z")
#setOpening(None)
#setOpening("2024-07-02T12:00:00Z")
#setOpening("2099-07-02T12:00:00Z")
#setClosing("2024-07-02T12:00:00Z")
#setClosing(None)
#setClosing("2024-07-01T12:00:00Z")
#getOpening()
#createOptions(generateRandomOption([]))
#createOptions(generateRandomOptionsTree(3))
#for _ in range(50):
#    dataIn = generateRandomOptionsTree(3)
#    r = createOptions(dataIn)
#    def sort(data):
#        #print(data)
#        if "id" in data: del data["id"]
#        if "thumbnail" in data: del data["thumbnail"]
#        if "parentOptionId" in data: del data["parentOptionId"]
#        if "examples" in data: del data["examples"]#.sort(key=lambda x: x["id"] if "id" in x else 0)
#        if "childOptions" in data:
#            data["childOptions"].sort(key=lambda x: x["slug"] if "slug" in x else 0)
#            for child in data["childOptions"]:
#                sort(child)
#        if "optionsOpt" in data:
#            data["optionsOpt"].sort(key=lambda x: x["slug"] if "slug" in x else 0)
#            for opt in data["optionsOpt"]:
#                sort(opt)
#    if r.status_code != 200:
#        print("Error creating options")
#        print(r.text)
#        deleteOption(r.json()["id"])
#        break
#    dataOut = r.json()
#    sort(dataIn)
#    sort(dataOut)
#    if dataIn != dataOut:
#        print("Error: data mismatch")
#        print("In:")
#        print(json.dumps(dataIn, indent=4))
#        print("Out:")
#        print(json.dumps(dataOut, indent=4))
#        deleteOption(r.json()["id"])
#        break
#    deleteOption(r.json()["id"])
#    
#createBundle(generateBundle([1, 13, 44]))
#createBundle(generateBundle([1, 44]))
#createBundle(generateBundle([1]))
#deleteOption(1)
#createOpt(generateOpt(51))
#generateAndAttachOpt()
#deleteOpt(44)
#uploadBundleThumbnail(1)
#deleteBundleThumbnail(1)
#uploadBundleExample(1)
#deleteBundleExample(1, 3)

#getCountries()
#CODE = createSession(ACCOUNT_EMAIL).json()["code"]
#deleteSession(ACCOUNT_EMAIL, CODE)
CODE = "cax1p7np"
print(CODE)

#calcData = getCalcData().json()
#for _ in range(40):
#    dataIn = generateRandomSelection(calcData)
#    r = updateSession(ACCOUNT_EMAIL, CODE, dataIn)
#    if r.status_code != 200:
#        print("Error updating session")
#        print(r.text)
#        break
#    dataOut = r.json()
#    if dataIn["selectedBundleId"] != dataOut["selectedBundleId"]:
#        print("Error: bundle mismatch")
#        print("In:")
#        print(json.dumps(dataIn, indent=4))
#        print("Out:")
#        print(json.dumps(dataOut, indent=4))
#        break
#    if dataIn["selectedOptions"].sort(key=lambda x: x["optionId"]) != dataOut["selectedOptions"].sort(key=lambda x: x["optionId"]):
#        print("Error: data mismatch")
#        print("In:")
#        print(json.dumps(dataIn, indent=4))
#        print("Out:")
#        print(json.dumps(dataOut, indent=4))
#        break
#updateSession(ACCOUNT_EMAIL, CODE, generateRandomSelection(calcData))
#randomSel = generateRandomSelection(calcData)
#randomSel["optionOptId"] = 1
#updateSession(ACCOUNT_EMAIL, CODE, randomSel)
#sendSessionEmail(ACCOUNT_EMAIL, CODE)
#verifySecretCode(ACCOUNT_EMAIL, CODE, "wrongcode")
#secret = getSecretCode(ACCOUNT_EMAIL, CODE, 1).json()["code"]
#verifySecretCode(ACCOUNT_EMAIL, CODE, secret)
#verifySecretCode(ACCOUNT_EMAIL, CODE, secret[:-2] + "00")
#createUpdateQuote(ACCOUNT_EMAIL, CODE, generateRandomQuoteReq(ACCOUNT_EMAIL, CODE))
#uploadQuoteImage(ACCOUNT_EMAIL, CODE)
#deleteQuoteImage(ACCOUNT_EMAIL, CODE, 2)
#quote = getQuote(ACCOUNT_EMAIL, CODE).json()["quote"]
#adminUpdateQuote(ACCOUNT_EMAIL, CODE, generateRandomAdminQuote(quote, []))
#adminDeleteQuote(ACCOUNT_EMAIL, CODE)
#adminSendQuotePerEmail(ACCOUNT_EMAIL, CODE)
#adminGetQuote(ACCOUNT_EMAIL, CODE)
#quote = adminGetQuote(ACCOUNT_EMAIL, CODE).json()["quoteInformation"]
#quote["quote"] = quote["quote"]["quote"]
#del quote["quote"]["images"]
#quote["quoteLines"].extend([generateRandomAdminQuoteLine()])
#quote["quoteLines"] = quote["quoteLines"][1:]
#adminUpdateQuote(ACCOUNT_EMAIL, CODE, quote)
#adminGetUnconfirmedQuotes()
#adminGetConfirmedQuotes()
#adminConfirmQuote(ACCOUNT_EMAIL, CODE)
#adminCreateFursuitFromQuote(ACCOUNT_EMAIL, CODE)
#getQuoteFromFursuit("Hl1usjkuNb")
#sendEmail(ACCOUNT_EMAIL)