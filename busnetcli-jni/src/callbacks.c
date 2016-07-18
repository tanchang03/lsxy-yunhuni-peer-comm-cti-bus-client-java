#include "callbacks.h"
#include <stdlib.h>
#include <stdbool.h>
#include <jni.h>
#include "globvars.h"

static JNIEnv* get_current_thread_env() {
    JNIEnv* env = NULL;
    if ((*jvm)->GetEnv(jvm, (void**) (&env), JNI_VERSION_1_8) == JNI_EDETACHED)
        (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
    return env;
}

void connection(void * arg, unsigned char local_client_id,
                int accesspoint_unit_id, int ack) {
    JNIEnv* env = get_current_thread_env();
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_connect, (jbyte) local_client_id, (jint) accesspoint_unit_id, (jint) ack
    );
}

void disconnect(void * arg, unsigned char local_client_id) {
    JNIEnv* env = get_current_thread_env();
    (*env)->CallStaticVoidMethod(env, cls_client, meth_client_connect, (jbyte) local_client_id);
}

void recvdata(void * arg, unsigned char local_client_id,
              SMARTBUS_PACKET_HEAD * head, void * data, int size) {
    JNIEnv* env = get_current_thread_env();
    jbyteArray jbytes_data = (*env)->NewByteArray(env, size);
    (*env)->SetByteArrayRegion(env, jbytes_data, 0, size, data);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_recvdata,
        (jbyte) head->cmd, (jbyte) head->cmdtype,
        (jbyte) head->src_unit_id, (jbyte) head->src_unit_client_id, (jbyte) head->src_unit_client_type,
        (jbyte) head->dest_unit_id, (jbyte) head->dest_unit_client_id, (jbyte) head->dest_unit_client_type,
        jbytes_data
    );
}

void global_connect(void * arg, char unit_id, char client_id, char client_type,
                    char accesspoint_unit, char status, const char * add_info) {
    JNIEnv* env = get_current_thread_env();
    jstring txt = (*env)->NewStringUTF(env, add_info);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_globalconenct,
        (jbyte) unit_id, (jbyte) client_id, (jbyte) client_type,
        (jbyte) status, txt
    );
}

void trace(const char * msg) {
    JNIEnv* env = get_current_thread_env();
    jstring txt = (*env)->NewStringUTF(env, msg);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_log,
        txt, (jboolean) false
    );
}

void trace_err(const char * msg) {
    JNIEnv* env = get_current_thread_env();
    jstring txt = (*env)->NewStringUTF(env, msg);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_log,
        txt, (jboolean) true
    );
}
