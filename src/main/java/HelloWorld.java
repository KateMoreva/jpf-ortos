public class HelloWorld {
    public String getGreeting() {
        return "Hello world.";
    }

    public static void main(String[] args) {
        System.out.println(new HelloWorld().getGreeting());
    }
}
