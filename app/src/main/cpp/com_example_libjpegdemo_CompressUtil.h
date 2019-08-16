//
// Created by user on 2019/8/16.
//
#include <jni.h>
#ifndef LIBJPEGDEMO_COM_EXAMPLE_LIBJPEGDEMO_COMPRESSUTIL_H
#define LIBJPEGDEMO_COM_EXAMPLE_LIBJPEGDEMO_COMPRESSUTIL_H

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_example_libjpegdemo_CompressUtil
 * Method:    compressBitmap
 * Signature: (IILjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_example_libjpegdemo_CompressUtil_compressBitmap
        (JNIEnv *, jclass, jobject, jint, jstring);

#ifdef __cplusplus
}
#endif
#endif