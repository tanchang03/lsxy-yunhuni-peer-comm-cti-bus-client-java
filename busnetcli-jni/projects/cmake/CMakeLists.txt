CMAKE_MINIMUM_REQUIRED(VERSION 2.6)

PROJECT(busnetcli-jni)

AUX_SOURCE_DIRECTORY(../../src SRC)
ADD_LIBRARY ( ${PROJECT_NAME} SHARED ${SRC} )

IF ( ${JAVA_INCLUDE_PATH} and ${JAVA_INCLUDE_PATH2} )
    INCLUDE_DIRECTORIES ( ${JAVA_INCLUDE_PATH} )
    INCLUDE_DIRECTORIES ( ${JAVA_INCLUDE_PATH2} )
ELSE()
    MESSAGE(WARNING "CMake's JNI Module can not find JAVA INCLUDE PATH, try to find that by environment variable JAVA_HOME")
    # 通过 JAVA_HOME环境变量定位 jni.h和jni_md.h
    IF(NOT DEFINED ENV{JAVA_HOME})
        # 没有找到JAVA_HOME环境变量，输出错误信息退出
        MESSAGE(FATAL_ERROR "Environment variable JAVA_HOME not defined")  
    ENDIF()
    SET(JNI_INCLUDE "$ENV{JAVA_HOME}/include")
    # jni.h的位置加入搜索路径
    INCLUDE_DIRECTORIES(${JNI_INCLUDE})
    # 判断操作系统,添加jni_md.h搜索路径
    IF(WIN32)
        #win32系统下`jni_md.h`位于`<jni_h_dir>/win32`下
        INCLUDE_DIRECTORIES("${JNI_INCLUDE}/win32")
    ELSEIF(CMAKE_SYSTEM_NAME MATCHES "Linux")
        #linux系统下`jni_md.h`位于`<jni_h_dir>/linux`下
        INCLUDE_DIRECTORIES("${JNI_INCLUDE}/linux")
    ELSE()
        # 不支持的操作系统报错退出
        MESSAGE(FATAL_ERROR "other unsupported platform: ${CMAKE_SYSTEM_NAME}")  
    ENDIF()
ENDIF()

if(NOT DEFINED ENV{BUSNETCLI_HOME})
    message(WARN, "Environment variable BUSNETCLI_HOME not defined, try using system includ and library path.")
else()
    INCLUDE_DIRECTORIES(ENV{BUSNETCLI_HOME}/include)
    LINK_DIRECTORIES(ENV{BUSNETCLI_HOME}/lib)
endif()
TARGET_LINK_LIBRARIES(${PROJECT_NAME} busnetcli)
