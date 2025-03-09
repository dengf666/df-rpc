public class test {

    public static void main(String[] args) {
        double[] a = new double[]{
                80, 86, 79, 93, 82, 84, 78, 81, 75, 76, 95, 81, 77, 84, 84, 95, 87, 89, 78, 94, 81, 74, 91, 84, 82, 88, 85, 85, 90, 94, 76,
                82, 91, 81, 76, 93, 86, 78, 90, 89, 74, 82, 60, 89, 89, 76, 82, 96, 75, 81, 85, 86, 80, 95, 85, 78, 86, 92, 89, 89, 81, 87, 81
        };

        double length = a.length;
        double sum = 0;
        for (int i = 0; i < length; i++) {
            sum += a[i];
        }
        System.out.println(sum / length);
    }
}
