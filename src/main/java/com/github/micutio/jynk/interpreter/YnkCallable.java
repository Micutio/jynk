package com.github.micutio.jynk.interpreter;

import com.github.micutio.jynk.interpreter.Interpreter;
import java.util.List;

interface YnkCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
