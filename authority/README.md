# permission
```
#测试文件上传

url8 = 'http://localhost:9090/file/upload'  # 替换为实际的上传URL

# 假设我们有一个文件名为'test.txt'，位于当前目录
file_path = 'output_image.png'

# 以二进制方式打开文件
with open(file_path, 'rb') as f:
    # 构造文件字典，'file'是服务器端接收文件的字段名，可以根据实际情况修改
    files = {'file': (file_path, f)}
    # 如果需要同时传递其他表单字段，可以使用data参数
    # data = {'key': 'value'}

    # 发送POST请求
    response = requests.post(url8,
                             files=files,
                             headers=headers1)

# 输出响应
# print(response.status_code)
print(response.text)



url9 = 'http://localhost:9090/file/page'  # 替换为实际的上传URL


response = requests.get(url9)
# 输出响应
# print(response.status_code)
print(response.text)
```