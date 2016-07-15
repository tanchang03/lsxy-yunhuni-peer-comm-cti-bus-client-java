/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   callbacks.h
 * Author: 雪彦
 *
 * Created on 2016年7月2日, 下午4:28
 */

#include <smartbus.h>

#ifndef CALLBACKS_H
#define CALLBACKS_H

#ifdef __cplusplus
extern "C" {
#endif

    void WINAPI connection(void * arg, unsigned char local_client_id, int accesspoint_unit_id, int ack);
    void WINAPI disconnect(void * arg, unsigned char local_client_id);
    void WINAPI recvdata(void * arg, unsigned char local_client_id, SMARTBUS_PACKET_HEAD * head, void * data, int size);
    void WINAPI global_connect(void * arg, char unitid, char clientid, char clienttype, char accesspoint_unit, char status, const char * add_info);
    void WINAPI trace(const char * msg);
    void WINAPI trace_err(const char * msg);

#ifdef __cplusplus
}
#endif

#endif /* CALLBACKS_H */

