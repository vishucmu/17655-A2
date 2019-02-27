package MessagePackage;

public enum MessageType {

    Mix(-9999),
    Monitor(0),
    TempConfirm(-5),
    HumiConfirm(-4),
    TempReading(1),
    HumiReading(2),
    HumiAction(4),
    TempAction(5);

    private int value;

    private MessageType(int v){
        this.value = v;
    }
}
