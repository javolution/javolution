package org.javolution.util;

import java.util.ArrayList;

public class TestFractal {

    public static void main(String[] args) {
        java.util.Random random = new java.util.Random(11111);
        ArrayList<Integer> aa = new ArrayList<Integer>();
        FastTable<Integer> ff = new FastTable<Integer>();
        long time = System.currentTimeMillis();
        for (int i = 0; i < 10000000; i++) {
            if (random.nextDouble() < 0.5) { // Insertion
                int n = Math.max(aa.size(), ff.size()) + 1;
                int j = (int) (n * random.nextDouble());
                int r = (int) (random.nextDouble() * 1000000);
  //              aa.add(j, r);
                ff.add(j, r);
//                if (!aa.equals(ff)) {
//                    System.out.println("Insertion of " + r + " at " + j);
//                    System.out.println("Expected: " + aa);
//                    System.out.println("Found   : " + ff);
//                    break;
//                } else {
//                    // System.out.println(ff);
//                }
            } else { // Removal
                int n = Math.max(aa.size(), ff.size());
                if (n == 0) continue;
                int j = (int) (n * random.nextDouble());
    //            aa.remove(j);
                ff.remove(j);
//                if (!aa.equals(ff)) {
//                    System.out.println("Removal at " + j);
//                    System.out.println("Expected: " + aa);
//                    System.out.println("Found   : " + ff);
//                    break;
//                } else {
//                    // System.out.println(ff);
//                }
            }
        }
        System.out.println("Size ArrayList: " + aa.size());
        System.out.println("Size FractalTable: " + ff.size());
        System.out.println("Time: " + (System.currentTimeMillis() - time));
    }
}
