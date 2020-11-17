import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/*
    输出txt方法
 */
public class Output {

    public List<String> SelectionClass=new ArrayList<String>();

    public List<String> SelectionMethod=new ArrayList<String>();

    private String SelectionClassName="selection-class.txt";

    private String SelectionMethodName="selection-method.txt";

    //输出两个txt
    public Output(){}

    public void SelectionClass(List<String> selectionClass) throws IOException {
        this.SelectionClass=selectionClass;
        Utils.print(this.SelectionClass,SelectionClassName);
    }

    public void SelectionMethod(List<String> selectionMethod) throws IOException {
        this.SelectionMethod=selectionMethod;;
        Utils.print(this.SelectionMethod,SelectionMethodName);
    }
}
