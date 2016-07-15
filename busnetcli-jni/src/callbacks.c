#include "callbacks.h"
#include <stdbool.h>
#include <pthread.h>
#include <jni.h>
#include "globvars.h"

#define TRACE_ENV_ARRAY_LEN 8

typedef struct _thread_env_t {
    pthread_t thread;
    JNIEnv* env;
} thread_env_t;

static pthread_mutex_t thread_env_mtx = PTHREAD_MUTEX_INITIALIZER;
static thread_env_t thread_env_array[TRACE_ENV_ARRAY_LEN] = {
    {0, NULL}, {0, NULL}, {0, NULL}, {0, NULL},
    {0, NULL}, {0, NULL}, {0, NULL}, {0, NULL}
};

static JNIEnv* get_current_thread_env() {
    JNIEnv* env = NULL;
    thread_env_t *thread_env = NULL;
    pthread_t thread = pthread_self();
    pthread_mutex_lock(&thread_env_mtx);
    {
        for (int i = 0; i < TRACE_ENV_ARRAY_LEN; ++i) {
            thread_env = &thread_env_array[i];
            if (thread == thread_env->thread) {
                env = thread_env->env;
                break;
            }
        }
        if (!env) {
            for (int i = 0; i < TRACE_ENV_ARRAY_LEN; ++i) {
                thread_env = &thread_env_array[i];
                if (!thread_env->thread) {
                    (*jvm)->AttachCurrentThread(jvm, (void**) (&env), NULL);
                    thread_env->thread = thread;
                    thread_env->env = env;
                    break;
                }
            }
        }
    }
    pthread_mutex_unlock(&thread_env_mtx);
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
    JNIEnv* env =
        get_current_thread_env();
    (*env)->CallStaticVoidMethod(env, cls_client, meth_client_connect, (jbyte) local_client_id);
}

void recvdata(void * arg, unsigned char local_client_id,
              SMARTBUS_PACKET_HEAD * head, void * data, int size) {
    JNIEnv* env = get_current_thread_env();
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
    JNIEnv* env = get_current_thread_env();
    jstring txt = (*env)->NewStringUTF(env, add_info);
    (*env)->CallStaticVoidMethod(
        env, cls_client, meth_client_globalconenct,
        (jbyte) unit_id, (jbyte) client_id, (jbyte) client_type,
        (jbyte) status, txt
    );
}

void trace(const char * msg) {
    pthread_t trd;
    trd = pthread_self();
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
