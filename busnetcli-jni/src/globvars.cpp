#include "globvars.h"

JavaVM *jvm;

jclass cls_client = NULL;

jmethodID meth_client_recvdata = NULL;
jmethodID meth_client_connect = NULL;
jmethodID meth_client_disconnect = NULL;
jmethodID meth_client_globalconenct = NULL;
jmethodID meth_client_log = NULL;
