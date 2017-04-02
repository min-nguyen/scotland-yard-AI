package Tester;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minni on 05/04/2016.
 */
public class Main {
    //How to use copy-constructor
    public static void main(String args[]){
        Height A = new Height(5);
        Height B = new Height(A);
        B.setHeight(10);
        System.out.println(A + "  has height " + A.getHeight()+ " + " + B + " has height " + B.getHeight());
    }
}
