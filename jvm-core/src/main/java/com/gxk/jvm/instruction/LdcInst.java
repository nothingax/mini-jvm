package com.gxk.jvm.instruction;

import com.gxk.jvm.rtda.Frame;
import com.gxk.jvm.rtda.Slot;
import com.gxk.jvm.rtda.heap.Heap;
import com.gxk.jvm.rtda.heap.KArray;
import com.gxk.jvm.rtda.heap.Class;
import com.gxk.jvm.rtda.heap.Field;
import com.gxk.jvm.rtda.heap.KObject;

public class LdcInst implements Instruction {
  public final String descriptor;
  public final Object val;

  @Override
  public int offset() {
    return 2;
  }

  public LdcInst(String descriptor, Object val) {
    this.descriptor = descriptor;
    this.val = val;
  }

  @Override
  public void execute(Frame frame) {
    switch (descriptor) {
      case "I":
        frame.pushInt(((Integer) val));
        break;
      case "F":
        frame.pushFloat(((float) val));
        break;
      case "Ljava/lang/String":
        Class klass = Heap.findClass("java/lang/String");
        if (klass == null) {
          klass = frame.method.clazz.classLoader.loadClass("java/lang/String");
        }
        if (!klass.getStat()) {
          Frame newFrame = new Frame(klass.getMethod("<clinit>", "()V"));
          klass.setStat(1);
          Class finalKlass = klass;
          newFrame.setOnPop(() -> finalKlass.setStat(2));
          frame.thread.pushFrame(newFrame);

          frame.nextPc = frame.getPc();
          return;
        }
        KObject object = klass.newObject();
        Field field = object.getField("value", "[C");
        Class arrClazz = new Class(1, "[C", frame.method.clazz.classLoader, null);

        char[] chars = val.toString().toCharArray();
        Character[] characters = new Character[chars.length];
        for (int i = 0; i < chars.length; i++) {
          characters[i] = chars[i];
        }
        KArray arr = new KArray(arrClazz, characters);
        field.val = new Slot[]{new Slot(arr)};
        frame.pushRef(object);
        break;
      case "L":
        Class klass2 = Heap.findClass(val.toString());
        if (klass2 == null) {
          klass2 = frame.method.clazz.classLoader.loadClass(val.toString());
        }
        frame.pushRef(klass2.getRuntimeClass());
        break;
      default:
        frame.pushRef(val);
        break;
    }
  }

  @Override
  public String format() {
    return "ldc " + descriptor + " " + val;
  }

  @Override
  public String toString() {
    return "LdcInst{" +
        "descriptor='" + descriptor + '\'' +
        ", val=" + val +
        '}';
  }
}
