LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OBJDIR          := ../../
LOCAL_LDLIBS    += -L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE    := andoridmisc

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include/

LOCAL_SRC_FILES := SilkCodec.cpp
LOCAL_SRC_FILES += JniHook.cpp

include $(BUILD_SHARED_LIBRARY)
