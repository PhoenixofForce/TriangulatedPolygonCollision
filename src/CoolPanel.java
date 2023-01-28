import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

public class CoolPanel extends JPanel {


	private float dx, dy, zoom;
	private int last_x, last_y;

	private Polygon current = null;
	private final List<HitBox> hitboxes = new ArrayList<>();

	//a movable, zoomable panel
	public CoolPanel() {

		centerCamera();

		this.addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				zoom *= Math.pow(1.2, -e.getPreciseWheelRotation());
			}
		});

		this.addMouseMotionListener(new MouseAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				last_x = e.getX();
				last_y = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (SwingUtilities.isMiddleMouseButton(e)) {
					dx += (e.getX() - last_x) / zoom;
					dy += (e.getY() - last_y) / zoom;
				} else if(SwingUtilities.isRightMouseButton(e) && current == null) {
					float[] translatedPosition = translate(e.getX(), e.getY());
					HitBox selectedHitbox = null;
					for (HitBox hitbox : hitboxes) {
						if (hitbox.getOutline().contains(translatedPosition[0], translatedPosition[1])) {
							selectedHitbox = hitbox;
							break;
						}
					}

					if(selectedHitbox != null) {
						int dx = (e.getX() - last_x) > 0? (int) Math.ceil((e.getX() - last_x) / zoom): (int) Math.floor((e.getX() - last_x) / zoom);
						int dy = (e.getY() - last_y) > 0? (int) Math.ceil((e.getY() - last_y) / zoom): (int) Math.floor((e.getY() - last_y) / zoom);
						selectedHitbox.getOutline().translate(dx, dy);
						for(Polygon polygon: selectedHitbox.getPolygons()) polygon.translate(dx, dy);
					}
				}

				last_x = e.getX();
				last_y = e.getY();
			}
		});

		this.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == 2 && current != null) current = null;
				if(e.getButton() == MouseEvent.BUTTON1) {
					if(current == null) current = new Polygon();
					float[] t = translate(e.getX(), e.getY());
					current.addPoint((int)t[0], (int)t[1]);
				}
				if(e.getButton() == MouseEvent.BUTTON3) {
					if(current != null) hitboxes.add(new HitBox(current));
					current = null;
				}
			}
		});
	}

	/**
	 * Translates screenspace into worldspace
	 * @param xPos
	 * @param yPos
	 * @return
	 */
	private float[] translate(int xPos, int yPos) {
		float x = xPos, y = yPos;
		x -= this.getWidth() / 2.0;
		y -= this.getHeight() / 2.0;
		x /= zoom;
		y /= zoom;
		x -= dx;
		y -= dy;
		x = (float) Math.floor(x);
		y = (float) Math.floor(y);

		return new float[]{x, y};
	}

	private void centerCamera() {
		this.dx = -64;
		this.dy = -64;
		this.zoom = 0.5f;
	}

	public void paintComponent() {
		if (this.getWidth() <= 0 || this.getHeight() <= 0) return;
		Graphics g = this.getGraphics();
		BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) img.getGraphics();

		g2.setColor(new Color(240, 248, 255));
		g2.fillRect(0, 0, img.getWidth(), img.getHeight());

		g2.translate(this.getWidth() / 2.0, this.getHeight() / 2.0);
		g2.scale(zoom, zoom);
		g2.translate(dx, dy);

		g2.setStroke(new BasicStroke(1f));
		g2.setColor(Color.RED);

		for (int i = 0; i < hitboxes.size(); i++) {
			HitBox b = hitboxes.get(i);

			g2.setColor(b.collidesWithOne(hitboxes) ? Color.RED : Color.GREEN);
			g2.fillPolygon(b.getOutline());

			g2.setColor(b.collidesWithOne(hitboxes) ? Color.RED.darker() : Color.GREEN.darker());
			g2.drawPolygon(b.getOutline());
			for(Polygon p: b.getPolygons()) g2.drawPolygon(p);

			g2.setColor(Color.BLUE);
			g2.drawRect(b.boundingBox().x, b.boundingBox().y, b.boundingBox().width, b.boundingBox().height);
		}

		if (current != null) {
			HitBox hb = new HitBox(current, true);
			g2.setColor(Color.CYAN);
			g2.fillPolygon(hb.getOutline());

			g2.setColor(Color.CYAN.darker());
			g2.drawPolygon(hb.getOutline());

			g2.setColor(Color.BLUE);
			g2.drawRect(hb.boundingBox().x, hb.boundingBox().y, hb.boundingBox().width, hb.boundingBox().height);
		}

		g.drawImage(img, 0, 0, null);
	}
}