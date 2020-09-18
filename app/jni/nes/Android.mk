LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := nes
FCEUX_PATH := ../fceux

LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(FCEUX_PATH)
# Add your application source files here...
LOCAL_SRC_FILES := Bridge.cpp \
					Emulator.cpp \
					Nes.cpp \

LOCAL_SHARED_LIBRARIES := main
LOCAL_CFLAGS += -DPSS_STYLE=1 -DHAVE_ASPRINTF
LOCAL_CFLAGS += \
    -frtti \
    -Wall  -Wno-write-strings  \
    -Wno-sign-compare  \
    -Wno-parentheses  \
    -Wno-unused-local-typedefs  \
    -fPIC \
    -Wno-c++11-narrowing \
    -Wno-unused-variable \

LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)
