package ardust.shared;

public class EventFlag {
    private boolean flag;

    public void set() {
        synchronized (this) {
            if (!flag) {
                flag = true;
                notifyAll();
            }
        }
    }

    public void reset() {
        synchronized (this) {
            flag = false;
        }
    }

    public void waitFor() {
        synchronized (this) {
            while (true) {
                if (!flag)
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
            }
        }
    }
}
