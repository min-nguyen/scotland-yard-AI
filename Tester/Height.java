package Tester;

/**
 * Created by minni on 05/04/2016.
 */
public class Height {
    private int x = 0;
    public Height(int x){
        this.x = x;
    }
    public Height(Height height){
        this.x = height.x;
    }
    public void setHeight(int n){
        x = n;
    }
    public int getHeight(){
        return x;
    }
}
