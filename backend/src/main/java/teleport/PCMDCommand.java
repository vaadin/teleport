package teleport;

public abstract class PCMDCommand implements DroneCommand {

	protected static final String POINT_05 = "1028443341";
	protected static final String POINT_1 = "1036831949";
	protected static final String POINT_2 = "1045220557";
	protected static final String POINT_5 = "1056964608";

	protected static final String MINUS_POINT_05 = "-1119040307";
	protected static final String MINUS_POINT_1 = "-1110651699";
	protected static final String MINUS_POINT_2 = "-1102263091";
	protected static final String MINUS_POINT_5 = "-1090519040";

	protected abstract String wrap(String command, int commandSequenceNumber);

	@Override
	public String name() {
		return getClass().getSimpleName();
	}
}
