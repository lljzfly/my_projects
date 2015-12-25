#!/usr/bin/env python
# -*- coding:utf-8 -*-

import time
import datetime
import json
import py_client

def RunTest():
    #print(help(py_client))
    if not py_client.Init():
        print("init fail")
        return

    #连接文件服务器
    conn_id=py_client.Connect("tcp:127.0.0.1:10030",True)
    print("conn_id=%d" % conn_id)

    request = {
        "src_type":65001,
        "src_id":1,
        "dest_type":4,
        "dest_id":0,
        "msg_id":2,
        "version":0,
        "data":{}
    }
    req_str=json.dumps(request)
    #print('req:%s' % req_str)
    resp_str=py_client.Send(conn_id,req_str)
    #print("resp:%s" % resp_str)
    resp_json=json.loads(resp_str)
    if resp_json['function_return']:
        print('register fail,error_msg=%s' 
            % resp_json['function_return'])
        return

    if 0!=resp_json['error_code']:
        print('register fail,error_code=%d' %
            resp_json['error_code'])
        return

    account="lljzfly"
    password="123456"
    #下载文件
    print("------test DownloadFileReq")
    request = {
        "src_type":65001,
        "src_id":1,
        "dest_type":4,
        "dest_id":0,
        "msg_id":1006,
        "version":0,
        "data":{
            "account":account,
            "password":password,
            "tfs_file_name":"T19yETByJT1RCvBVdK",
        }
    }
        
    req_str=json.dumps(request)
    print("req_str:%s" % req_str)
    resp_str=py_client.Send(conn_id,req_str)
    print('resp:%s' % resp_str)
    resp_json=json.loads(resp_str)
    if resp_json['function_return']:
        print('request fail,error_msg=%s'
            % (resp_json['function_return']))
    if 0!=resp_json['error_code']:
        print('request fail,error_code=%d' %
            resp_json['error_code'])
    print('--------data=%s' % resp_json['data'])

    py_client.Disconnect(conn_id)

    py_client.Stop()
    py_client.Wait()


if "__main__"==__name__:
    RunTest()