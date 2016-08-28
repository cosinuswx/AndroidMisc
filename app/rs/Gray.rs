
#pragma version(1)
#pragma rs java_package_name(com.winomtech.androidmisc.rs)

int gPos;

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

