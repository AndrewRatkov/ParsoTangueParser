package src.consts;

import java.util.List;

public class FunctionInfo {
    public String name;
    public List<Type> input_types;
    public FunctionReturnType output_type; 

    public FunctionInfo(String _name, List<Type> _input_types, FunctionReturnType _output_type) {
        name= _name;
        input_types = _input_types;
        output_type = _output_type;
    }
}
