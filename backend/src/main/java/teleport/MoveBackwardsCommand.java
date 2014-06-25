package teleport;

class MoveBackwardsCommand extends PcmdCommand {

    public MoveBackwardsCommand(int commandSeqNo, float speed) {
        super(commandSeqNo, speed * -1, 0, 0, 0);
    }
}
