package MessagePackage;

public enum QueueState {

    Active(0),
    Ready(1);

    private int state;

    private QueueState(int s){
        this.state = s;
    }

}
