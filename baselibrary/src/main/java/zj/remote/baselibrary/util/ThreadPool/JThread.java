package zj.remote.baselibrary.util.ThreadPool;

/**
 * Created by Joker on 2015/3/7.
 */
public class JThread extends Thread {
    //线程池
    private ThreadPool threadPool;
    //任务
    private Runnable target;
    private boolean isShutDown = false;
    private boolean isIdle = false;

    public JThread(Runnable target, String name, ThreadPool threadPool) {
        super(name);
        this.target = target;
        this.threadPool = threadPool;
    }

    public Runnable getTarget() {
        return target;
    }

    public boolean isIdle() {
        return isIdle;
    }

    @Override
    public void run() {
        //只要没有关闭，则一直不结束该线程
        while (!isShutDown) {
            isIdle = false;
            if (null != target) {
                //执行任务，注意这里调用的是run()，而不是start()
                target.run();
            }
            //任务结束，闲置状态
            isIdle = true;
            try {
                threadPool.repool(JThread.this);
                synchronized (this) {
                    //线程空闲，等待新的任务到来
                    wait();
                }
            } catch (InterruptedException e) {
            }
            isIdle = false;
        }
    }

    public synchronized void setTarget(Runnable target) {
        this.isShutDown = false;
        this.target = target;
        //设置任务之后，通知run方法，开始执行任务
        notifyAll();
    }

    /**
     * 关闭线程
     */
    public synchronized void shutDown() {
        this.isShutDown = true;
        notifyAll();
    }
}
