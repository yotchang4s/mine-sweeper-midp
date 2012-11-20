import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

public class MineSweeper extends MIDlet {

	public MineSweeper() {
	}

	public void startApp() {
		Display.getDisplay(this).setCurrent(new MineSweeperCanvas());
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) {
	}

	public class MineSweeperCanvas extends GameCanvas implements
			MineSweeperListener {
		private final int TOP = 0, PLAY = 1;
		private int scene;
		private int level = 0;
		private int bgColor = 0x444444;
		private int color = 0xffffff;
		private MineSweeperField mineSweeperLevel0Field;
		private MineSweeperField mineSweeperLevel1Field;
		private MineSweeperField mineSweeperLevel2Field;
		private MineSweeperField mineSweeperLevel3Field;

		private MineSweeperCanvas() {
			mineSweeperLevel0Field = new MineSweeperLevel0Field(this);
			mineSweeperLevel1Field = new MineSweeperLevel1Field(this);
			mineSweeperLevel2Field = new MineSweeperLevel2Field(this);
			mineSweeperLevel3Field = new MineSweeperLevel3Field(this);
			topSceneInit();
			playSceneInit();
			setScene(TOP);
		}

		private void setScene(int scene) {
			switch (scene) {
			case TOP:
				topSceneStart();
				break;
			case PLAY:
				playSceneStart();
				break;
			}
			this.scene = scene;
		}

		public void commandAction(Command c, Displayable s) {
			if (scene == TOP) {
				topSceneCommandAction(c, s);
			} else if (scene == PLAY) {
				playSceneCommandAction(c, s);
			}
		}

		public void keyPressed(int keyCode) {
			if (scene == TOP) {
				topSceneKeyPressed(keyCode);
			} else if (scene == PLAY) {
				playSceneKeyPressed(keyCode);
			}
		}

		public void keyReleased(int keyCode) {
			if (scene == TOP) {
				topScenekeyReleased(keyCode);
			} else if (scene == PLAY) {
				playScenekeyReleased(keyCode);
			}
		}

		public void keyRepeated(int keyCode) {
			keyPressed(keyCode);
		}

		// ----------------------------------------------------
		// ----ＴＯＰの処理
		// ----------------------------------------------------
		private int sceneTop;
		private int marginTop;
		private Image titleImage;
		private String[] levelStr = { "超初級", "初級", "中級", "上級" };
		private int[] score;
		private Command cmdExit;

		private void topSceneInit() {
			cmdExit = new Command("EXIT", Command.EXIT, 1);
			try {
				titleImage = Image.createImage("/title.png");
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			score = new int[4];
			marginTop = (height - (titleImage.getHeight() + 4
					* graOff.getFont().getHeight() + 3 + 3)) / 2;
		}

		private void topSceneStart() {
			sceneTop = 0;
			score[0] = mineSweeperLevel0Field.getHScore();
			score[1] = mineSweeperLevel1Field.getHScore();
			score[2] = mineSweeperLevel2Field.getHScore();
			score[3] = mineSweeperLevel3Field.getHScore();
			addCommand(cmdExit);
			topSceneDraw();
		}

		private void topSceneDraw() {
			graOff.setColor(0x000000);
			graOff.fillRect(0, 0, width, height);
			graOff.drawImage(titleImage, (width - titleImage.getWidth()) / 2,
					marginTop, Graphics.TOP | Graphics.LEFT);
			for (int i = 0; i <= 3; i++) {
				if (i == level) {
					graOff.setColor(0xffffff);
				} else {
					graOff.setColor(0x5e5e5e);
				}
				graOff.drawString(levelStr[i], width / 2
						- graOff.getFont().stringWidth(levelStr[0]), marginTop
						+ titleImage.getHeight() + i + i
						* graOff.getFont().getHeight() + 3, Graphics.TOP
						| Graphics.LEFT);
			}
			graOff.setColor(0xffffff);
			graOff.drawString("HSCORE", width / 2 + 5,
					marginTop + titleImage.getHeight() + 3, Graphics.TOP
							| Graphics.LEFT);
			graOff.drawImage(numberFrameImage, width / 2 + 5,
					marginTop + titleImage.getHeight()
							+ graOff.getFont().getHeight() + 3, Graphics.TOP
							| Graphics.LEFT);
			drawNumber(graOff, score[level], 3, width / 2 + 7, marginTop
					+ titleImage.getHeight() + graOff.getFont().getHeight() + 5);
			repaint();
			serviceRepaints();
		}

		public void topSceneCommandAction(Command c, Displayable s) {
			if (c == cmdExit) {
				destroyApp(false);
				notifyDestroyed();
			}
		}

		public void topSceneKeyPressed(int keyCode) {
			int action = 0;
			try {
				action = getGameAction(keyCode);
			} catch (Exception e) {
			}
			if (action == LEFT || keyCode == KEY_NUM4) {
			} else if (action == RIGHT || keyCode == KEY_NUM6) {
			} else if (action == UP || keyCode == KEY_NUM2) {
				int itijiLevel = level - 1;
				if (0 <= itijiLevel) {
					level = itijiLevel;
				} else {
					level = 3;
				}
			} else if (action == DOWN || keyCode == KEY_NUM8) {
				int itijiLevel = level + 1;
				if (itijiLevel <= 3) {
					level = itijiLevel;
				} else {
					level = 0;
				}
			}
			topSceneDraw();
		}

		public void topScenekeyReleased(int keyCode) {
			int action = 0;
			try {
				action = getGameAction(keyCode);
			} catch (Exception e) {
			}
			if (action == FIRE || keyCode == KEY_NUM5) {
				removeCommand(cmdExit);
				setScene(PLAY);
			}
		}

		// ----------------------------------------------------
		// ----ＰＬＡＹの処理
		// ----------------------------------------------------
		private final int RUN = 0, WIN = 1, LOSE = 2;
		private int x;
		private int y;
		private int blockWidthHeight;
		private boolean isPush;
		private boolean isDrawThread;
		private Graphics graField;
		private Graphics graInfo;
		private int playScene;
		private Image imgField;
		private int fieldWidth;
		private int fieldHeight;
		private MineSweeperField mineSweeperField;
		private Image blockBombImage;
		private Image blockCloseImage;
		private Image blockFlagImage;
		private Image blockBombLoseImage;
		private Image blockMissImage;
		private Image sunSmileImage;
		private Image sunSurpriseImage;
		private Image sunGlassesImage;
		private Image sunOopsImage;
		private Image numberFrameImage;
		private Image blockPushImage;
		private Image imgInfo;
		private Image[] blockNumberImages;
		private Image[] numberImages;
		private Command cmdReset;
		private Command cmdRedo;
		private Command cmdFlag;

		private void playSceneInit() {
			blockNumberImages = new Image[9];
			numberImages = new Image[11];
			try {
				blockCloseImage = Image.createImage("/block/close.png");
				blockBombImage = Image.createImage("/block/bomb.png");
				blockFlagImage = Image.createImage("/block/flag.png");
				blockBombLoseImage = Image.createImage("/block/bomb_lose.png");
				blockMissImage = Image.createImage("/block/miss.png");
				sunSmileImage = Image.createImage("/sun/smile.png");
				sunSurpriseImage = Image.createImage("/sun/surprise.png");
				sunGlassesImage = Image.createImage("/sun/glasses.png");
				sunOopsImage = Image.createImage("/sun/oops.png");
				numberFrameImage = Image.createImage("/number/frame.png");
				for (int i = 0; i <= 8; i++) {
					blockNumberImages[i] = Image.createImage("/block/number/"
							+ i + ".png");
				}
				for (int i = 0; i <= 9; i++) {
					numberImages[i] = Image
							.createImage("/number/" + i + ".png");
				}
				numberImages[10] = Image.createImage("/number/mainasu.png");
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			imgInfo = Image.createImage(sunSmileImage.getWidth()
					+ numberFrameImage.getWidth() * 2,
					numberFrameImage.getHeight());
			graInfo = imgInfo.getGraphics();
			graInfo.drawImage(numberFrameImage, 0, 0, Graphics.TOP
					| Graphics.LEFT);
			graInfo.drawImage(numberFrameImage, numberFrameImage.getWidth()
					+ sunSmileImage.getWidth(), 0, Graphics.TOP | Graphics.LEFT);
			blockPushImage = blockNumberImages[0];
			cmdReset = new Command("RESET", Command.SCREEN, 2);
			cmdRedo = new Command("REDO", Command.SCREEN, 1);
			cmdFlag = new Command("FLAG", Command.SCREEN, 2);
			blockWidthHeight = blockCloseImage.getWidth() - 1;
		}

		private synchronized void playSceneStart() {
			graOff.translate(-graOff.getTranslateX(), -graOff.getTranslateY());
			bufferClear();
			graOff.setColor(color);
			graOff.drawString("Now Loading...", (width - graOff.getFont()
					.stringWidth("Now Loading...")) / 2, (height - graOff
					.getFont().getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
			repaint();
			serviceRepaints();
			isPush = false;
			switch (level) {
			case 0:
				mineSweeperField = mineSweeperLevel0Field;
				break;
			case 1:
				mineSweeperField = mineSweeperLevel1Field;
				break;
			case 2:
				mineSweeperField = mineSweeperLevel2Field;
				break;
			case 3:
				mineSweeperField = mineSweeperLevel3Field;
				break;
			default:
				mineSweeperField = mineSweeperLevel1Field;
				break;
			}
			System.gc();
			mineSweeperField.start();
			x = mineSweeperField.getWidth() / 2;
			y = mineSweeperField.getHeight() / 2;
			addCommand(cmdRedo);
			addCommand(cmdFlag);
			imgField = Image.createImage(
					blockWidthHeight * mineSweeperField.getWidth() + 1,
					blockWidthHeight * mineSweeperField.getHeight() + 1);
			graField = imgField.getGraphics();
			fieldWidth = imgField.getWidth();
			fieldHeight = imgField.getHeight();
			playScene = RUN;
			setPush(false);
			drawInfoSun(sunSmileImage);
			drawInfoBombCount();
			drawInfoScoreNumber();
			bufferClear();
			drawFieldCloseAll();
			drawOffField();
			hasBuffer = true;
			(new DrawThread()).start();
		}

		public void blockOpened(boolean[][] koshin) {
			for (int x = 0; x < mineSweeperField.getWidth(); x++) {
				for (int y = 0; y < mineSweeperField.getHeight(); y++) {
					if (koshin[x][y]) {
						int state = mineSweeperField.getState(x, y);
						if (state == MineSweeperField.VISIBLE) {
							int number = mineSweeperField.getNumber(x, y);
							if (0 <= number && number <= 8) {
								graField.drawImage(blockNumberImages[number], x
										* blockWidthHeight, y
										* blockWidthHeight, Graphics.TOP
										| Graphics.LEFT);
								drawBlockFrame(x, y);
							} else if (number == MineSweeperField.BOMB) {
								graField.drawImage(blockBombImage, x
										* blockWidthHeight, y
										* blockWidthHeight, Graphics.TOP
										| Graphics.LEFT);
								drawBlockFrame(x, y);
							}
						} else if (state == MineSweeperField.NOT_VISIBLE) {
							if (mineSweeperField.getFlag(x, y)) {
								drawFieldFlag(x, y);
							}
						}
					}
				}
			}
		}

		public void gameOver(boolean isWin) {
			removeCommand(cmdFlag);
			if (isWin) {
				playScene = WIN;
				drawInfoSun(sunGlassesImage);
				drawInfoBombCount();
			} else {
				playScene = LOSE;
				drawInfoSun(sunOopsImage);
				for (int i = 0; i < mineSweeperField.getWidth(); i++) {
					for (int j = 0; j < mineSweeperField.getHeight(); j++) {
						if (mineSweeperField.getState(i, j) == MineSweeperField.VISIBLE
								&& mineSweeperField.getFlag(i, j)) {
							graField.drawImage(blockMissImage, i
									* blockWidthHeight, j * blockWidthHeight,
									Graphics.TOP | Graphics.LEFT);
							drawBlockFrame(i, j);
						}
					}
				}
				graField.drawImage(blockBombLoseImage, x * blockWidthHeight, y
						* blockWidthHeight, Graphics.TOP | Graphics.LEFT);
				drawBlockFrame(x, y);
			}
			addCommand(cmdReset);
		}

		private void drawOffField() {
			graOff.drawImage(imgField, (width - imgField.getWidth()) / 2,
					(height - imgField.getHeight()) / 2, Graphics.TOP
							| Graphics.LEFT);
		}

		private void bufferClear() {
			int translateX = graOff.getTranslateX();
			int translateY = graOff.getTranslateY();
			graOff.translate(-translateX, -translateY);
			graOff.setColor(bgColor);
			graOff.fillRect(0, 0, width, height);
			graOff.translate(translateX, translateY);
		}

		private void drawFieldFlag(int x, int y) {
			graField.drawImage(blockFlagImage, x * blockWidthHeight, y
					* blockWidthHeight, Graphics.TOP | Graphics.LEFT);
		}

		private void drawFieldCloseBlock(int x, int y) {
			graField.drawImage(blockCloseImage, x * blockWidthHeight, y
					* blockWidthHeight, Graphics.TOP | Graphics.LEFT);
		}

		private void drawFieldCloseAll() {
			for (int x = 0; x < mineSweeperField.getWidth(); x++) {
				for (int y = 0; y < mineSweeperField.getHeight(); y++) {
					drawFieldCloseBlock(x, y);
				}
			}
		}

		private void drawBlockFrame(int x, int y) {
			graField.setColor(0x000000);
			if (mineSweeperField.getNumber(x, y) != 0) {
				if (mineSweeperField.getState(x + 1, y) == MineSweeperField.NOT_VISIBLE) {
					graField.drawLine(x * blockWidthHeight + blockWidthHeight,
							y * blockWidthHeight, x * blockWidthHeight
									+ blockWidthHeight, y * blockWidthHeight
									+ blockWidthHeight);
				}
				if (mineSweeperField.getState(x - 1, y) == MineSweeperField.NOT_VISIBLE) {
					graField.drawLine(x * blockWidthHeight, y
							* blockWidthHeight, x * blockWidthHeight, y
							* blockWidthHeight + blockWidthHeight);
				}
				if (mineSweeperField.getState(x, y - 1) == MineSweeperField.NOT_VISIBLE) {
					graField.drawLine(x * blockWidthHeight, y
							* blockWidthHeight, x * blockWidthHeight
							+ blockWidthHeight, y * blockWidthHeight);
				}
				if (mineSweeperField.getState(x, y + 1) == MineSweeperField.NOT_VISIBLE) {
					graField.drawLine(x * blockWidthHeight, y
							* blockWidthHeight + blockWidthHeight, x
							* blockWidthHeight + blockWidthHeight, y
							* blockWidthHeight + blockWidthHeight);
				}
			}
		}

		private void drawInfoSun(Image sunImage) {
			graInfo.drawImage(sunImage, numberFrameImage.getWidth(), 0,
					Graphics.TOP | Graphics.LEFT);
		}

		private void setPush(boolean isPush) {
			this.isPush = isPush;
		}

		private void drawInfoBombCount() {
			drawNumber(graInfo, mineSweeperField.getBombSize()
					- mineSweeperField.getTrueFlagCount(), 3, 2, 2);
		}

		private void drawInfoScoreNumber() {
			drawNumber(graInfo, mineSweeperField.getNowTime(), 3,
					numberFrameImage.getWidth() + sunSmileImage.getWidth() + 2,
					2);
		}

		private void drawNumber(Graphics g, long numbers, int figure, int x,
				int y) {
			long numbers1 = Math.abs(numbers);
			for (int i = 0; i < figure; i++) {
				long c = 1;
				for (int j = 0; j < figure - 1; j++) {
					c *= 10;
				}
				for (int j = 0; j < i; j++) {
					c /= 10;
				}
				int temporaryCount = (int) ((numbers1 / c) % 10);
				if (numbers < 0) {
					if (i == 0) {
						temporaryCount = 10;
					}
				}
				g.drawImage(numberImages[temporaryCount], x + i
						* numberImages[temporaryCount].getWidth() + i, y,
						Graphics.TOP | Graphics.LEFT);
			}
		}

		// 上部のゲーム情報を描画
		private void drawInfo() {
			int translateX = graOff.getTranslateX();
			int translateY = graOff.getTranslateY();
			graOff.translate(-translateX, -translateY);
			graOff.drawImage(imgInfo, (width - imgInfo.getWidth()) / 2, 2,
					Graphics.TOP | Graphics.LEFT);
			graOff.translate(translateX, translateY);
		}

		private boolean hasBuffer = false;
		private boolean isMoveDraw = false;
		private int left, right, up, down, firePressed, fireReleased, flag;

		private class DrawThread extends Thread {
			public void run() {
				int oldTime = 0;
				int nowTime = 0;
				boolean hasScoreBuffer = false;
				left = 0;
				right = 0;
				up = 0;
				down = 0;
				firePressed = 0;
				fireReleased = 0;
				flag = 0;
				isDrawThread = true;
				while (isDrawThread) {
					nowTime = mineSweeperField.getNowTime();
					for (int i = 0; i < left; i++) {
						int itijiX = x - 1;
						if (0 <= itijiX) {
							x = itijiX;
							graOff.translate(blockWidthHeight, 0);
						} else {
							x = mineSweeperField.getWidth() - 1;
							graOff.translate(-graOff.getTranslateX() * 2, 0);
						}
						left--;
						isMoveDraw = true;
					}
					for (int i = 0; i < right; i++) {
						int itijiX = x + 1;
						if (itijiX <= mineSweeperField.getWidth() - 1) {
							x = itijiX;
							graOff.translate(-blockWidthHeight, 0);
						} else {
							x = 0;
							graOff.translate(-graOff.getTranslateX() * 2, 0);
						}
						right--;
						isMoveDraw = true;
					}
					for (int i = 0; i < up; i++) {
						int itijiY = y - 1;
						if (0 <= itijiY) {
							y = itijiY;
							graOff.translate(0, blockWidthHeight);
						} else {
							y = mineSweeperField.getHeight() - 1;
							graOff.translate(0, -graOff.getTranslateY() * 2
									+ imgInfo.getHeight() + 2);
						}
						up--;
						isMoveDraw = true;
					}
					for (int i = 0; i < down; i++) {
						int itijiY = y + 1;
						if (itijiY <= mineSweeperField.getHeight() - 1) {
							y = itijiY;
							graOff.translate(0, -blockWidthHeight);
						} else {
							y = 0;
							graOff.translate(0, -graOff.getTranslateY() * 2
									+ imgInfo.getHeight() + 2);
						}
						down--;
						isMoveDraw = true;
					}
					for (int i = 0; i < firePressed; i++) {
						if (playScene == RUN) {
							setPush(true);
							drawInfoSun(sunSurpriseImage);
							hasBuffer = true;
						}
						firePressed--;
					}
					for (int i = 0; i < fireReleased; i++) {
						if (playScene == RUN) {
							drawInfoSun(sunSmileImage);
							mineSweeperField.open(x, y);
							setPush(false);
							isMoveDraw = true;
						}
						fireReleased--;
					}
					for (int i = 0; i < flag; i++) {
						if (mineSweeperField.getState(x, y) == MineSweeperField.NOT_VISIBLE) {
							mineSweeperField.setFlag(x, y);
							if (mineSweeperField.getFlag(x, y)) {
								drawFieldFlag(x, y);
								drawInfoBombCount();
							} else {
								drawFieldCloseBlock(x, y);
							}
							drawInfoBombCount();
							isMoveDraw = true;
						}
						flag--;
					}
					if (nowTime != oldTime) {
						oldTime = nowTime;
						drawInfoScoreNumber();
						hasScoreBuffer = true;
					}
					if (isMoveDraw) {
						bufferClear();
						drawOffField();
						isMoveDraw = false;
						hasBuffer = true;
					}
					if (hasBuffer) {
						// 選択の枠と押した時の凹みを描画
						if (playScene == RUN) {
							if (isPush
									&& mineSweeperField.getState(x, y) == MineSweeperField.NOT_VISIBLE
									&& !mineSweeperField.getFlag(x, y)) {
								graOff.drawImage(blockPushImage,
										(width - imgField.getWidth()) / 2 + x
												* blockWidthHeight,
										(height - imgField.getHeight()) / 2 + y
												* blockWidthHeight,
										Graphics.TOP | Graphics.LEFT);
							}
							graOff.setColor(0xff0000);
							graOff.drawRect((width - imgField.getWidth()) / 2
									+ x * blockWidthHeight,
									(height - imgField.getHeight()) / 2 + y
											* blockWidthHeight,
									blockWidthHeight, blockWidthHeight);
						}
						drawInfo();

						// バッファを実際に描画
						repaint();
						serviceRepaints();
						hasBuffer = false;
						hasScoreBuffer = false;
					} else if (hasScoreBuffer) {
						drawInfo();
						repaint(width / 2 + sunSmileImage.getWidth() / 2 + 2,
								4, numberImages[0].getWidth() * 3 + 2,
								numberImages[0].getHeight());
						serviceRepaints();
						hasScoreBuffer = false;
					}
					try {
						sleep(10);
						yield();
					} catch (Exception e) {
						System.out.println(e.toString());
					}
				}
			}
		}

		public void playSceneCommandAction(Command c, Displayable s) {
			if (c == cmdRedo) {
				isDrawThread = false;
				removeCommand(cmdReset);
				addCommand(cmdFlag);
				playSceneStart();
			} else if (c == cmdReset) {
				isDrawThread = false;
				removeCommand(cmdReset);
				removeCommand(cmdRedo);
				removeCommand(cmdFlag);
				graOff.translate(-graOff.getTranslateX(),
						-graOff.getTranslateY());
				setScene(TOP);
			} else if (c == cmdFlag) {
				flag++;
			}
		}

		private void playSceneKeyPressed(int keyCode) {
			int action = 0;
			try {
				action = getGameAction(keyCode);
			} catch (Exception e) {
			}
			if (action == LEFT || keyCode == KEY_NUM4) {
				left++;
			} else if (action == RIGHT || keyCode == KEY_NUM6) {
				right++;
			} else if (action == UP || keyCode == KEY_NUM2) {
				up++;
			} else if (action == DOWN || keyCode == KEY_NUM8) {
				down++;
			} else if (action == FIRE || keyCode == KEY_NUM5) {
				firePressed++;
			}
		}

		private void playScenekeyReleased(int keyCode) {
			int action = 0;
			try {
				action = getGameAction(keyCode);
			} catch (Exception e) {
			}
			if (action == FIRE || keyCode == KEY_NUM5) {
				fireReleased++;
			}
		}
	}
}