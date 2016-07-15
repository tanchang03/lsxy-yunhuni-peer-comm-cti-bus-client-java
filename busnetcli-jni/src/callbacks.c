#include "callbacks.h"
#include <stdbool.h>
#include <jni.h>
#include "globvars.h"

static JNIEnv* env = NULL;

void connection(void * arg, unsigned char local_client_id,
                int accesspoint_unit_id, int ack) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    (*env)->CallStaticVoidMethod(env, cls_client, meth_client_connect, (jbyte) local_client_id, (jint) accesspoint_unit_id, (jint) ack);
}

void disconnect(void * arg, unsigned char local_client_id) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    (*env)->CallStaticVoidMethod(env, cls_client, meth_client_connect, (jbyte) local_client_id);
}

void recvdata(void * arg, unsigned char local_client_id,
              SMARTBUS_PACKET_HEAD * head, void * data, int size) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    jobject obj_head = (*env)->NewObject(
                           env, cls_head, meth_head,
                           (jbyte) head->head_flag, (jbyte) head->cmd, (jbyte) head->cmdtype,
                           (jbyte) head->src_unit_id, (jbyte) head->src_unit_client_id,
                           (jbyte) head->src_unit_client_type, (jbyte) head->dest_unit_id,
                           (jbyte) head->dest_unit_client_id,
                           (jbyte) head->dest_unit_client_type
                       );
    jbyteArray data_obj = NULL;
    (*env)->SetByteArrayRegion(env, data_obj, 0, size, data);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_recvdata,
        (jbyte) local_client_id, obj_head, data_obj
    );
}

void global_connect(void * arg, char unit_id, char client_id, char client_type,
                    char accesspoint_unit, char status, const char * add_info) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    jstring txt = (*env)->NewStringUTF(env, add_info);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_globalconenct,
        (jbyte) unit_id, (jbyte) unit_id, (jbyte) client_id,
        (jbyte) client_type, (jbyte) accesspoint_unit, (jbyte) status, txt
    );
}

void trace(const char * msg) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    jstring txt = (*env)->NewStringUTF(env, msg);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_log,
        txt, (jboolean) false
    );
}

void trace_err(const char * msg) {
    if (!env)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    jstring txt = (*env)->NewStringUTF(env, msg);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_log,
        txt, (jboolean) true
    );
}
