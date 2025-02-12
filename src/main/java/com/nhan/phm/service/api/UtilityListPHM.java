package com.nhan.phm.service.api;
/**
 * @source: [SPMF library open-source API](http://www.philippe-fournier-viger.com/spmf/)
 * @license: GNU GPL version 3
 * @apiNote: this code is modify by @thnhan1 for academic purpose
 **/

import java.util.ArrayList;
import java.util.List;

public class UtilityListPHM {
    Integer item;
    long sumIutils = 0L;
    long sumRutils = 0L;
    List<Element> elements = new ArrayList<>();
    int largestPeriodicity = 0;
    int smallestPeriodicity = Integer.MAX_VALUE;

    public int getLargestPeriodicity() {
        return this.largestPeriodicity;
    }

    public UtilityListPHM(Integer item) {
        this.item = item;
    }

    public void addElement(Element element) {
        this.sumIutils += (long) element.iutils;
        this.sumRutils += (long) element.rutils;
        this.elements.add(element);
    }

    public int getSupport() {
        return this.elements.size();
    }
}
