#include <linux/init.h>  
#include <linux/module.h>  
#include <linux/types.h>  
#include <linux/fs.h>  
#include <linux/proc_fs.h>  
#include <linux/device.h>  
#include <asm/uaccess.h>  
#include <linux/slab.h>

#include "membuff.h"

static int devMajor = 0;
static int devMinor = 0;

static struct class* pMembufClass = NULL;
static struct membuf_dev* pMembufDev = NULL;

static int membuf_open(struct inode* inode, struct file* filp);
static int membuf_release(struct inode* inode, struct file* filp);

static ssize_t membuf_read(struct file* filp, char __user *buf, size_t count, loff_t* f_pos);
static ssize_t membuf_write(struct file* filp, const char __user *buf, size_t count, loff_t* f_pos);

static struct file_operations membuf_ops = {
	.owner = THIS_MODULE,
	.open = membuf_open,
	.release = membuf_release,
	.read = membuf_read,
	.write = membuf_write
};

static int membuf_open(struct inode* inode, struct file* filp) {
	struct membuf_dev* pMembufDev;

	pMembufDev = container_of(inode->i_cdev, struct membuf_dev, dev);
	filp->private_data = pMembufDev;

	return 0;
}

static int membuf_release(struct inode* inode, struct file* filp) {
	return 0;
}

static ssize_t membuf_read(struct file* filp, char __user *buf, size_t count, loff_t* f_pos) {
	ssize_t err = 0;
	size_t readCnt = 0;
	struct membuf_dev* pMembufDev = filp->private_data;

	if (down_interruptible(&(pMembufDev->sema))) {
		return -ERESTARTSYS;
	}

	/* 计算需要复制多少内容 */
	readCnt = pMembufDev->wroteLen - filp->f_pos;
	if (readCnt > count) readCnt = count;
	printk(KERN_INFO "want read %d, but can read %d", count, readCnt);

	/* 没数据可读，直接返回 */
	if (0 == readCnt) {
		goto out;
	}

	if (copy_to_user(buf, pMembufDev->pBuffer + filp->f_pos, readCnt)) {
		err = -EFAULT;
		goto out;
	}

	err = readCnt;
	*f_pos += readCnt;

out:
	up(&(pMembufDev->sema));
	return err;
}

static ssize_t membuf_write(struct file* filp, const char __user *buf, size_t count, loff_t* f_pos) {
	ssize_t err = 0;
	size_t writeCnt = 0;
	struct membuf_dev* pMembufDev = filp->private_data;

	if (down_interruptible(&(pMembufDev->sema))) {
		return -ERESTARTSYS;
	}

	writeCnt = pMembufDev->bufLen - pMembufDev->wroteLen;
	if (writeCnt > count) writeCnt = count;

	printk(KERN_INFO "membuff_write want: %d can write: %d", count, writeCnt);

	if (0 == writeCnt && 0 != count) {
		err = -ENOSPC;
		goto out;
	}

	if (copy_from_user(pMembufDev->pBuffer + pMembufDev->wroteLen, buf, writeCnt)) {
		err = -EFAULT;
		goto out;
	}

	pMembufDev->wroteLen += writeCnt;
	err = writeCnt;
	*f_pos += writeCnt;

out:
	up(&(pMembufDev->sema));
	return err;
}

static int __init membuff_init(void) {
	int err = -1;
	dev_t dev = 0, devno;
	struct device* temp = NULL;

	printk(KERN_INFO "membuff_init");

	/* 分配设备号 */
	err = alloc_chrdev_region(&dev, 0, 1, MEMORY_BUFF_NODE_NAME);
	if (err < 0) {
		printk(KERN_ALERT"Failed to alloc char dev region.\n");
		goto fail;
	}

	devMajor = MAJOR(dev);
	devMinor = MINOR(dev);

	pMembufDev = kmalloc(sizeof(struct membuf_dev), GFP_KERNEL);
	if (!pMembufDev) {
		err = -ENOMEM;
		printk(KERN_ALERT"Failed to alloc memory for device");
		goto unregister;
	}

	memset(pMembufDev, 0, sizeof(struct membuf_dev));

	pMembufDev->pBuffer = kmalloc(DEVICE_BUFFER_LENGTH, GFP_KERNEL);
	if (!pMembufDev->pBuffer) {
		err = -ENOMEM;
		printk(KERN_ALERT"Failed to alloc memory for device buffer\n");
		goto cleanup;
	}

	pMembufDev->bufLen = DEVICE_BUFFER_LENGTH;
	pMembufDev->wroteLen = 0;

	sema_init(&(pMembufDev->sema), 1);

	cdev_init(&(pMembufDev->dev), &membuf_ops);
	pMembufDev->dev.owner = THIS_MODULE;
	pMembufDev->dev.ops = &membuf_ops;
	devno = MKDEV(devMajor, devMinor);

	err = cdev_add(&(pMembufDev->dev), devno, 1);
	if (err) {
		printk(KERN_ALERT"Failed to add char device.\n");
		goto cleanup;
	}

	pMembufClass = class_create(THIS_MODULE, MEMORY_BUFF_CLASS_NAME);
	if (IS_ERR(pMembufClass)) {
		err = PTR_ERR(pMembufClass);
		printk(KERN_ALERT "Failed to create membuf class.\n");
		goto destroy_cdev;
	}

	temp = device_create(pMembufClass, NULL, devno, "%s", MEMORY_BUFF_FILE_NAME);
	if (IS_ERR(temp)) {
		err = PTR_ERR(temp);
		printk(KERN_ALERT "Failed to create membuff device %d.", err);
		goto destroy_class;
	}

	printk(KERN_INFO "membuff_init success");
	return 0;

destroy_class:
	class_destroy(pMembufClass);

destroy_cdev:
	cdev_del(&(pMembufDev->dev));

cleanup:
	printk(KERN_INFO "membuff_init cleanup");
	kfree(pMembufDev);

unregister:
	printk(KERN_INFO "membuff_init unregister");
	unregister_chrdev_region(MKDEV(devMajor, devMinor), 1);

fail:	
	printk(KERN_INFO "membuff_init failed");
	return err;
}

static void __exit membuff_exit(void) {
	dev_t devno = MKDEV(devMajor, devMinor);

	if (pMembufClass) {
		device_destroy(pMembufClass, devno);
		class_destroy(pMembufClass);
	}

	if (pMembufDev) {
		cdev_del(&(pMembufDev->dev));
		kfree(pMembufDev->pBuffer);
		kfree(pMembufDev);
	}

	unregister_chrdev_region(devno, 1);
}

MODULE_LICENSE("GPL");
MODULE_DESCRIPTION("Memory Buffer Driver");

module_init(membuff_init);
module_exit(membuff_exit);
