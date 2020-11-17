import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class TxtLoader {
    //读入change_info.txt自我生成改动依赖
    public List<String> changes=new ArrayList<String>();
    public List<String> classChanges=new ArrayList<String>();
    public List<String> methodChanges=new ArrayList<String>();

    public TxtLoader(String TxtPath) throws IOException {
            FileReader fileReader =new FileReader(TxtPath);
            BufferedReader bufferedReader =new BufferedReader(fileReader);
            String str=null;
            while((str=bufferedReader.readLine())!=null) {
                if(str.trim().length()>2) {
                    changes.add(str);
                }
            }
            init();
    }

    private void init(){
        for(String x:changes){
            classChanges.add(x.split(" ")[0]);
            methodChanges.add(x.split(" ")[1]);
        }
        classChanges=Utils.removal(classChanges);
    }
}
