import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.*;

/*
    CG图制作地点
 */
public class GraphMaker {

    private static String ExclusionPath="E:\\程序\\java个人包\\经典自动化\\src\\main\\resources\\exclusion.txt";

    public List<String> dot_class_message=new ArrayList<String>();

    public List<String> dot_method_message=new ArrayList<String>();

    public List<String> dot_graph=new ArrayList<>();

    private List<File> class_list;

    public CHACallGraph cg;

    public AnalysisScope scope;

    private static List<File> listFiles(File rootfile, List<File> res) {
        File[] fs=rootfile.listFiles();
        for(File f:fs){
            if(f.isDirectory()){
                listFiles(f,res);
            }
            else if(f.isFile()){
                if(f.getName().endsWith(".class")){
                    res.add(f);
                }
            }
        }
        return res;
    }

    public GraphMaker(String path) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        ClassLoader classLoader=GraphMaker.class.getClassLoader();
        scope= AnalysisScopeReader.readJavaScope("scope.txt",new File(ExclusionPath),classLoader);
        File rootfile = new File(path);
        List<File> res = new ArrayList<File>();
        class_list = listFiles(rootfile, res);
        for (File file : class_list)
            scope.addClassFileToScope(ClassLoaderReference.Application, file);
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        cg = new CHACallGraph(cha);
        cg.init(eps);
    }

    public void outclassGraph() {
        String title="digraph class {";
        dot_class_message.add(title);
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    if(!Utils.isTest(signature)){
                        Iterator<CGNode> predNodes= cg.getPredNodes(node);
                        if(!predNodes.hasNext()) continue;
                        while(predNodes.hasNext()){
                            CGNode x=predNodes.next();
                            if("Application".equals(x.getMethod().getDeclaringClass().getClassLoader().toString())) {
                                String preclassInnerName=x.getMethod().getDeclaringClass().getName().toString();
                                dot_class_message.add("    \""+classInnerName+"\" -> \""+preclassInnerName+"\";");
                                dot_graph.add(classInnerName+" "+preclassInnerName);
                            }
                        }
                    }
                }
            }
        }
        dot_class_message.add("}");
        dot_class_message=Utils.removal(dot_class_message);
        dot_graph=Utils.removal(dot_graph);
        Collections.sort(dot_graph);
    }

    public void outmethodGraph(){
        String title="digraph method {";
        dot_method_message.add(title);
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                        Iterator<CGNode> predNodes= cg.getPredNodes(node);
                        if(!predNodes.hasNext()) continue;
                        while(predNodes.hasNext()){
                            CGNode x=predNodes.next();
                            if("Application".equals(x.getMethod().getDeclaringClass().getClassLoader().toString())) {
                                String presignature=x.getMethod().getSignature();
                                dot_method_message.add("    \""+signature+"\" -> \""+presignature+"\";");
                            }
                        }
                }
            }
        }
        dot_method_message.add("}");
        dot_method_message=Utils.removal(dot_method_message);
    }

    public void outdotmessage() throws IOException {
        String outPath="E:\\程序\\java个人包\\经典自动化\\report\\5-MoreTriangle";
        String classDot=outPath+"\\class.dot";
        String methodDot=outPath+"\\method.dot";
        Utils.print(dot_class_message,classDot);
        Utils.print(dot_method_message,methodDot);
    }
}
