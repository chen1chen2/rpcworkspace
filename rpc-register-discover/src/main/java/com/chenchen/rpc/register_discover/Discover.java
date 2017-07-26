package com.chenchen.rpc.register_discover;

import com.chenchen.rpc.common.Constant;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务发现类
 */
public class Discover {
    // 运用同步锁保证zk完全连接
    private CountDownLatch latch = new CountDownLatch(1);

    // socket服务器列表, volatile 表示任何时候在堆中只有一个内存，表明是同步的
    private volatile List<String> servlerList = new ArrayList<String>();

    private String zkAddress;

    // 构造的时候进行连接和服务发现
    public Discover(String zkAddress) {
        this.zkAddress = zkAddress;
        ZooKeeper zk = getZKConnect();
        if(zk != null) { // 确保正真有zk,而且已经连接上
            discover(zk);
        }
    }

    // 找到一个可用的地址,随机的，可以通过zk节点中保存的值进行，负载均衡等操作
    public String getServerAddress() {
        int size = servlerList.size();
        if(size > 0) { // 有值
            if(size == 1) {
                return servlerList.get(0);
            } else {
               return servlerList.get(ThreadLocalRandom.current().nextInt(size));
            }
        }
        return null;
    }

    public ZooKeeper getZKConnect() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(zkAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        latch.countDown();
                    }
                }
            });
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return zk;
    }

    /**
     * 发现新的节点，并且监听节点，然后进行节点list的更新
     * @param zk
     */
    public void discover(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    // 节点改变, 当节点改变后，继续递归，再次发现服务，更新列表
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        discover(zk);
                    }
                }
            });
            // 临时存放节点
            List<String> servlerList = new ArrayList<String>();

            for(String node : nodeList) {
                // 获取节点中的服务器地址
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/"
                        + node, false, null);
                servlerList.add(new String(bytes));
            }
            // 刷新服务列表
            this.servlerList = servlerList;

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
