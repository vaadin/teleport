package teleport;

public class MoveDownCommand extends PcmdCommand {
    public MoveDownCommand(int commandSeqNo, float gaz) {
        super(commandSeqNo, 0, 0, -1 * gaz, 0);
    }
}
