import java.util.*;
 
/**
 * code by me
 * <p>
 * Data:2017/8/17 Time:16:40
 * User:lbh
 */
public class KNNExample {
 
    /**
     * KNN数据模型
     */
    public static class KNNModel implements Comparable<KNNModel> {
        public double a;
        public double b;
        public double c;
        public double distince;
        String type;
 
        public KNNModel(double a, double b, double c, String type) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.type = type;
        }
        /**
         * 按距离排序
         *
         * @param arg
         * @return
         */
        @Override
        public int compareTo(KNNModel arg) {
            return Double.valueOf(this.distince).compareTo(Double.valueOf(arg.distince));
        }
    }
 
    /**
     * 计算距离
     *
     * @param knnModelList
     * @param i
     */
    private static void calDistince(List<KNNModel> knnModelList, KNNModel i) {
        double distince;
        for (KNNModel m : knnModelList) {
            distince = Math.sqrt((i.a - m.a) * (i.a - m.a) + (i.b - m.b) * (i.b - m.b) + (i.c - m.c) * (i.c - m.c));
            m.distince = distince;
        }
    }
 
    /**
     * 找出前k个数据中分类最多的数据
     *
     * @param knnModelList
     * @return
     */
    private static String findMostData(List<KNNModel> knnModelList) {
        Map<String, Integer> typeCountMap = new HashMap<String, Integer>();
        String type = "";
        Integer tempVal = 0;
        // 统计分类个数
        for (KNNModel model : knnModelList) {
            if (typeCountMap.containsKey(model.type)) {
                typeCountMap.put(model.type, typeCountMap.get(model.type) + 1);
            } else {
                typeCountMap.put(model.type, 1);
            }
        }
        // 找出最多分类
        for (Map.Entry<String, Integer> entry : typeCountMap.entrySet()) {
            if (entry.getValue() > tempVal) {
                tempVal = entry.getValue();
                type = entry.getKey();
            }
        }
        return type;
    }
 
    /**
     * KNN 算法的实现
     *
     * @param k
     * @param knnModelList
     * @param inputModel
     * @return
     */
    public static String calKNN(int k, List<KNNModel> knnModelList, KNNModel inputModel) {
        System.out.println("1.计算距离");
        calDistince(knnModelList, inputModel);
        System.out.println("2.按距离（近-->远）排序");
        Collections.sort(knnModelList);
        System.out.println("3.取前k个数据");
        while (knnModelList.size() > k) {
            knnModelList.remove(k);
        }
        System.out.println("4.找出前k个数据中分类出现频率最大的数据");
        String type = findMostData(knnModelList);
        return type;
    }
 
    /**
     * 测试KNN算法
     *
     * @param args
     */
    public static void main(String[] args) {
        // 准备数据
        List<KNNModel> knnModelList = new ArrayList<KNNModel>();
        knnModelList.add(new KNNModel(1.1, 1.1, 1.1, "A"));
        knnModelList.add(new KNNModel(1.2, 1.1, 1.0, "A"));
        knnModelList.add(new KNNModel(1.1, 1.0, 1.0, "A"));
        knnModelList.add(new KNNModel(3.0, 3.1, 1.0, "B"));
        knnModelList.add(new KNNModel(3.1, 3.0, 1.0, "B"));
        knnModelList.add(new KNNModel(5.4, 6.0, 4.0, "C"));
        knnModelList.add(new KNNModel(5.5, 6.3, 4.1, "C"));
        knnModelList.add(new KNNModel(6.0, 6.0, 4.0, "C"));
        knnModelList.add(new KNNModel(10.0, 12.0, 10.0, "M"));
        // 预测数据
        KNNModel predictionData = new KNNModel(5.1, 6.2, 2.0, "NB");
        // 计算
        String result = calKNN(3, knnModelList, predictionData);
        System.out.println("预测结果："+result);
    }
}