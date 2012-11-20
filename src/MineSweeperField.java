import java.util.*;
import javax.microedition.rms.*;

public abstract class MineSweeperField {
	public final static int BOMB = 9, VISIBLE = 0, NOT_VISIBLE = 1, ATHER = -1;
	private int[][] stage;
	private int[][] state;
	private boolean[][] koshin;
	private boolean[][] flag;
	private static Random random;
	private MineSweeperListener msl;
	protected int width = 5;
	protected int height = 5;
	protected int bombSize = 5;
	protected String dbName;
	private long startTime;
	private long endTime;
	private boolean isGame = false;

	public MineSweeperField(MineSweeperListener l) {
		msl = l;
		if (random == null) {
			random = new Random();
		}
	}

	public int getHScore() {
		int hScore = 999;
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(dbName, false);
			hScore = Integer.parseInt(new String(rs.getRecord(1)));
			if (rs != null)
				rs.closeRecordStore();
		} catch (Exception e) {
			try {
				if (rs != null)
					rs.closeRecordStore();
				rs = RecordStore.openRecordStore(dbName, true);

				byte[] data = Long.toString(hScore).getBytes();
				rs.addRecord(data, 0, data.length);
				if (rs != null)
					rs.closeRecordStore();
			} catch (Exception f) {
				System.out.println(f.toString());
			}
		}
		return hScore;
	}

	protected void setHScore(int hScore) {
		RecordStore rs = null;
		try {
			rs = RecordStore.openRecordStore(dbName, false);

			byte[] data = Long.toString(hScore).getBytes();
			rs.setRecord(1, data, 0, data.length);
			if (rs != null)
				rs.closeRecordStore();
		} catch (RecordStoreException e) {
			e.printStackTrace();
		}
	}

	protected void start() {
		flag = new boolean[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				flag[i][j] = false;
			}
		}
		stage = new int[width][height];
		state = new int[width][height];
		koshin = new boolean[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i * height + j < bombSize) {
					stage[i][j] = BOMB;
				} else {
					stage[i][j] = 0;
				}
				state[i][j] = NOT_VISIBLE;
				koshin[i][j] = false;
			}
		}
		int c = 0;
		while (c < width * height) {
			int x1 = Math.abs(random.nextInt() % width);
			int y1 = Math.abs(random.nextInt() % height);
			int x2 = Math.abs(random.nextInt() % width);
			int y2 = Math.abs(random.nextInt() % height);
			if ((stage[x1][y1] == BOMB || stage[x2][y2] == BOMB)
					&& !(stage[x1][y1] == BOMB && stage[x2][y2] == BOMB)) {
				int a = stage[x1][y1];
				stage[x1][y1] = stage[x2][y2];
				stage[x2][y2] = a;
				c++;
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (stage[i][j] == BOMB) {
					for (int x = -1; x < 2 && x + i < width; x++) {
						for (int y = -1; y < 2 && y + j < height; y++) {
							if (0 <= x + i && 0 <= y + j
									&& stage[x + i][y + j] != BOMB) {
								stage[x + i][y + j]++;
							}
						}
					}
				}
			}
		}
		startTime = 0;
		endTime = 0;
		isGame = true;
	}

	public boolean open(int x, int y) {
		if (x < width && y < height) {
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			if (state[x][y] == NOT_VISIBLE && !flag[x][y]) {
				state[x][y] = VISIBLE;
				koshin[x][y] = true;
				boolean win = false;
				if (stage[x][y] == BOMB) {
					endTime = System.currentTimeMillis();
					isGame = false;
					for (int i = 0; i < width; i++) {
						for (int j = 0; j < height; j++) {
							if ((stage[i][j] == BOMB && !flag[i][j])
									|| (stage[i][j] != BOMB && flag[i][j])) {
								state[i][j] = VISIBLE;
								koshin[i][j] = true;
							}
						}
					}
					win = false;
				} else {
					if (stage[x][y] == 0) {
						for (int i = -1; i < 2 && x + i < width; i++) {
							for (int j = -1; j < 2 && y + j < height; j++) {
								if (0 <= x + i && 0 <= y + j) {
									open0(x + i, y + j);
								}
							}
						}
					}
					boolean isEmptyBombExist = false;
					for (int i = 0; i < width; i++) {
						for (int j = 0; j < height; j++) {
							if (state[i][j] == NOT_VISIBLE
									&& stage[i][j] != BOMB) {
								isEmptyBombExist = true;
							}
						}
					}
					if (!isEmptyBombExist || bombSize == 0) {
						endTime = System.currentTimeMillis();
						isGame = false;
						int nowTime = getNowTime();
						if (999 < nowTime) {
							setHScore(999);
						} else if (getHScore() > nowTime) {
							setHScore(nowTime);
						}
						for (int i = 0; i < width; i++) {
							for (int j = 0; j < height; j++) {
								if (stage[i][j] == BOMB && !flag[i][j]) {
									flag[i][j] = true;
									koshin[i][j] = true;
								}
							}
						}
						win = true;
					}
				}
				boolean[][] a = new boolean[width][height];
				for (int i = 0; i < width; i++) {
					for (int j = 0; j < height; j++) {
						a[i][j] = koshin[i][j];
						koshin[i][j] = false;
					}
				}
				msl.blockOpened(a);
				if (!isGame) {
					msl.gameOver(win);
				}
				return true;
			}
		}
		return false;
	}

	public void open0(int x, int y) {
		if (x < width && y < height) {
			if (state[x][y] == NOT_VISIBLE && !flag[x][y]) {
				state[x][y] = VISIBLE;
				koshin[x][y] = true;
				if (stage[x][y] == 0) {
					for (int i = -1; i < 2 && x + i < width; i++) {
						for (int j = -1; j < 2 && y + j < height; j++) {
							if (0 <= x + i && 0 <= y + j) {
								open0(x + i, y + j);
							}
						}
					}
				}
			}
		}
	}

	public boolean getFlag(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height) {
			return flag[x][y];
		}
		return false;
	}

	public void setFlag(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height
				&& state[x][y] == NOT_VISIBLE) {
			flag[x][y] = !flag[x][y];
		}
	}

	public void setFlag(boolean flag, int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height) {
			this.flag[x][y] = flag;
		}
	}

	public int getNumber(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height) {
			return stage[x][y];
		}
		return ATHER;
	}

	public int getState(int x, int y) {
		if (0 <= x && x < width && 0 <= y && y < height) {
			return state[x][y];
		}
		return ATHER;
	}

	public int getTrueFlagCount() {
		int count = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (flag[i][j]) {
					count++;
				}
			}
		}
		return count;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getBombSize() {
		return bombSize;
	}

	public int getNowTime() {
		if (startTime == 0) {
			return 0;
		} else if (!isGame) {
			return (int) ((endTime - startTime) / 1000);
		} else {
			return (int) ((System.currentTimeMillis() - startTime) / 1000);
		}
	}
}