import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {

		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		CoolPanel c = new CoolPanel();
		frame.add(c, BorderLayout.CENTER);
		c.setBackground(Color.WHITE);
		c.setFocusable(true);

		frame.setVisible(true);

		List<Long> times = new ArrayList<>();
		new Thread(()->{
			while (true) {
				times.add(System.currentTimeMillis());
				for(int i = 0; i < times.size(); i++) {
					long l = times.get(i);
					if(System.currentTimeMillis() - l > 1000L) times.remove(l);
				}
				frame.setTitle(times.size() + " FPS");
				c.paintComponent();
			}
		}).start();
	}

}
