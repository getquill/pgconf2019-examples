package io.getquill.java;

import jdk.nashorn.internal.runtime.Context;
import sun.font.Script;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.FileReader;

public class FormatterInEngine {

  private ScriptEngine engine;

  public FormatterInEngine() throws Exception {
    engine = startEngine();
  }

  public String format(String sql) throws Exception {
    String command = "sqlFormatter.format(\"" + sql +  "\")";
    Object result = engine.eval(command);
    return result + "";
  }

  public ScriptEngine startEngine() throws Exception {
    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
    engine.eval(new FileReader("etc/sql-formatter.js"));
    return engine;
  }
}
