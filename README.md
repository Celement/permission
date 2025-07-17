测试用例
```
import requests

# 定义目标URL
url1 = "http://localhost:9090/auth/captcha"  # 替换为实际API地址
# 准备请求头

response = requests.get(url1)
code=response.json()["data"]["code"]
token=response.json()["data"]["token"]
print("测试验证码接口成功")
print(response.json())
# 定义目标URL
url2 = "http://localhost:9090/auth/login"  # 替换为实际API地址
# 准备请求头
headers = {
    "Content-Type": "application/json",  # 声明JSON内容类型
    # "Authorization": "Bearer YOUR_ACCESS_TOKEN"  # 替换为实际的认证令牌
}

payload = {
    "username": "admin",
    "password": "admin",
    "code": code,
    "token":token
}
try:
    # 发送POST请求
    response = requests.post(
        url2,
        json=payload,  # 自动序列化为JSON并设置Content-Type
        headers=headers
    )
    # 检查HTTP状态码
    if response.status_code == 200:
        print("测试登录接口成功")
        print("请求成功！")
        print("响应内容：", response.json())  # 解析JSON响应
        tokenName=response.json()["data"]["tokenName"]
        tokenValue=response.json()["data"]["tokenValue"]
    else:
        print(f"请求失败！状态码：{response.status_code}")
        print("错误信息：", response.text)

except requests.exceptions.RequestException as e:
    print("请求异常：", e)
except ValueError:  # JSON解析异常
    print("响应不是有效的JSON格式")

url3="http://localhost:9090/auth/isLogin"
headers1 = {
    # "Content-Type": "application/json",  # 声明JSON内容类型
    "Authorization": tokenValue  # 替换为实际的认证令牌
}
response = requests.get(url3,headers=headers1 )
print("测试验证登录接口成功")
print(response.text)

url4="http://localhost:9090/auth/tokenInfo"
headers1 = {
    # "Content-Type": "application/json",  # 声明JSON内容类型
    "Authorization": tokenValue  # 替换为实际的认证令牌
}
response = requests.get(url4,headers=headers1 )
print("测试token信息接口成功")
print(response.text)

url5="http://localhost:9090/auth/authInfo"
headers1 = {
    # "Content-Type": "application/json",  # 声明JSON内容类型
    "Authorization": tokenValue  # 替换为实际的认证令牌
}
response = requests.get(url5,headers=headers1 )
print("测试认证信息接口成功")
print(response.text)    #默认会把有些数据类型变为python的数据类型
# email/{username
url6="http://localhost:9090/auth/email/admin"
headers1 = {
    # "Content-Type": "application/json",  # 声明JSON内容类型
    "Authorization": tokenValue  # 替换为实际的认证令牌
}
response = requests.get(url6,headers=headers1 )
print("测试发送邮箱验证码")
print(response.text)    #默认会把有些数据类型变为python的数据类型

#测试注册
url7="http://localhost:9090/auth/register"
print(code)
print(token)
payload2 = {
    "username":"zs",
    "password":"zs",
    "checkPass":"zs",
    "nickname":"zs",
    "avatar":"zs",
    "phone":"13913913999",
    "email":"123@qq.com",
    "content":"真的不错",
    "code": code,
    "token":token
}
response = requests.post(url7,
                         headers=headers ,
                         json=payload2)

print(response.text)    #默认会把有些数据类型变为python的数据类型

```
