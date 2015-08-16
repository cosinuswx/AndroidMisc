#ifndef _MEMORY_BUFF_H
#define _MEMORY_BUFF_H

#include <linux/cdev.h>
#include <linux/semaphore.h>

#define MEMORY_BUFF_NODE_NAME	"membuff"
#define MEMORY_BUFF_FILE_NAME	"membuff"
#define MEMORY_BUFF_CLASS_NAME	"membuff"

#define DEVICE_BUFFER_LENGTH 1024

#define MASK_READ	0x01
#define MASK_WRITE	0x02

struct membuf_dev {
	char* pBuffer;
	size_t bufLen;
	size_t wroteLen;
	struct cdev dev;
	struct semaphore sema;
};

#endif

