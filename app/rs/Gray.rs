
#pragma version(1)
#pragma rs java_package_name(com.winomtech.androidmisc.rs)
//#pragma rs_fp_relaxed

static int gWidth;
static int gHeight;
static int gPos;

void root(const uchar4 *in, uchar4 *out, uint32_t x, uint32_t y) {
    out->a = 0xff;
    if (x > gPos) {
        out->argb = in->argb;
    } else {
        out->r = out->g = out->b = (in->r + in->g + in->b) / 3;
    }
}

void init() {
}

void setPos(int pos) {
    gPos = pos;
}

void setSize(int w, int h) {
    gWidth = w;
    gHeight = h;
}
