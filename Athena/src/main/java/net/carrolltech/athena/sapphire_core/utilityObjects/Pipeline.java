package net.carrolltech.athena.sapphire_core.utilityObjects;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// I don't know if having a Route object is the most descriptive way to handle the probelm
public class Pipeline {

    // This is the packageName;className combo
    ArrayList<String> apps = null;
    int index = 0;
    String packageName = "";
    String className = "";


    public Pipeline(String pipelineString){
        apps.addAll(deserialize(pipelineString));
    }

    // This is used internally. Just creating a new Route object will deserialize
    private List<String> deserialize(String pipelineString){
        // This turns all of the packageName;className pairs into their own string
        return Arrays.asList(pipelineString.split(","));
    }

    public void Next(){
        index++;
        setPackageName(index);
        setClassName(index);
    }

    // I need to do error check if it is at 0
    public void Last(){
        index--;
        setPackageName(index);
        setClassName(index);
    }

    private void setPackageName(int index){
        String packageClassName = apps.get(index);
        String[] formatted = packageClassName.split(";");
        packageName = formatted[0];
    }

    public String getPackageName(){
        return packageName;
    }

    private void setClassName(int index){
        String packageClassName = apps.get(index);
        String[] formatted = packageClassName.split(";");
        className = formatted[1];
    }

    public String getClassName() {
        return className;
    }

    @NonNull
    @Override
    public String toString() {
        String pipeline = apps.get(0);

        if(apps.size() >= 2) {
            for (String app : apps.subList(1, apps.size() - 1)) {
                pipeline = pipeline.concat("," + app);
            }
        }
        return pipeline;
    }
}
