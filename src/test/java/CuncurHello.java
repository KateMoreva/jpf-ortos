public class CuncurHello {
    public static void main(String[] args) {

        StringBuffer buf = new StringBuffer();
        HelloWorld hw1 = new HelloWorld(buf, "Hello, ");
        HelloWorld hw2 = new HelloWorld(buf, "World!");
        hw1.start(); // spin off first worker thread
        hw2.start(); // spin off second worker thread
        try {
            hw1.join(); // wait for first worker to finish
            hw2.join(); // wait for second worker
        } catch (InterruptedException e) {
        }

        assert (buf.toString().equals("Hello, World!"));
    }
}
