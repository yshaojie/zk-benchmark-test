package com.self;

/**
 * Created by   shaojieyue
 * Created date 2015-08-12 09:59
 */
public class ZKSetDataBenchmarkTest {
    public static void main(String[] args) throws Exception {
        String zkString = args[0];
        int dataLen = Integer.valueOf(args[1]);
        int threads = Integer.valueOf(args[2]);
        int count = Integer.valueOf(args[3]);

        ZKConnBenchmark zkConnBenchmark = new ZKConnBenchmark(zkString,threads);
        zkConnBenchmark.setData(dataLen,count);
        System.out.println("work end");
//        zkConnBenchmark.watch(1,5*1000L);
        Thread.sleep(10000000L);
    }
}
