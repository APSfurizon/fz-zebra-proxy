inData = """
{
    "slug": "OWwrPWbGan",
    "name": "hWaJqEZ5gF",
    "description": "3NqhXwF4hmCDtWCI86Zs",
    "price": 1000,
    "optionLevel": "PARTS",
    "optionType": "MULTI",
    "displayOrder": 68,
    "categorySlug": "category2",
    "childOptions": [
        {
            "slug": "HyqhsJPTdU",
            "name": "6JJz6fKytz",
            "description": "J21lv0CYDmniIk1MXUQS",
            "price": 4400,
            "optionLevel": "OPTIONS",
            "optionType": "MULTI",
            "displayOrder": 62,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": [],
            "min": 1,
            "max": 4
        },
        {
            "slug": "YLDFfaSWrg",
            "name": "6HSjlAOzeM",
            "description": "FbBf5ZanTkh6pMrGXm07",
            "price": 5600,
            "optionLevel": "COMPLEXITY",
            "optionType": "LIST",
            "displayOrder": 62,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": [
                {
                    "slug": "sI3eBgvNij",
                    "name": "JHGt7ZQ5Os",
                    "description": "uPGdhcbn1BNKLiKxJmZS",
                    "price": 400,
                    "displayOrder": 76
                }
            ],
            "min": null,
            "max": null
        },
        {
            "slug": "t02Ztky5To",
            "name": "vzVyMO1VcS",
            "description": "u6CuuEtPSJaiM87B3imv",
            "price": 9000,
            "optionLevel": "PARTS",
            "optionType": "CHECKBOX",
            "displayOrder": 9,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": [],
            "min": null,
            "max": null
        }
    ],
    "optionsOpt": [],
    "min": 4,
    "max": 6
}
"""
outData = """
{
    "slug": "OWwrPWbGan",
    "name": "hWaJqEZ5gF",
    "description": "3NqhXwF4hmCDtWCI86Zs",
    "price": 1000,
    "optionLevel": "PARTS",
    "optionType": "MULTI",
    "min": 4,
    "max": 6,
    "displayOrder": 68,
    "categorySlug": "category2",
    "childOptions": [
        {
            "slug": "HyqhsJPTdU",
            "name": "6JJz6fKytz",
            "description": "J21lv0CYDmniIk1MXUQS",
            "price": 4400,
            "optionLevel": "OPTIONS",
            "optionType": "MULTI",
            "min": 1,
            "max": 4,
            "displayOrder": 62,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": []
        },
        {
            "slug": "YLDFfaSWrg",
            "name": "6HSjlAOzeM",
            "description": "FbBf5ZanTkh6pMrGXm07",
            "price": 5600,
            "optionLevel": "COMPLEXITY",
            "optionType": "LIST",
            "min": null,
            "max": null,
            "displayOrder": 62,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": [
                {
                    "slug": null,
                    "name": null,
                    "description": null,
                    "price": 0,
                    "displayOrder": 0,
                    "parentOptionId": null
                }
            ]
        },
        {
            "slug": "t02Ztky5To",
            "name": "vzVyMO1VcS",
            "description": "u6CuuEtPSJaiM87B3imv",
            "price": 9000,
            "optionLevel": "PARTS",
            "optionType": "CHECKBOX",
            "min": null,
            "max": null,
            "displayOrder": 9,
            "categorySlug": "category1",
            "childOptions": [],
            "optionsOpt": []
        }
    ],
    "optionsOpt": []
}
"""

import json

jsonIn = json.loads(inData)
jsonOut = json.loads(outData)  

print(jsonIn == jsonOut)