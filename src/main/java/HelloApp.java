import wtf.HellApp;

public class HelloApp {
    private String getGreetings() {
        return "WTF";
    }

    public static void main(String[] args) {
//        System.out.println(new HelloApp().getGreetings());
        int a = 6;
        int b = 7;
        System.out.println(a+b);
        System.out.println(new HellApp().getHellGreetings());
    }
}
