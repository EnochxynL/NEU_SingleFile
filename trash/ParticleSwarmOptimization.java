import java.util.Random;

class Particle {
    
    public int dimension = 2; //维数
    
    public double[] position = new double[dimension]; //位置
    public double[] velocity = new double[dimension]; //速度
    public double[] pbest = new double[dimension]; //个体最优位置
    public double currentFitnessValue = Double.MAX_VALUE; //适应值
    public double pbestFitnessValue = Double.MAX_VALUE; //个体最优适应值

    public Particle() {
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            position[i] = random.nextDouble() * 30 - 15;//[-15,15]
            velocity[i] = random.nextDouble() * 2 - 1;//[-1,1]
            pbest[i] = position[i];
        }
        currentFitnessValue = getFitnessValue(position);
        pbestFitnessValue = currentFitnessValue;
    }

    //计算适应值
    public double getFitnessValue(double[] position) {
        //这里以Rosenbrock函数为例，最小值为0，位置在(1,1)
        double x = position[0];
        double y = position[1];
        return Math.pow((1 - x), 2) + 100 * Math.pow((y - x * x), 2);
    }

    public void run() {
        // 位置更新
        for (int i = 0; i < dimension; i++) {
            position[i] += velocity[i];
        }
        // 适应值更新
        currentFitnessValue = getFitnessValue(position);
        // 个体最优位置更新
        if (currentFitnessValue < pbestFitnessValue) {
            pbestFitnessValue = currentFitnessValue;
            System.arraycopy(position, 0, pbest, 0, dimension);
        }
    }

    public void updateVelocity(double[] gbest) {
        double w = 0.5; //惯性权重
        double c1 = 1.5; //学习因子
        double c2 = 1.5; //学习因子
        Random random = new Random();
        for (int i = 0; i < dimension; i++) {
            double r1 = random.nextDouble();
            double r2 = random.nextDouble();
            velocity[i] = w * velocity[i] + c1 * r1 * (pbest[i] - position[i]) + c2 * r2 * (gbest[i] - position[i]);
            //速度限制
            if (velocity[i] > 2) velocity[i] = 2;
            if (velocity[i] < -2) velocity[i] = -2;
        }
    }
}


class