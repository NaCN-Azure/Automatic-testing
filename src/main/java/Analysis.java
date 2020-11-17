import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.util.CancelException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/*
    主方法，用于分析
 */
public class Analysis {

    public static TxtLoader txtLoader;
    public static GraphMaker graphMaker;
    public static Output output;
    public static Input input;
    public static CHACallGraph cg;
    public static List<String> SelectMethod;
    public static List<String> SelectClass;

    public static void run(String[] args) throws CancelException, ClassHierarchyException, InvalidClassFileException, IOException {
        input=new Input(args);
        graphMaker=new GraphMaker(input.ClassPath);
        System.out.println("CG picture has made!");
        cg=graphMaker.cg;
        graphMaker.outclassGraph();//生成类依赖图
        graphMaker.outmethodGraph();//方法依赖图
        System.out.println("Depending picture has made!");
        //graphMaker.outdotmessage();//输出图片的方法，打包成jar后不需要
        output=new Output();
        txtLoader=new TxtLoader(input.ChangeInfoPath);
        System.out.println("Changed method has been load!");
        if(input.type==0) {
            selectClass();
            System.out.println("Class done");
        }
        else if(input.type==1) {
            selectMethod();
            System.out.println("Method done");
        }
    }

    private static void selectClass() throws IOException {
        SelectClass=new ArrayList<>();
        List<String> HasChangedClass=new ArrayList<>();
        for(String checkingClass:txtLoader.classChanges){
            for(String temp: graphMaker.dot_graph){
                String start=temp.split(" ")[0];
                String end=temp.split(" ")[1];
                if(start.compareTo(checkingClass)==0 && Utils.isTest(end)){
                    HasChangedClass.add(end);
                }
            }
        }
        for(CGNode node:cg){
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    if(HasChangedClass.contains(classInnerName)&& !Utils.isMain(signature)&&!Utils.isinitialize(signature)){
                        SelectClass.add(classInnerName+" "+signature);
                    }
                }
            }
        }
        SelectClass=Utils.removal(SelectClass);
        output.SelectionClass(SelectClass);
    }

    private static void selectMethod() throws IOException {
        SelectMethod=new ArrayList<String>();
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    if(Utils.isTest(signature)){
                        if(containMethod(node,txtLoader.methodChanges,signature)&& !Utils.isMain(signature)){
                            SelectMethod.add(classInnerName+" "+signature);
                        }
                    }
                }
            }
        }
        SelectMethod=Utils.removal(SelectMethod);
        output.SelectionMethod(SelectMethod);
    }

    private static Boolean containMethod(CGNode node,List<String> changes,String signature){
        Iterator<CGNode> succNodes=cg.getSuccNodes(node);
        if(!succNodes.hasNext()) return false;
        Boolean result=false;
        while(succNodes.hasNext()){
            CGNode x=succNodes.next();
            if (!"Application".equals(x.getMethod().getDeclaringClass().getClassLoader().toString())) continue;
            String succSignature = x.getMethod().getSignature();
            for(String single:changes){
                if(single.compareTo(succSignature)==0){
                    return true;
                }
            }
            result=containMethod(x,changes,succSignature);
            if(result){
                break;
            }
        }
        return result;
    }

}
