/*
 * Client.c
 *
 *  Created on: 2016年7月11日
 *      Author: liuxy
 */

#include "Client.h"
#include <stdbool.h>
#include <smartbus_netcli_interface.h>
#include "globvars.h"
#include "callbacks.h"


jint JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_initiateLibrary(
    JNIEnv * env, jclass cls, jbyte unit_id) {
    (*env)->GetJavaVM(env, &jvm);
    // Get Client Class
    cls_client = cls;
    // Init
    int result = SmartBusNetCli_Init((unsigned char) unit_id);
    if (result != SMARTBUS_ERR_OK) {
        return (jint) result;
    }
    // Set callbacks
    meth_client_globalconenct = (*env)->GetStaticMethodID(env, cls,
                                "callbackGlobalConnect", "(BBBBLjava/lang/String;)V");
    meth_client_connect = (*env)->GetStaticMethodID(env, cls, "callbackConnect",
                          "(BII)V");
    meth_client_disconnect = (*env)->GetStaticMethodID(env, cls,
                             "callbackDisconnect", "(B)V");
    meth_client_recvdata = (*env)->GetStaticMethodID(env, cls, "callbackData",
                           "(BBBBBBBB[B)V");
    meth_client_log = (*env)->GetStaticMethodID(env, cls, "callbackLog",
                      "(Ljava/lang/String;Z)V");
    SmartBusNetCli_SetCallBackFn(connection, recvdata, disconnect, NULL,
                                 global_connect, NULL);
    SmartBusNetCli_SetTraceStr(trace, trace_err);
    return (jint) result;
}

void JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_releaseLibrary(
    JNIEnv * env, jclass cls) {
    cls_client = NULL;
    SmartBusNetCli_Release();
}

jint JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_createConnect(
    JNIEnv * env, jclass cls, jbyte local_clientid, jint local_clienttype,
    jstring master_ip, jshort master_port, jstring slaver_ip,
    jshort slaver_port, jstring author_username, jstring author_pwd,
    jstring add_info) {
    const char *pc_masterip = (*env)->GetStringUTFChars(env, master_ip, NULL);
    const char *pc_slaverip = (*env)->GetStringUTFChars(env, slaver_ip, NULL);
    const char *pc_author_username = (*env)->GetStringUTFChars(env,
                                     author_username, NULL);
    const char *pc_author_pwd = (*env)->GetStringUTFChars(env, author_pwd, NULL);
    const char *pc_add_info = (*env)->GetStringUTFChars(env, add_info, NULL);
    int result = SmartBusNetCli_CreateConnect(
                     (unsigned char) local_clientid, (int) local_clienttype,
                     pc_masterip, (unsigned short) master_port,
                     pc_slaverip, (unsigned short) slaver_port,
                     pc_author_username, pc_author_pwd, pc_add_info
                 );
    (*env)->ReleaseStringUTFChars(env, slaver_ip, pc_masterip);
    (*env)->ReleaseStringUTFChars(env, slaver_ip, pc_slaverip);
    (*env)->ReleaseStringUTFChars(env, author_username, pc_author_username);
    (*env)->ReleaseStringUTFChars(env, author_pwd, pc_author_pwd);
    (*env)->ReleaseStringUTFChars(env, add_info, pc_add_info);
    return (jint) result;
}

jint JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_launchFlow(
    JNIEnv * env, jclass cls, jbyte local_client_id, jint server_unit_id,
    jint ipsc_index, jstring project_id, jstring flow_id, jint mode,
    jint timeout, jstring value_list) {
    const char* pc_project_id = (*env)->GetStringUTFChars(env, project_id,
                                NULL);
    const char* pc_flow_id = (*env)->GetStringUTFChars(env, flow_id, NULL);
    const char* pc_value_list = (*env)->GetStringUTFChars(env, value_list, NULL);
    int result = SmartBusNetCli_RemoteInvokeFlow(
                     (unsigned char) local_client_id, (int) server_unit_id,
                     (int) ipsc_index, pc_project_id, pc_flow_id, (int) mode,
                     (int) timeout, pc_value_list
                 );
    (*env)->ReleaseStringUTFChars(env, project_id, pc_project_id);
    (*env)->ReleaseStringUTFChars(env, flow_id, pc_flow_id);
    (*env)->ReleaseStringUTFChars(env, value_list, pc_value_list);
    return (jint) result;
}

jint JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_sendNotification(
    JNIEnv * env, jclass cls, jbyte local_client_id, jint server_unit_id,
    jint ipsc_index, jstring project_id, jstring title, jint mode,
    jint expires, jstring param) {
    const char* pc_project_id = (*env)->GetStringUTFChars(env, project_id, NULL);
    const char* pc_title = (*env)->GetStringUTFChars(env, title, NULL);
    const char* pc_param = (*env)->GetStringUTFChars(env, param, NULL);
    int result = SmartBusNetCli_SendNotify((unsigned char) local_client_id,
                                           (int) server_unit_id, (int) ipsc_index, pc_project_id, pc_title,
                                           (int) mode, (int) expires, pc_param);
    (*env)->ReleaseStringUTFChars(env, project_id, pc_project_id);
    (*env)->ReleaseStringUTFChars(env, title, pc_title);
    (*env)->ReleaseStringUTFChars(env, param, pc_param);
    return (jint) result;
}

jint JNICALL Java_com_lsxy_app_area_cti_busnetcli_Client_sendData(JNIEnv * env,
        jclass cls, jbyte local_client_id, jbyte cmd, jbyte cmd_type,
        jint dst_unit_id, jint dst_client_id, jint dst_client_type,
        jbyteArray data) {
    jsize data_sz = (*env)->GetArrayLength(env, data);
    jboolean is_copy = false;
    jbyte *buf = (*env)->GetByteArrayElements(env, data, &is_copy);
    int result = SmartBusNetCli_SendData((unsigned char) local_client_id,
                                         (unsigned char) cmd, (unsigned char) cmd_type, (int) dst_unit_id,
                                         (int) dst_client_id, (int) dst_client_type, (const void*) buf,
                                         (int) data_sz);
    return (jint) result;
}
