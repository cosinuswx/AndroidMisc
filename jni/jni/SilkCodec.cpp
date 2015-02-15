#include "Common.h"

#define CLASS_PATH	"com/winomtech/androidmisc/jni/SilkCodec"

// 用来记录一些常用的field
struct fields_t
{
	jfieldID	nativeCodecInJavaObj;	// CSilkCodec对象保存在java对象的哪个字段
};
static fields_t javaCodecFields;

class CSilkCodec
{
private:
	FILE*		mFilePoint;

public:
	CSilkCodec()
	{
		mFilePoint = NULL;
	}

	void init(const char* path)
	{
		mFilePoint = fopen(path, "wb");
		if (NULL == mFilePoint)
		{
			LOGE("file open failed");
			return;
		}

		char* header = new char[16] {0x53, 0x00, 0x49, 0x00, 0x4c, 0x00, 0x4b, 0x00,
			0x5f, 0x00, 0x56, 0x00, 0x32, 0x00, 0x0a, 0x00};
		fwrite(header, sizeof(short), 8, mFilePoint);
	}

	void doDec(void* buf, int len)
	{
		if (NULL != mFilePoint)
		{
			fwrite(buf, 1, len, mFilePoint);
		}
		else
		{
			LOGE("doDec failed, file is null");
		}
	}

	void release()
	{
		if (NULL != mFilePoint)
		{
			fclose(mFilePoint);
			mFilePoint = NULL;
		}
	}
};

void setCodecObject(JNIEnv* env, jobject thiz, CSilkCodec* codec)
{
	// TODO：线程安全

	// TODO：干掉原来的？

	// 设置新的
	env->SetLongField(thiz, javaCodecFields.nativeCodecInJavaObj, (jlong)codec);
}

CSilkCodec* getCodecObject(JNIEnv* env, jobject thiz)
{
	return (CSilkCodec*) env->GetLongField(thiz, javaCodecFields.nativeCodecInJavaObj);
}

// 初始化输出文件，会写入文件头
extern "C"
void Java_com_winomtech_androidmisc_jni_SilkCodec_nativeInit(JNIEnv* env, jobject thiz, jstring strPath)
{
	puts("dsafsdf");

	jclass silkCodecClass = env->FindClass(CLASS_PATH);
	if (NULL == silkCodecClass)
	{
		LOGE("can't find class com/kevin/androidmisc/SilkCodec");
		return;
	}

	// 签名记得用J，表示是long
	javaCodecFields.nativeCodecInJavaObj = env->GetFieldID(silkCodecClass, "mNativeRecorderInJavaObj", "J");
	if (NULL == javaCodecFields.nativeCodecInJavaObj)
	{
		LOGE("can't find field mNativeRecorderInJavaObj");
		return;
	}

	const char* path = env->GetStringUTFChars(strPath, NULL);
	CSilkCodec* codec = new CSilkCodec();
	codec->init(path);

	setCodecObject(env, thiz, codec);
}

// 编码数据到文件中
extern "C"
void Java_com_winomtech_androidmisc_jni_SilkCodec_nativeDoDec(JNIEnv* env, jobject thiz, jcharArray buf, int len)
{
	jchar* charBuf = (jchar*) env->GetPrimitiveArrayCritical(buf, NULL);
	if (NULL == charBuf)
	{
		LOGE("GetPrimitiveArrayCritical failed");
		return;
	}

	CSilkCodec* codec = getCodecObject(env, thiz);
	codec->doDec(charBuf, len * sizeof(jchar));
	
	env->ReleasePrimitiveArrayCritical(buf, charBuf, 0);
}

// 关闭文件，并释放编码器
extern "C"
void Java_com_winomtech_androidmisc_jni_SilkCodec_nativeRelease(JNIEnv* env, jobject thiz)
{
	CSilkCodec* codec = getCodecObject(env, thiz);
	codec->release();
}

