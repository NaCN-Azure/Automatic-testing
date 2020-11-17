import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
/*
    常用方法库
 */
public class Utils {
    public static List<String> removal(List<String>x){
        LinkedHashSet<String> hashSet = new LinkedHashSet<>(x);
        x = new ArrayList<>(hashSet);
        return x;
    }
    public static Boolean isTest(String x){
        return x.contains("Test");
    }
    public static Boolean isMain(String signature){
        return signature.contains("<init>");
    }
    public static Boolean isinitialize(String x){return x.contains("initialize()");}
    public static void print(List<String> output,String path) throws IOException {
        File file=new File(path);
        Writer writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        for(String x:output){
            writer.write(x+"\r\n");
        }
        writer.flush();
        writer.close();
    }
}
