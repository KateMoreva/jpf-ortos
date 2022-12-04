public class HelloWorld extends Thread {
    StringBuffer buf;
    String data;

    public HelloWorld(StringBuffer buf, String data) {
        this.buf = buf;
        this.data = data;
    }

    public void run() {
        buf.append(data);
    }
}
