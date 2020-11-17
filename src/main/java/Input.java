/*
    输入分析区
 */
public class Input {
    public String ClassPath;
    public String ChangeInfoPath;
    public int type;
    public Input(String[] args){
        String command=args[0];
        ClassPath=args[1];
        ChangeInfoPath=args[2];
        if(command.compareTo("-c")==0){
            type=0;
        }
        else if(command.compareTo("-m")==0){
            type=1;
        }
    }
}
