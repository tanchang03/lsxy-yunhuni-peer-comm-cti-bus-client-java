/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   globvars.h
 * Author: 雪彦
 *
 * Created on 2016年7月2日, 下午5:08
 */

#ifndef GLOBVARS_H
#define GLOBVARS_H

#include <jni.h>

#ifdef __cplusplus
extern "C" {
#endif


    extern JavaVM *jvm;

    extern jclass cls_client;
    
    extern jmethodID meth_client_recvdata;    
    extern jmethodID meth_client_connect;
    extern jmethodID meth_client_disconnect;
    extern jmethodID meth_client_globalconenct;
    extern jmethodID meth_client_log;
    
    extern jclass cls_head;
    extern jmethodID meth_head;


#ifdef __cplusplus
}
#endif

#endif /* GLOBVARS_H */

