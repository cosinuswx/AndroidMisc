cmake_minimum_required(VERSION 2.8.4)
project(jni)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")

INCLUDE_DIRECTORIES("/Users/kevinhuang/Soft/android-ndk-r10d/platforms/android-21/arch-arm/usr/include"
					"./include")
	
set(SOURCE_FILES
	SilkCodec.cpp
	JniHook.cpp)
add_executable(jni ${SOURCE_FILES})