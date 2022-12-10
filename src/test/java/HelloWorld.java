public class HelloWorld extends Thread {
    StringBuffer buf;
    String data;

    public HelloWorld(StringBuffer buf, String data) {
        this.buf = buf;
        this.data = data;
    }

    public void run() {
        try {
            System.out.println("STAAAAART" + System.currentTimeMillis());
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("STOOOOOOOP" + System.currentTimeMillis());
        buf.append(data);
    }
}
