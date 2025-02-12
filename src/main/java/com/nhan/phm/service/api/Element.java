package com.nhan.phm.service.api;

/**
 * @source: [SPMF library open-source API](http://www.philippe-fournier-viger.com/spmf/)
 * @license: GNU GPL version 3
 * @apiNote: this code is modify by @thnhan1 for academic purpose
 **/
public class Element {
    public final int tid;
    public final int iutils;
    public int rutils;

    public Element(int tid, int iutils, int rutils) {
        this.tid = tid;
        this.iutils = iutils;
        this.rutils = rutils;
    }
}
