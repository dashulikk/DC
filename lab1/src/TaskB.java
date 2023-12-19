import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TaskB {
    /**
     * 0 - free
     * 1 - busy with the first thread
     * 2 - busy with the second thread
     */
    public static int semaphore = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(700, 400, 400, 300);
        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);
        placeComponents(panel);
        frame.setVisible(true);
    }

    private static void placeComponents(JPanel panel) {
        JSlider slider = new JSlider(0,100,50);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(50, 20, 300, 40);
        panel.add(slider);

        JButton startBtn1 = new JButton("ПУСК 1");
        JButton startBtn2 = new JButton("ПУСК 2");
        JButton stopBtn1 = new JButton("СТОП 1");
        JButton stopBtn2 = new JButton("СТОП 2");

        startBtn1.setBounds(80, 80,100, 30);
        startBtn2.setBounds(220, 80,100, 30);
        stopBtn1.setBounds(80, 130, 100, 30);
        stopBtn2.setBounds(220, 130, 100, 30);

        panel.add(startBtn1);
        panel.add(startBtn2);
        panel.add(stopBtn1);
        panel.add(stopBtn2);

        JLabel label = new JLabel();
        label.setBounds(100, 180, 200, 30);
        panel.add(label);

        Thread th1 = new Thread(new Runner1(slider));
        Thread th2 = new Thread(new Runner2(slider));

        startBtn1.addActionListener(e -> {
            if (semaphore == 0) {
                semaphore = 1;
                label.setText("Виконується перший потік");
                if (!th1.isAlive()){
                    try {
                        th1.start();
                        System.out.println("The first thread started");
                        th1.setPriority(Thread.MIN_PRIORITY);
                    } catch (IllegalThreadStateException ex) {
                        label.setText("Перший потік знищений");
                        semaphore = 0;
                    }
                }
            } else {
                label.setText("Зайнято потоком");
            }
        });
        startBtn2.addActionListener(e -> {
            if (semaphore == 0) {
                semaphore = 2;
                label.setText("Виконується другий потік");
                if (!th2.isAlive()){
                    try {
                        th2.start();
                        System.out.println("The second thread started");
                        th2.setPriority(Thread.MAX_PRIORITY);
                    } catch (IllegalThreadStateException ex) {
                        label.setText("Другий потік знищений");
                        semaphore = 0;
                    }
                }
            } else {
                label.setText("Зайнято потоком");
            }
        });
        stopBtn1.addActionListener(e -> {
            if (semaphore == 1) {
                semaphore = 0;
                label.setText("Вільно");
            }
        });
        stopBtn2.addActionListener(e -> {
            if (semaphore == 2) {
                semaphore = 0;
                label.setText("Вільно");
            }
        });
        stopBtn1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    th1.interrupt();
                }
            }
        });
        stopBtn2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    th2.interrupt();
                }
            }
        });
    }
}

class Runner1 implements Runnable {
    private final JSlider slider;

    public Runner1(JSlider slider) {
        this.slider = slider;
    }

    @Override
    public void run() {
        while (true) {
            if (TaskB.semaphore == 1) {
                slider.setValue(10);
            }
            System.out.println("First thread is running");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                System.out.println("Перший потік знищено");
                return;
            }
        }
    }
}

class Runner2 implements Runnable {
    private final JSlider slider;

    public Runner2(JSlider slider) {
        this.slider = slider;
    }

    @Override
    public void run() {
        while (true) {
            if (TaskB.semaphore == 2) {
                slider.setValue(90);
            }
            System.out.println("Second thread is running");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                System.out.println("Другий потік знищено");
                return;
            }
        }
    }
}