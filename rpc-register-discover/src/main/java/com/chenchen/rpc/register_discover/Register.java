package com.chenchen.rpc.register_discover;

import com.chenchen.rpc.common.Constant;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 到zk上进行服务注册，将服务器中的socket的地址服务地址注册到zk节点中
 */
public class Register {
    private String zkAddress;
    private CountDownLatch latch = new CountDownLatch(1); // 运用同步所的机制，实现zk连接完毕后才进行祖册

    public Register(String zkAddress) {
        //zookeeper的地址
        this.zkAddress = zkAddress;
    }


    // 进行服务注册
    public void register(String data) {
        if(data != null) {
            ZooKeeper zk = getZKConnect();

            if(zk != null) { // 一定可以拿到zk
                // 进行注册
                createNode(zk, data);
            }
        }


    }

    /**
     * 服务注册
     * @param zk
     * @param
     */
    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            // 查看是否已经存在节点
            if(zk.exists(Constant.ZK_REGISTRY_PATH, false) == null) { // 没有根节点
                // 创建根节点,一个持久化的根节点
                zk.create(Constant.ZK_REGISTRY_PATH, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            // 创建带序列的瞬时节点，值为服务地址和端口，当服务挂掉了，这个节点会直接挂掉，然后通知其他客户端
            zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到zk的连接
     * @return
     */
    public ZooKeeper getZKConnect(){
        ZooKeeper zk = null;
        try {
             zk = new ZooKeeper(zkAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getState() == Event.KeeperState.SyncConnected) { // 表明已经连接上了
                        latch.countDown();
                    }
                }
            });
            // 当数字为0的时候锁释放
            latch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 代表一定已经拿到连接
        return zk;
    }
}
