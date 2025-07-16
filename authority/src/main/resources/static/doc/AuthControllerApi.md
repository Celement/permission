
# 
## 登录接口
**URL:** `/auth/login`

**Type:** `POST`


**Content-Type:** `application/json; charset=utf-8`

**Description:** 登录接口




**Body-parameters:**

Parameter|Type|Description|Required|Since
---|---|---|---|---
username|string|No comments found.|false|-
password|string|No comments found.|false|-
code|string|No comments found.|false|-
token|string|No comments found.|false|-

**Request-example:**
```
curl -X POST -H 'Content-Type: application/json; charset=utf-8' -i /auth/login --data '{
  "username": "jospeh.rempel",
  "password": "dj1ykv",
  "code": "55540",
  "token": "0fao0k"
}'
```

**Response-example:**
```
{}
```

## 验证码接口
**URL:** `/auth/captcha`

**Type:** `GET`


**Content-Type:** `application/x-www-form-urlencoded;charset=utf-8`

**Description:** 验证码接口





**Request-example:**
```
curl -X GET -i /auth/captcha
```
**Response-fields:**

Field | Type|Description|Since
---|---|---|---
code|int32|No comments found.|-
msg|string|No comments found.|-
data|object|No comments found.|-

**Response-example:**
```
{
  "code": 40,
  "msg": "yvi73c",
  "data": {
    "waring": "You may have used non-display generics."
  }
}
```

## 判断会话是否登录
**URL:** `/auth/isLogin`

**Type:** `GET`


**Content-Type:** `application/x-www-form-urlencoded;charset=utf-8`

**Description:** 判断会话是否登录





**Request-example:**
```
curl -X GET -i /auth/isLogin
```

**Response-example:**
```
string
```

## token信息
**URL:** `/auth/tokenInfo`

**Type:** `GET`


**Content-Type:** `application/x-www-form-urlencoded;charset=utf-8`

**Description:** token信息





**Request-example:**
```
curl -X GET -i /auth/tokenInfo
```

**Response-example:**
```
{}
```

## 权限验证
**URL:** `/auth/authInfo`

**Type:** `GET`


**Content-Type:** `application/x-www-form-urlencoded;charset=utf-8`

**Description:** 权限验证





**Request-example:**
```
curl -X GET -i /auth/authInfo
```

**Response-example:**
```
{}
```

