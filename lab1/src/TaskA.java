import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class TaskA {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400,300);
        JPanel panel = new JPanel();
        frame.add(panel);

        JSlider slider = new JSlider(0,100,50);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBounds(50, 20, 300, 40);

        Thread th1 = new Thread(() -> {
            while (true) {
                synchronized (slider) {
                    if (slider.getValue() > 10) {
                        slider.setValue(slider.getValue() - 10);
                    }
                }
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread th2 = new Thread(() -> {
            while (true) {
                synchronized (slider) {
                    if (slider.getValue() < 90) {
                        slider.setValue(slider.getValue() + 10);
                    }
                }
                try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        JSpinner spinner1 = new JSpinner();
        JSpinner spinner2 = new JSpinner();
        spinner1.setValue(5);
        spinner2.setValue(5);
        spinner1.setBounds(145, 80, 50, 30);
        spinner2.setBounds(205, 80, 50, 30);
        spinner1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                th1.setPriority(getSpinnerValue((JSpinner) e.getSource()));
                System.out.println("The priority of the first thread was changed to " + th1.getPriority());
            }
        });
        spinner2.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                th2.setPriority(getSpinnerValue((JSpinner) e.getSource()));
                System.out.println("The priority of the second thread was changed to " + th2.getPriority());
            }
        });

        JButton btn = new JButton("START");
        btn.setBounds(165, 130, 70, 30);
        btn.addActionListener(e -> {
            synchronized (slider) {
                th1.start();
                th2.start();
            }
        });

        panel.setLayout(null);
        panel.add(btn);
        panel.add(slider);
        panel.add(spinner1);
        panel.add(spinner2);

        frame.setVisible(true);
    }

    private static int getSpinnerValue(JSpinner spinner) {
        if ((int) spinner.getValue() > 10) {
            spinner.setValue(10);
            return 10;
        } else if ((int) spinner.getValue() < 1) {
            spinner.setValue(1);
            return 1;
        } else
            return (int) spinner.getValue();
    }
}
