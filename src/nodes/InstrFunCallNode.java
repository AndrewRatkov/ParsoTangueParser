package src.nodes;

public class InstrFunCallNode implements InstrNode {
    private FunNode fun_call;

    public InstrFunCallNode(FunNode _fun_call) {
        this.fun_call = _fun_call;
    }

    public void buildTree() {}

    public String getTree() {
        return fun_call.getTree();
    }
}
