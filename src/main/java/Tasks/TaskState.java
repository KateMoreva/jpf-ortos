package Tasks;

public enum TaskState {

    RUNNING("RUNNING"),
    READY("READY"),
    WAITING("WAITING");


    private String name;

    TaskState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
