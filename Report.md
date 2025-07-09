# Report.md

## 1. `start()` vs `run()`

### Code:
```java
public class StartVsRun {    
    static class MyRunnable implements Runnable {    
        public void run() {    
            System.out.println("Running in: " + Thread.currentThread().getName()); 
        }    
    }    

    public static void main(String[] args) throws InterruptedException {    
        Thread t1 = new Thread(new MyRunnable(), "Thread-1");    
        System.out.println("Calling run()");    
        t1.run();    
        Thread.sleep(100);    

        Thread t2 = new Thread(new MyRunnable(), "Thread-2");    
        System.out.println("Calling start()");    
        t2.start();    
    }  
}
```

### Output:
```
Calling run()
Running in: main
Calling start()
Running in: Thread-2
```

### Notes:

- When we call `run()` directly, it's just a method call. No new thread. So it runs in the `main` thread.
- When we use `start()`, it actually starts a new thread. That’s why the output says `Thread-2`.

So `start()` creates a new thread, `run()` just calls the method.

---

## 2. Daemon Threads

### Code:
```java
public class DaemonExample {
    static class DaemonRunnable implements Runnable {
        public void run() {
            for(int i = 0; i < 20; i++) {
                System.out.println("Daemon thread running...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //[Handling Exception...]  
                }
            }
        }
    }
    public static void main(String[] args) {
        Thread thread = new Thread(new DaemonRunnable());
        thread.setDaemon(true);
        thread.start();
        System.out.println("Main thread ends.");
    }
}  
```

### Output (roughly):
```
Main thread ends.
Daemon thread running...
```

Sometimes you’ll see 1 or 2 lines from the daemon thread, sometimes none—it depends on timing.

### Notes:

- Daemon threads are background threads. If the main thread finishes, the JVM doesn't wait for daemon threads to complete.
- If you remove `setDaemon(true)`, it becomes a normal thread, and the JVM will wait until it's done.

### Real-world examples:

Garbage collection, background log writers, etc.

---

## 3. Shorter Thread Creation

### Code:
```java
public class ThreadDemo {  
    public static void main(String[] args) {  
        Thread thread = new Thread(() -> {  
            System.out.println("Thread is running using a ...!");  
        });  

        thread.start();  
    }  
}
```

### Output:
```
Thread is running using a ...!
```

### Notes:

- This is using a lambda expression: `() -> { }`
- It’s just a shortcut for writing a `Runnable` without needing to make a whole new class.

Compared to writing a class that implements `Runnable` or extends `Thread`, Used when you just need a quick thread.
