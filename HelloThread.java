import java.util.concurrent.TimeUnit;

public class HelloThread {
    public static void main(String[] args) {
        // 创建异步线程
        MyThread asyncThread = new MyThread();

        // 启动异步线程
        asyncThread.start();

        while (true) {
            if (asyncThread.count > 5) {
                asyncThread.interrupt();
                break;
            }
        }

        System.out.println("Hello for 5 seconds");

        while (true) {
            if (asyncThread.count > 10) {
                asyncThread.running = false;
                break;
            }
        }

        System.out.println("Hello World");
    }
}

class MyThread extends Thread{

    public volatile boolean running = true;
    
    public volatile int count = 0;

    @Override
    public void run() {
        System.out.println("Thread Name:" + this.getName() + ", Running Name:" + Thread.currentThread().getName() + "-hello");
        
        while (running) {
            // 执行任务
            System.out.println("Running..." + count++);
            try {
                // 暂停1秒钟
                TimeUnit.SECONDS.sleep(1);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
