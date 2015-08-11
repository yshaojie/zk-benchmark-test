package com.self;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.data.Stat;

import javax.sound.midi.Soundbank;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Created by   shaojieyue
 * Created date 2015-08-09 18:24
 */
public class ZKConnBenchmark {
    //缓存连接
    private List<CuratorFramework> connections = null;

    private String zkString = null;

    public ZKConnBenchmark(String zkString) {
        this.zkString = zkString;
    }

    private CuratorFramework createCuratorFrameworkConn() {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                                    .namespace("zktest")
                                    .connectString(zkString)
                                    .retryPolicy(new RetryNTimes(2, 1000))
                                    .build();
        client.start();
        return client;
    }

    public final void connections(int count){
        if (count<1) {
            throw new IllegalArgumentException("count="+count+" param error.");
        }
        connections = new ArrayList(count);
        long start = System.currentTimeMillis();
        for (int i = 1; i <= count; i++) {
            final CuratorFramework curatorFrameworkConn = createCuratorFrameworkConn();
            if (curatorFrameworkConn.getState() != CuratorFrameworkState.STARTED) {
                System.out.println("i="+i+" connection fail.");
            }else {
//                System.out.println("i="+i+" success.");
                connections.add(curatorFrameworkConn);
            }
            if (i%500==0) {
                System.out.println("connections count="+i);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("connections test count="+count+" cost time ="+(end-start)+" average time="+((end-start)/count));
    }

    private byte[] createData(int length){
        //length kb 的数据
        byte[] data = new byte[1024*length];
        for (int i = 0; i < data.length; i++) {
            data[i]='a';
        }
        return data;
    }

    public void watch(int dataLen,long sleepms) throws Exception {
        String path = "/node1";
        final List<Long> tims = new CopyOnWriteArrayList<Long>();
        System.out.println("start init zk watch....");
        System.out.println("need init zk conn count=" + connections.size());
        for (int i = 1; i <= 1; i++) {
            path="/node"+i;
            doWatch(path, tims);
        }

        System.out.println("start zk watch end....");

        //设置数据
        String data = System.currentTimeMillis()+new String(createData(dataLen));
        connections.get(0).setData().forPath(path, data.getBytes());
        Thread.sleep(sleepms);
        List<Long> ss = new ArrayList<Long>(tims.size());
        long all = 0;
        for (Long s : tims) {
            all = all + s;
            ss.add(s);
        }
        System.out.println("get wath in " + sleepms + "ms count=" + tims.size()+"  average time="+(all/tims.size()));
        System.out.println("wath test end.............");
//        Collections.sort(ss, new Comparator<Long>() {
//            public int compare(Long o1, Long o2) {
//                return (int) (o2.longValue() - o1.longValue());
//            }
//        });
//        for (Long tim : ss) {
//            System.out.println(tim);
//        }

    }

    private void doWatch(String path, final List<Long> tims) throws Exception {
        System.out.println("start watch path="+path);
        final AtomicInteger counter = new AtomicInteger();
        //初始化监听
        for (final CuratorFramework connection : connections) {
            connection.getData().usingWatcher(new CuratorWatcher() {
                public void process(final WatchedEvent watchedEvent) throws Exception {
                    new Thread(new Runnable() {
                        public void run() {
                            long current = System.currentTimeMillis();
                            final byte[] bytes;
                            try {
                                bytes = connection.getData().forPath(watchedEvent.getPath());
                                long setTime = Long.valueOf(new String(bytes).substring(0,13)) ;
                                final long ts = current - setTime;
//                                long ts = 0;
                                tims.add(ts);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();


                }
            }).forPath(path);
        }
    }


    public void setDataAndGet(int dataLen, int nodeCount) throws Exception {
        byte[] data = createData(dataLen);
        Random random = new Random();
        int size = connections.size();
        List<String> nodes = new ArrayList<String>(nodeCount);
        long start = System.currentTimeMillis();
        long min = 10000;
        long max = 0;
        long all = 0;
        for (CuratorFramework connection : connections) {
            for (int i = 1; i <= nodeCount; i++) {
                //随机节点
                String path = "/node"+i;
                try {
                    long s1=System.currentTimeMillis();
                    final Stat stat = connection.setData().forPath(path, data);
                    long s2=System.currentTimeMillis();
                    long ts = s2-s1;
                    all = all + ts;
                    if (min>ts) {
                        min = ts;
                    }
                    if(max<ts){
                        max = ts;
                    }

                    nodes.add(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("set data cost min="+min+" max="+max+" average time="+((all))/(connections.size()*nodeCount));
        start = System.currentTimeMillis();
        min = 100000;
        max = 0;
        all = 0;
        for (CuratorFramework connection : connections) {
            for (int i = 1; i <= nodeCount; i++) {
                //随机节点
                String path = "/node"+i;
                long s1=System.currentTimeMillis();
                final byte[] bytes = connection.getData().forPath(path);
                System.out.println(new String(bytes).length());
                long s2=System.currentTimeMillis();
                long ts = s2-s1;
                all = all + ts;
                if (min>ts) {
                    min = ts;
                }
                if(max<ts){
                    max = ts;
                }
            }
        }
        end = System.currentTimeMillis();
        System.out.println("get data cost min="+min+" max="+max+" average time="+((all))/(connections.size()*nodeCount));
        System.out.println("set data success");
    }

}
