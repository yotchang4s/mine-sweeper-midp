import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public abstract class GameCanvas extends Canvas implements CommandListener {
	private static Image imgOff;
	protected static Graphics graOff;
	protected final int width = getWidth();
	protected final int height = getHeight();

	public GameCanvas() {
		if (graOff == null) {
			imgOff = Image.createImage(width, height);
			graOff = imgOff.getGraphics();
		}
		setCommandListener(this);
	}

	public void drawBuffer(int x, int y, int width, int height) {
		repaint(x, y, width, height);
		serviceRepaints();
	}

	public void drawBuffer() {
		repaint();
		serviceRepaints();
	}

	public final void paint(Graphics g) {
		g.drawImage(imgOff, 0, 0, Graphics.TOP | Graphics.LEFT);
	}

	public void commandAction(Command c, Displayable s) {
	}
}