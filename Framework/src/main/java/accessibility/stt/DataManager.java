package accessibility.stt;

import java.util.ArrayList;

/**
 *  This is a data abstraction for managing the output data from an AudioProcessor
 */

public class DataManager {

    public static String FINAL_RESULT = "final";
    public static String PARTIAL_RESULT = "partial";
    public static String RESULT = "result";

    ArrayList<String> result = new ArrayList<String>();
    ArrayList<String> partialResult = new ArrayList<String>();
    ArrayList<String> finalResult = new ArrayList<String>();

    public ArrayList<String> retrieve(String category){
        if(category.equals(RESULT)){
            return result;
        }else if(category.equals(PARTIAL_RESULT)){
            return partialResult;
        }else if(category.equals(FINAL_RESULT)){
            return finalResult;
        }
        return null;
    }

    public void push(String category,String data){}
    public void pop(String category){}
    public void popAll(String category){}
    public void getAllCategories(){
    }
}
