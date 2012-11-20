public class MineSweeperLevel1Field extends MineSweeperField {

	public MineSweeperLevel1Field(MineSweeperListener l) {
		super(l);
		width = 8;
		height = 8;
		bombSize = 10;
		dbName = "syokyu";
	}
}