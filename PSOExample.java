import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class PSOExample {
    public static void main(String[] args) {
        int dimension = 24;
        int swarmSize = 72; // 说来奇怪，对于24维的rosenbrock，粒子数1024表现很好
        int maxIter = 2048;

        // 定义目标函数。注意，这个只是一个二维函数！
        // https://zhuanlan.zhihu.com/p/564819718
        Objective demoFunc = new Objective() {
            public double xmin() { return -4; }
            public double xmax() { return 4; }
            public double function(double[] x) {
                // x[0] = x, x[1] = y
                double x0 = x[0];
                double y0 = x[1];
                return 3 * Math.cos(x0 * y0) + x0 + y0 * y0;
            }
        };
        Objective rastrigin = new Objective() {
            public double xmin() { return -5.12; }
            public double xmax() { return 5.12; }
            public double function(double[] x) {
                double sum = 10 * x.length;
                for (int i = 0; i < x.length; i++) {
                    sum += x[i] * x[i] - 10 * Math.cos(2 * Math.PI * x[i]);
                }
                return sum;
            }
        };
        Objective rosenbrock = new Objective() {
            public double xmin() { return -5; }
            public double xmax() { return 10; }
            public double function(double[] x) {
                double sum = 0.0;
                for (int i = 0; i < x.length - 1; i++) {
                    sum += 100 * Math.pow(x[i + 1] - x[i] * x[i], 2) + Math.pow(x[i] - 1, 2);
                }
                return sum;
            }
        };

        Objective objFunc = rastrigin;

        // 创建全局最优对象
        Global global = new Global(dimension);

        // 创建粒子群
        Particle[] swarm = new Particle[swarmSize];
        for (int i = 0; i < swarmSize; i++) {
            swarm[i] = new Particle(dimension, 1.5, 1.5, 0.9, objFunc, global);
        }

        if (false) {
            // 迭代优化
            for (int iter = 0; iter < maxIter; iter++) {
                for (Particle p : swarm) {
                    p.run();
                }
                // 可选：输出当前迭代的全局最优
                System.out.printf("Iter %d: gbest = [%.4f, %.4f], gvalue = %.6f%n",
                    iter, global.gbest[0], global.gbest[1], global.gvalue);
            }
        }
        else if (false) {
            // 并行迭代优化
            for (int iter = 0; iter < maxIter; iter++) {
                Thread[] threads = new Thread[swarmSize];
                for (int i = 0; i < swarmSize; i++) {
                    threads[i] = new Thread(swarm[i]);
                    threads[i].start();
                }
                // 等待所有线程完成
                for (int i = 0; i < swarmSize; i++) {
                    try {
                        threads[i].join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                // 可选：输出当前迭代的全局最优
                System.out.printf("Iter %d: gbest = [%.4f, %.4f], gvalue = %.6f%n",
                    iter, global.gbest[0], global.gbest[1], global.gvalue);
            }
        }
        else {
            // 使用线程池并发执行
            ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int iter = 0; iter < maxIter; iter++) {
                for (Particle p : swarm) {
                    pool.execute(p); // 并发执行每个粒子的run方法
                }
                pool.shutdown();
                while (!pool.isTerminated()) {
                    // 等待所有粒子本轮更新完成
                }
                // 输出当前迭代全局最优
                System.out.printf("Iter %d: gbest = [%.4f, %.4f], gvalue = %.6f%n",
                    iter, global.gbest[0], global.gbest[1], global.gvalue);

                // [ ]: 是否需要重新开启线程池用于下一轮？
                if (iter < maxIter - 1) pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            }
        }

        // 输出最终结果
        System.out.println("最终全局最优位置: ");
        for (double v : global.gbest) System.out.printf("%.6f ", v);
        System.out.println("\n最终全局最优值: " + global.gvalue);
    }
}

class Particle implements Runnable {
    public int dimension = 2; //维数

    public double[] xmin;
    public double[] xmax;

    public double[] position; //位置
    public double[] pvelocity; //速度
    public double[] pbest; //个体最优位置
    public double pvalue = Double.MAX_VALUE; //个体最优适应值

    final public Objective objFunc;
    final public Global global;
    public double c1 = 1.5; //学习因子
    public double c2 = 1.5; //学习因子
    public double w = 0.9; //惯性权重
    // [ ]: 建议：增加速度上下限属性

    public Particle(int dimension, double c1, double c2, double w, Objective objFunc, Global global) {
        this.dimension = dimension;
        this.global = global;
        this.objFunc = objFunc;

        this.xmin = new double[dimension];
        this.xmax = new double[dimension];
        this.position = new double[dimension];
        this.pvelocity = new double[dimension];
        this.pbest = new double[dimension];
        this.c1 = c1;
        this.c2 = c2;
        this.w = w;
        
        for (int i = 0; i < dimension; i++) {
            this.xmin[i] = objFunc.xmin();
            this.xmax[i] = objFunc.xmax();
            this.position[i] = rand() * (xmax[i] - xmin[i]) + xmin[i];
            this.pvelocity[i] = rand() * 2 - 1;
            this.pbest[i] = this.position[i];
        }
        this.pvalue = eval();
    }

    public static double rand() {
        // 使用 ThreadLocalRandom 避免每次 new Random()
        return ThreadLocalRandom.current().nextDouble();
    }

    //计算适应值
    public double eval() {
        return objFunc.function(this.position);
    }

    public void run() {

        // 取一份个体最优的本地副本，避免长时间持锁（synchronized意义是给变量加互斥锁）
        double[] copy_global_gbest = new double[this.dimension];
        synchronized (this.global) {
            System.arraycopy(this.global.gbest, 0, copy_global_gbest, 0, this.dimension);
        }
        
        // [x]: 特性：加上惯性权重。
        // 根据全体信息和个体信息进行的速度更新
        for (int i = 0; i < this.dimension; i++) {
            double rand1 = rand(); // [x]: 特性：按维度生成随机数
            double rand2 = rand();
            this.pvelocity[i] = this.w * this.pvelocity[i] + 
                                this.c2 * rand2 * (copy_global_gbest[i] - this.position[i]) + 
                                this.c1 * rand1 * (this.pbest[i] - this.position[i]);
        }

        // 位置更新
        for (int i = 0; i < this.dimension; i++) {
            this.position[i] += this.pvelocity[i];
            // 边界限制
            if (this.position[i] < this.xmin[i]) this.position[i] = this.xmin[i];
            if (this.position[i] > this.xmax[i]) this.position[i] = this.xmax[i];
        }

        // 个体最优位置和个体最优适应值更新
        double tmp_value = eval();
        if (tmp_value < this.pvalue) {
            this.pvalue = tmp_value;
            System.arraycopy(this.position, 0, this.pbest, 0, this.dimension);
        }
        
        // [x]: 特性：单个粒子信息异步更新
        synchronized (this.global) {
            // 全体最优位置和全体最优适应值更新
            if (tmp_value < this.global.gvalue) {
                this.global.gvalue = tmp_value;
                System.arraycopy(this.position, 0, this.global.gbest, 0, this.dimension);
            }
        }
    }
}

class Global {
    public double[] gbest;//全局最优位置
    public double gvalue = Double.MAX_VALUE;//全局最优适应值
    
    // 新增构造器，确保 gbest 被正确分配
    public Global(int dimension) {
        this.gbest = new double[dimension];
    }
}

interface Objective {
    //目标函数
    public double xmin();
    public double xmax();
    public double function(double[] x);
}
