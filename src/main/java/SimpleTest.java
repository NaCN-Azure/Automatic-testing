import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraphStats;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/*
    简单的代码测试区，没有其他用处，就是debug用的
 */
public class SimpleTest {
    public static List<String> test=new ArrayList<>();

    public static void main(String[] args) throws CancelException, IOException, ClassHierarchyException, InvalidClassFileException {
        /* 省略构建分析域（AnalysisScope）对象scope的过程 */
        inittest();
        ClassLoader classLoader=SimpleTest.class.getClassLoader();
        AnalysisScope scope=AnalysisScopeReader.readJavaScope("scope.txt",new File("E:\\程序\\java个人包\\经典自动化\\src\\main\\resources\\exclusion.txt"),classLoader);
        File rootfile = new File("E:\\程序\\java个人包\\经典自动化\\data\\ClassicAutomatedTesting\\2-DataLog\\target");
        List<File> res = new ArrayList<File>();
        List<File> class_list = listFiles(rootfile, res);
        for (File file : class_list)
            scope.addClassFileToScope(ClassLoaderReference.Application, file);

// 1.生成类层次关系对象
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
// 2.生成进入点
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
// 3.利用CHA算法构建调用图
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
// 4.遍历cg中所有的节点
        for(CGNode node: cg) {
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();

                    String now=classInnerName + " " + signature;

                    //System.out.println("此方法" + now);

                    if(test.contains(now)) {
                        All(node, cg);
                    }

                }
            } else {
                //System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s",node.getMethod(), node.getMethod().getClass()));
            }
        }
    }
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

    private static void All(CGNode node,CHACallGraph cg){
        ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
        String classInnerName = method.getDeclaringClass().getName().toString();
        if("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
            // 获取方法签名
            String signature = method.getSignature();
            String now = classInnerName + " " + signature;
            System.out.println("此方法" + now);
            Iterator<CGNode> a = cg.getSuccNodes(node);
            System.out.println("此方法测试:{");
            while (a.hasNext()) {
                CGNode x = a.next();
                ShrikeBTMethod m = (ShrikeBTMethod) x.getMethod();
                if ("Application".equals(m.getDeclaringClass().getClassLoader().toString())) {
                    System.out.println(m.getDeclaringClass().getName().toString() + " " + m.getSignature());
                }
                All(x,cg);
            }
            System.out.println("}\n");
        }
    }

    private static void inittest(){
        test.add("Lnet/mooctest/DatalogTest3 net.mooctest.DatalogTest3.testFact()V");
    }
}
