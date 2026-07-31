public class Sum 
{
    public static void main(String args[]) {
        if(args.length != 2) {
            System.out.println("Input <num1><num2>");
            return;
        }

        double num1 = Double.parseDouble(args[0]);
        double num2 = Double.parseDouble(args[1]);
        double sum = num1 + num2;
        System.out.println("Sum is: " + sum);
    }
}