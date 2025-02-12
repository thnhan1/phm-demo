package com.nhan.phm.service.api;

/**
 * @source: [SPMF library open-source API](http://www.philippe-fournier-viger.com/spmf/)
 * @license: GNU GPL version 3
 * @apiNote: this code is modify by @thnhan1 for academic purpose
 **/


import java.io.*;
import java.util.*;

public class AlgoPHM {
    final int BUFFERS_SIZE = 200;
    public int phuiCount = 0;
    public int candidateCount = 0;
    public double totalExecutionTime = 0.0F;
    public double maximumMemoryUsage = 0.0F;
    public boolean findingIrregularItemsets = false;
    Map<Integer, Long> mapItemToTWU;
    Map<Integer, AlgoPHM.ItemInfo> mapItemToItemInfo;
    BufferedWriter writer = null;
    Map<Integer, Map<Integer, Long>> mapEUCS = null;
    Map<Integer, Map<Integer, Long>> mapESCS = null;
    boolean ENABLE_LA_PRUNE = true;
    boolean ENABLE_EUCP = true;
    boolean ENABLE_ESCP = true;
    boolean DEBUG = false;
    int databaseSize = 0;
    int minPeriodicity;
    int maxPeriodicity;
    int minAveragePeriodicity;
    int maxAveragePeriodicity;
    int minimumLength = 0;
    int maximumLength = Integer.MAX_VALUE;
    double supportPruningThreshold = 0.0F;
    private int[] itemsetBuffer = null;

    public AlgoPHM() {
    }

    public void runAlgorithm(String input, String output, int minUtility, int minPeriodicity, int maxPeriodicity, int minAveragePeriodicity, int maxAveragePeriodicity) throws IOException {
        MemoryLogger.getInstance().reset();
        long startTimestamp = 0L;
        this.maxPeriodicity = maxPeriodicity;
        this.minPeriodicity = minPeriodicity;
        this.minAveragePeriodicity = minAveragePeriodicity;
        this.maxAveragePeriodicity = maxAveragePeriodicity;
        this.itemsetBuffer = new int[200];
        if (this.ENABLE_EUCP) {
            this.mapEUCS = new HashMap<>();
        }

        if (this.ENABLE_ESCP) {
            this.mapESCS = new HashMap<>();
        }

        startTimestamp = System.currentTimeMillis();
        this.writer = new BufferedWriter(new FileWriter(output));
        this.mapItemToTWU = new HashMap<>();
        this.mapItemToItemInfo = new HashMap<>();
        BufferedReader myInput = null;
        this.databaseSize = 0;
        String thisLine = null;
        long sumOfTransactionLength = 0L;

        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

            while ((thisLine = myInput.readLine()) != null) {
                if (!thisLine.isEmpty() && thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%' && thisLine.charAt(0) != '@') {
                    ++this.databaseSize;
                    String[] split = thisLine.split(":");
                    String[] items = split[0].split(" ");
                    int transactionUtility = Integer.parseInt(split[1]);
                    sumOfTransactionLength += items.length;

                    for (int i = 0; i < items.length; ++i) {
                        Integer item = Integer.parseInt(items[i]);
                        Long twu = this.mapItemToTWU.get(item);
                        twu = twu == null ? (long) transactionUtility : twu + (long) transactionUtility;
                        this.mapItemToTWU.put(item, twu);
                        ItemInfo itemInfo = this.mapItemToItemInfo.get(item);
                        if (itemInfo == null) {
                            itemInfo = new ItemInfo();
                            this.mapItemToItemInfo.put(item, itemInfo);
                        }

                        ++itemInfo.support;
                        int periodicity = this.databaseSize - itemInfo.lastSeenTransaction;
                        if (itemInfo.largestPeriodicity < periodicity) {
                            itemInfo.largestPeriodicity = periodicity;
                        }

                        itemInfo.lastSeenTransaction = this.databaseSize;
                        if (itemInfo.support != 1 && periodicity < itemInfo.smallestPeriodicity) {
                            itemInfo.smallestPeriodicity = periodicity;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }

        }

        this.supportPruningThreshold = (double) this.databaseSize / (double) maxAveragePeriodicity - (double) 1.0F;

        for (Map.Entry<Integer, AlgoPHM.ItemInfo> entry : this.mapItemToItemInfo.entrySet()) {
            AlgoPHM.ItemInfo itemInfo = entry.getValue();
            int periodicity = this.databaseSize - itemInfo.lastSeenTransaction;
            if (itemInfo.largestPeriodicity < periodicity) {
                itemInfo.largestPeriodicity = periodicity;
            }

            if (this.DEBUG) {
                PrintStream var10000 = System.out;
                String var10001 = String.valueOf(entry.getKey());
                var10000.println(" item : " + var10001 + "\tavgPer: " + (double) this.databaseSize / (double) (itemInfo.support + 1) + "\tminPer: " + itemInfo.smallestPeriodicity + "\tmaxPer: " + itemInfo.largestPeriodicity + "\tTWU: " + this.mapItemToTWU.get(entry.getKey()) + "\tsup.: " + itemInfo.support);
            }
        }

        if (this.DEBUG) {
            System.out.println("Number of transactions : " + this.databaseSize);
            System.out.println("Average transaction length : " + (double) sumOfTransactionLength / (double) this.databaseSize);
            System.out.println("Number of items : " + this.mapItemToItemInfo.size());
            System.out.println("Average pruning threshold  (|D| / maxAvg $) - 1): " + this.supportPruningThreshold);
        }

        List<UtilityListPHM> listOfUtilityLists = new ArrayList<>();
        Map<Integer, UtilityListPHM> mapItemToUtilityList = new HashMap<>();

        for (Integer item : this.mapItemToTWU.keySet()) {
            AlgoPHM.ItemInfo itemInfo = this.mapItemToItemInfo.get(item);
            if ((double) itemInfo.support >= this.supportPruningThreshold && itemInfo.largestPeriodicity <= maxPeriodicity && this.mapItemToTWU.get(item) >= (long) minUtility) {
                UtilityListPHM uList = new UtilityListPHM(item);
                mapItemToUtilityList.put(item, uList);
                listOfUtilityLists.add(uList);
                uList.largestPeriodicity = itemInfo.largestPeriodicity;
                uList.smallestPeriodicity = itemInfo.smallestPeriodicity;
            }
        }

        listOfUtilityLists.sort(new Comparator<UtilityListPHM>() {
            public int compare(UtilityListPHM o1, UtilityListPHM o2) {
                return AlgoPHM.this.compareItems(o1.item, o2.item);
            }
        });

        try {
            myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));
            int tid = 0;

            while ((thisLine = myInput.readLine()) != null) {
                if (!thisLine.isEmpty() && thisLine.charAt(0) != '#' && thisLine.charAt(0) != '%' && thisLine.charAt(0) != '@') {
                    String[] split = thisLine.split(":");
                    String[] items = split[0].split(" ");
                    String[] utilityValues = split[2].split(" ");
                    int remainingUtility = 0;
                    long newTWU = 0L;
                    List<AlgoPHM.Pair> revisedTransaction = new ArrayList<>();

                    for (int i = 0; i < items.length; ++i) {
                        AlgoPHM.Pair pair = new AlgoPHM.Pair();
                        pair.item = Integer.parseInt(items[i]);
                        pair.utility = Integer.parseInt(utilityValues[i]);
                        AlgoPHM.ItemInfo itemInfo = this.mapItemToItemInfo.get(pair.item);
                        if ((double) itemInfo.support >= this.supportPruningThreshold && itemInfo.largestPeriodicity <= maxPeriodicity && this.mapItemToTWU.get(pair.item) >= (long) minUtility) {
                            revisedTransaction.add(pair);
                            remainingUtility += pair.utility;
                            newTWU += pair.utility;
                        }
                    }

                    Collections.sort(revisedTransaction, new Comparator<AlgoPHM.Pair>() {
                        public int compare(AlgoPHM.Pair o1, AlgoPHM.Pair o2) {
                            return AlgoPHM.this.compareItems(o1.item, o2.item);
                        }
                    });

                    for (int i = 0; i < revisedTransaction.size(); ++i) {
                        AlgoPHM.Pair pair = revisedTransaction.get(i);
                        remainingUtility -= pair.utility;
                        UtilityListPHM utilityListOfItem = mapItemToUtilityList.get(pair.item);
                        Element element = new Element(tid, pair.utility, remainingUtility);
                        utilityListOfItem.addElement(element);
                        if (this.ENABLE_EUCP) {
                            Map<Integer, Long> mapFMAPItem = this.mapEUCS.computeIfAbsent(pair.item, k -> new HashMap());

                            for (int j = i + 1; j < revisedTransaction.size(); ++j) {
                                AlgoPHM.Pair pairAfter = revisedTransaction.get(j);
                                Long twuSum = mapFMAPItem.get(pairAfter.item);
                                if (twuSum == null) {
                                    mapFMAPItem.put(pairAfter.item, newTWU);
                                } else {
                                    mapFMAPItem.put(pairAfter.item, twuSum + newTWU);
                                }
                            }
                        }

                        if (this.ENABLE_ESCP) {
                            Map<Integer, Long> mapESItem = this.mapESCS.computeIfAbsent(pair.item, k -> new HashMap());

                            for (int j = i + 1; j < revisedTransaction.size(); ++j) {
                                AlgoPHM.Pair pairAfter = revisedTransaction.get(j);
                                Long support = mapESItem.get(pairAfter.item);
                                if (support == null) {
                                    mapESItem.put(pairAfter.item, 1L);
                                } else {
                                    mapESItem.put(pairAfter.item, support + 1L);
                                }
                            }
                        }
                    }

                    ++tid;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (myInput != null) {
                myInput.close();
            }

        }

        this.mapItemToItemInfo = null;
        this.mapItemToTWU = null;
        mapItemToUtilityList = null;
        MemoryLogger.getInstance().checkMemory();
        this.phm(this.itemsetBuffer, 0, null, listOfUtilityLists, minUtility);
        MemoryLogger.getInstance().checkMemory();
        this.writer.close();
        this.totalExecutionTime = (double) (System.currentTimeMillis() - startTimestamp);
        this.maximumMemoryUsage = MemoryLogger.getInstance().getMaxMemory();
    }

    public void runAlgorithmIrregular(String input, String output, int minUtility, int regularityThreshold) throws IOException {
        this.findingIrregularItemsets = true;
        this.setEnableESCP(false);
        this.runAlgorithm(input, output, minUtility, regularityThreshold, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
    }

    private int compareItems(int item1, int item2) {
        int compare = (int) (this.mapItemToTWU.get(item1) - this.mapItemToTWU.get(item2));
        return compare == 0 ? item1 - item2 : compare;
    }

    private void phm(int[] prefix, int prefixLength, UtilityListPHM pUL, List<UtilityListPHM> ULs, int minUtility) throws IOException {
        int patternSize = prefixLength + 1;

        for (int i = 0; i < ULs.size(); ++i) {
            UtilityListPHM X = ULs.get(i);
            if (X.sumIutils + X.sumRutils >= (long) minUtility) {
                double averagePeriodicity = (double) this.databaseSize / ((double) X.getSupport() + (double) 1.0F);
                if (X.sumIutils >= (long) minUtility && averagePeriodicity <= (double) this.maxAveragePeriodicity && averagePeriodicity >= (double) this.minAveragePeriodicity && X.smallestPeriodicity >= this.minPeriodicity && X.largestPeriodicity <= this.maxPeriodicity && patternSize >= this.minimumLength && patternSize <= this.maximumLength) {
                    this.writeOut(prefix, prefixLength, X, averagePeriodicity);
                }

                if (patternSize < this.maximumLength) {
                    List<UtilityListPHM> exULs = new ArrayList<>();

                    for (int j = i + 1; j < ULs.size(); ++j) {
                        UtilityListPHM Y = ULs.get(j);
                        if (this.ENABLE_EUCP) {
                            Map<Integer, Long> mapTWUF = this.mapEUCS.get(X.item);
                            if (mapTWUF != null) {
                                Long twuF = mapTWUF.get(Y.item);
                                if (twuF == null || twuF < (long) minUtility) {
                                    continue;
                                }
                            }
                        }

                        if (this.ENABLE_ESCP) {
                            Map<Integer, Long> mapSUPF = this.mapESCS.get(X.item);
                            if (mapSUPF != null) {
                                Long supportF = mapSUPF.get(Y.item);
                                if (supportF != null && (double) supportF < this.supportPruningThreshold) {
                                    continue;
                                }
                            }
                        }

                        ++this.candidateCount;
                        UtilityListPHM temp = this.construct(pUL, X, Y, minUtility);
                        if (temp != null) {
                            exULs.add(temp);
                        }
                    }

                    this.itemsetBuffer[prefixLength] = X.item;
                    this.phm(this.itemsetBuffer, prefixLength + 1, X, exULs, minUtility);
                }
            }
        }

        MemoryLogger.getInstance().checkMemory();
    }

    private UtilityListPHM construct(UtilityListPHM P, UtilityListPHM px, UtilityListPHM py, int minUtility) {
        UtilityListPHM pxyUL = new UtilityListPHM(py.item);
        int lastTid = -1;
        long totalUtility = px.sumIutils + px.sumRutils;
        long totalSupport = px.getSupport();

        for (Element ex : px.elements) {
            Element ey = this.findElementWithTID(py, ex.tid);
            if (ey == null) {
                if (this.ENABLE_LA_PRUNE) {
                    totalUtility -= ex.iutils + ex.rutils;
                    if (totalUtility < (long) minUtility) {
                        return null;
                    }

                    --totalSupport;
                    if ((double) totalSupport < this.supportPruningThreshold) {
                        return null;
                    }
                }
            } else if (P == null) {
                int periodicity = ex.tid - lastTid;
                if (periodicity > this.maxPeriodicity) {
                    return null;
                }

                if (periodicity >= pxyUL.largestPeriodicity) {
                    pxyUL.largestPeriodicity = periodicity;
                }

                lastTid = ex.tid;
                if (pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity) {
                    pxyUL.smallestPeriodicity = periodicity;
                }

                Element eXY = new Element(ex.tid, ex.iutils + ey.iutils, ey.rutils);
                pxyUL.addElement(eXY);
            } else {
                Element e = this.findElementWithTID(P, ex.tid);
                if (e != null) {
                    int periodicity = ex.tid - lastTid;
                    if (periodicity > this.maxPeriodicity) {
                        return null;
                    }

                    if (periodicity >= pxyUL.largestPeriodicity) {
                        pxyUL.largestPeriodicity = periodicity;
                    }

                    lastTid = ex.tid;
                    if (pxyUL.elements.size() > 0 && periodicity < pxyUL.smallestPeriodicity) {
                        pxyUL.smallestPeriodicity = periodicity;
                    }

                    Element eXY = new Element(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rutils);
                    pxyUL.addElement(eXY);
                }
            }
        }

        int periodicity = this.databaseSize - 1 - lastTid;
        if (periodicity > this.maxPeriodicity) {
            return null;
        } else {
            if (periodicity >= pxyUL.largestPeriodicity) {
                pxyUL.largestPeriodicity = periodicity;
            }

            if ((double) pxyUL.getSupport() < this.supportPruningThreshold) {
                return null;
            } else {
                return pxyUL;
            }
        }
    }

    private Element findElementWithTID(UtilityListPHM ulist, int tid) {
        List<Element> list = ulist.elements;
        int first = 0;
        int last = list.size() - 1;

        while (first <= last) {
            int middle = first + last >>> 1;
            if (list.get(middle).tid < tid) {
                first = middle + 1;
            } else {
                if (list.get(middle).tid <= tid) {
                    return list.get(middle);
                }

                last = middle - 1;
            }
        }

        return null;
    }

    private void writeOut(int[] prefix, int prefixLength, UtilityListPHM utilityList, double averagePeriodicity) throws IOException {
        ++this.phuiCount;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < prefixLength; ++i) {
            buffer.append(prefix[i]);
            buffer.append(' ');
        }

        buffer.append(utilityList.item);
        buffer.append(" #UTIL: ");
        buffer.append(utilityList.sumIutils);
        if (this.findingIrregularItemsets) {
            buffer.append(" #REG: ");
            buffer.append(utilityList.largestPeriodicity);
        } else {
            buffer.append(" #SUP: ");
            buffer.append(utilityList.getSupport());
            buffer.append(" #MINPER: ");
            buffer.append(utilityList.smallestPeriodicity);
            buffer.append(" #MAXPER: ");
            buffer.append(utilityList.largestPeriodicity);
            buffer.append(" #AVGPER: ");
            buffer.append(averagePeriodicity);
        }

        this.writer.write(buffer.toString());
        this.writer.newLine();
    }

    public void printStats() throws IOException {
        if (this.DEBUG && this.ENABLE_EUCP) {
            System.out.println("===== CONTENT OF EUCP =====");

            for (Map.Entry<Integer, Map<Integer, Long>> entry : this.mapEUCS.entrySet()) {
                System.out.print("Item:" + entry.getKey() + " -- ");

                for (Map.Entry<Integer, Long> entry2 : (entry.getValue()).entrySet()) {
                    PrintStream var10000 = System.out;
                    String var10001 = String.valueOf(entry2.getKey());
                    var10000.print(var10001 + " (" + entry2.getValue() + ")  ");
                }

                System.out.println();
            }
        }

        if (this.DEBUG && this.ENABLE_ESCP) {
            System.out.println("===== CONTENT OF ESCS =====");

            for (Map.Entry<Integer, Map<Integer, Long>> entry : this.mapESCS.entrySet()) {
                System.out.print("Item:" + entry.getKey() + " -- ");

                for (Map.Entry<Integer, Long> entry2 : (entry.getValue()).entrySet()) {
                    PrintStream var26 = System.out;
                    String var27 = String.valueOf(entry2.getKey());
                    var26.print(var27 + " (" + entry2.getValue() + ")  ");
                }

                System.out.println();
            }
        }

        String optimizationEUCP = this.ENABLE_EUCP ? " EUCP: true -" : " EUCP: false -";
        String optimizationESCP = this.ENABLE_ESCP ? " ESCP: true " : " ESCP: false ";
        String name = "PHM";
        String patternType = "Periodic";
        if (this.findingIrregularItemsets) {
            name = name + "_irregular";
            optimizationESCP = "";
            patternType = "Irregular";
        }

        System.out.println("=============  " + name + " v2.38" + optimizationEUCP + optimizationESCP + "=====");
        System.out.println(" Database size: " + this.databaseSize + " transactions");
        System.out.println(" Time : " + this.totalExecutionTime + " ms");
        System.out.println(" Memory ~ " + this.maximumMemoryUsage + " MB");
        System.out.println(" " + patternType + " High-utility itemsets count : " + this.phuiCount);
        System.out.println(" Candidate count : " + this.candidateCount);
        if (this.DEBUG && this.ENABLE_EUCP) {
            int pairCount = 0;
            double maxMemory = this.getObjectSize(this.mapEUCS);

            for (Map.Entry<Integer, Map<Integer, Long>> entry : this.mapEUCS.entrySet()) {
                maxMemory += this.getObjectSize(entry.getKey());

                for (Map.Entry<Integer, Long> entry2 : (entry.getValue()).entrySet()) {
                    ++pairCount;
                    maxMemory += this.getObjectSize(entry2.getKey()) + this.getObjectSize(entry2.getValue());
                }
            }

            System.out.println("EUCS size " + maxMemory + " MB    PAIR COUNT " + pairCount);
        }

        if (this.DEBUG && this.ENABLE_ESCP) {
            int pairCount = 0;
            double maxMemory = this.getObjectSize(this.mapESCS);

            for (Map.Entry<Integer, Map<Integer, Long>> entry : this.mapESCS.entrySet()) {
                maxMemory += this.getObjectSize(entry.getKey());

                for (Map.Entry<Integer, Long> entry2 : (entry.getValue()).entrySet()) {
                    ++pairCount;
                    maxMemory += this.getObjectSize(entry2.getKey()) + this.getObjectSize(entry2.getValue());
                }
            }

            System.out.println("ESCS size " + maxMemory + " MB    PAIR COUNT " + pairCount);
        }

        System.out.println("===================================================");
    }

    private double getObjectSize(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        double maxMemory = (double) baos.size() / (double) 1024.0F / (double) 1024.0F;
        return maxMemory;
    }

    public void setEnableEUCP(boolean enable) {
        this.ENABLE_EUCP = enable;
    }

    public void setEnableESCP(boolean enable) {
        this.ENABLE_ESCP = enable;
    }

    public void setMinimumLength(int minimumLength) {
        this.minimumLength = minimumLength;
    }

    public void setMaximumLength(int maximumLength) {
        this.maximumLength = maximumLength;
    }

    class Pair {
        int item = 0;
        int utility = 0;

        Pair() {
        }
    }

    class ItemInfo {
        int support = 0;
        int largestPeriodicity = 0;
        int smallestPeriodicity = Integer.MAX_VALUE;
        int lastSeenTransaction = 0;

        ItemInfo() {
        }
    }
}
